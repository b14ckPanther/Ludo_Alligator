package Model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One finished game, as it is saved in the history file (DB-1).
 *
 * @param id              game number, 1, 2, 3 ... (0 until it is saved)
 * @param endedAt         date and time the game ended
 * @param difficulty      the level that was played
 * @param durationSeconds game time without pauses
 * @param places          player names in finishing order (1st first)
 */
public record GameRecord(int id, LocalDateTime endedAt, Difficulty difficulty, long durationSeconds,
                         List<String> places) {

    public GameRecord {
        places = List.copyOf(places);
        if (places.size() < GameConfig.MIN_PLAYERS || places.size() > GameConfig.MAX_PLAYERS) {
            throw new IllegalArgumentException("A game has 2 to 4 players");
        }
    }

    /** The record of a game that just ended. It gets its number when it is saved. */
    public static GameRecord of(Game game, LocalDateTime endedAt) {
        List<String> names = game.ranking().stream().map(Player::name).toList();
        return new GameRecord(0, endedAt, game.difficulty(), game.clock().elapsedMillis() / 1000, names);
    }

    /** Same record with a game number. */
    public GameRecord withId(int newId) {
        return new GameRecord(newId, endedAt, difficulty, durationSeconds, places);
    }
}
