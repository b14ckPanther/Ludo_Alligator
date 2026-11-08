package Model;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * The game history in the course CSV file, history.csv (DB-1, DB-3).
 *
 * <p>The header row is copied exactly from the course template, including its
 * spelling ("4rd_place_name"). Rows look like the template's sample row:
 * <pre>1,easy,19/10/2026;12:01:00,Mor,Noy,Safa,Muhammed,01:05:04</pre>
 * Lines end with CRLF like the template. Places 3 and 4 stay empty in games
 * with fewer players.
 *
 * <p>New games are only appended; old rows are never changed or deleted.
 * A row that can't be read is skipped, so one bad line doesn't hide the
 * whole history (REL-2).
 */
public final class CsvHistoryRepository implements HistoryRepository {
    public static final String HEADER = "gameID,difficulty,timestamp(DD/MM/YYYY; HH:MM:SS),"
            + "1st_place_name,2nd_place_name,3rd_place_name,4rd_place_name,game_duration(HH:MM:SS)";
    private static final String EOL = "\r\n";
    private static final int COLUMNS = 8;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd/MM/yyyy;HH:mm:ss");

    private final Path file;
    private final List<GameRecord> records = new ArrayList<>();
    private int lastId;

    public CsvHistoryRepository(Path file) {
        this.file = file;
        load();
    }

    public Path file() {
        return file;
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // can't read it at all: start with an empty history, saving will report the problem
            return;
        }
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                GameRecord record = decode(parseLine(lines.get(i)));
                records.add(record);
                lastId = Math.max(lastId, record.id());
            } catch (RuntimeException e) {
                // damaged or hand-edited row, skip it (REL-2)
            }
        }
    }

    @Override
    public synchronized List<GameRecord> findAll() {
        return records.stream()
                .sorted(Comparator.comparing(GameRecord::endedAt).thenComparing(GameRecord::id).reversed())
                .toList();
    }

    @Override
    public synchronized GameRecord save(GameRecord record) {
        GameRecord numbered = record.withId(lastId + 1);
        StringBuilder out = new StringBuilder();
        try {
            Path folder = file.toAbsolutePath().getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            if (!Files.exists(file) || Files.size(file) == 0) {
                out.append(HEADER).append(EOL);
            } else if (!endsWithNewline()) {
                // someone removed the last line break, don't glue our row to theirs
                out.append(EOL);
            }
            out.append(encode(numbered)).append(EOL);
            Files.writeString(file, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Can't write " + file.getFileName() + ": " + e.getMessage(), e);
        }
        records.add(numbered);
        lastId = numbered.id();
        return numbered;
    }

    private boolean endsWithNewline() throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return bytes.length > 0 && bytes[bytes.length - 1] == '\n';
    }

    // ------------------------------------------------------------------ one row

    static String encode(GameRecord r) {
        List<String> cols = new ArrayList<>();
        cols.add(String.valueOf(r.id()));
        cols.add(r.difficulty().csvName());
        cols.add(TIMESTAMP.format(r.endedAt()));
        for (int i = 0; i < GameConfig.MAX_PLAYERS; i++) {
            cols.add(i < r.places().size() ? r.places().get(i) : "");
        }
        cols.add(formatDuration(r.durationSeconds()));
        List<String> quoted = new ArrayList<>();
        for (String c : cols) {
            quoted.add(quote(c));
        }
        return String.join(",", quoted);
    }

    static GameRecord decode(List<String> cols) {
        if (cols.size() < COLUMNS) {
            throw new IllegalArgumentException("Row has " + cols.size() + " columns, expected " + COLUMNS);
        }
        int id = Integer.parseInt(cols.get(0).strip());
        Difficulty difficulty = Difficulty.valueOf(cols.get(1).strip().toUpperCase(Locale.ROOT));
        // the template header shows "; " but its sample row has no space, so accept both
        String stamp = cols.get(2).strip().replaceAll(";\\s*", ";");
        LocalDateTime endedAt = LocalDateTime.parse(stamp, TIMESTAMP);
        List<String> places = new ArrayList<>();
        for (int i = 3; i < 3 + GameConfig.MAX_PLAYERS; i++) {
            String name = cols.get(i).strip();
            if (!name.isEmpty()) {
                places.add(name);
            }
        }
        long duration = parseDuration(cols.get(7).strip());
        return new GameRecord(id, endedAt, difficulty, duration, places);
    }

    /** HH:MM:SS, hours always with two digits (DB-1). */
    public static String formatDuration(long seconds) {
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    static long parseDuration(String text) {
        String[] parts = text.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Duration must be HH:MM:SS, got " + text);
        }
        return Long.parseLong(parts[0]) * 3600 + Long.parseLong(parts[1]) * 60 + Long.parseLong(parts[2]);
    }

    /** RFC 4180: a field with a comma, a quote or a line break goes in quotes, and quotes are doubled. */
    static String quote(String field) {
        boolean needsQuotes = field.indexOf(',') >= 0 || field.indexOf('"') >= 0
                || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0;
        return needsQuotes ? '"' + field.replace("\"", "\"\"") + '"' : field;
    }

    /** Splits one CSV line into fields, following RFC 4180 quoting. */
    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields;
    }
}
