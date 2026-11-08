package Model;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Finds the folder where the game keeps its files: a "data" folder next to
 * the jar (SRS 2.1.8). The path is worked out from where the jar is, so it
 * works the same on Windows and macOS (risk R4).
 *
 * <p>When running from the IDE (classes in target/classes or build/classes)
 * the folder is made in the project folder instead.
 */
public final class DataFiles {
    private DataFiles() { }

    public static Path folder() {
        try {
            Path code = Path.of(DataFiles.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path base = Files.isDirectory(code) ? projectFolder(code) : code.getParent();
            if (base != null) {
                return base.resolve("data");
            }
        } catch (URISyntaxException | SecurityException e) {
            // fall back to the folder the game was started from
        }
        return Path.of("data");
    }

    public static Path historyFile() {
        return folder().resolve("history.csv");
    }

    /** Goes up from the classes folder until it finds the folder with "src" in it. */
    private static Path projectFolder(Path classes) {
        for (Path p = classes; p != null; p = p.getParent()) {
            if (Files.isDirectory(p.resolve("src"))) {
                return p;
            }
        }
        return classes;
    }
}
