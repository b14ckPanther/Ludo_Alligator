package Model;

import java.util.List;

/**
 * Where finished games are kept (Repository pattern, SDP 6.2).
 * The rest of the game only uses this interface, so it doesn't care that
 * the games are stored in a CSV file.
 */
public interface HistoryRepository {

    /** All saved games, newest first (P-HIS-1). */
    List<GameRecord> findAll();

    /**
     * Adds a finished game at the end of the history (P-END-4) and returns it
     * with its game number.
     *
     * @throws java.io.UncheckedIOException if the file can't be written
     */
    GameRecord save(GameRecord record);
}
