package Model;

/**
 * Stopwatch for the game time. It can be paused, and paused time is not
 * counted (needed for the duration in the history file, DB-1).
 */
public final class GameClock {
    private long total;
    private long startedAt = -1;

    public void start() {
        if (startedAt < 0) {
            startedAt = System.nanoTime();
        }
    }

    public void pause() {
        if (startedAt >= 0) {
            total += System.nanoTime() - startedAt;
            startedAt = -1;
        }
    }

    public long elapsedMillis() {
        long running = startedAt >= 0 ? System.nanoTime() - startedAt : 0;
        return (total + running) / 1_000_000;
    }
}
