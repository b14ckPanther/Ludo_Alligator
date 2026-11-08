package View.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Plays screen animations one after another.
 *
 * <p>The model changes at once, but the player should first see the dice
 * land and only then the next turn start. Each step gets a {@code done}
 * callback and has to call it once when its animation is over.
 */
public final class Sequencer {
    private final Deque<Consumer<Runnable>> steps = new ArrayDeque<>();
    private final List<Runnable> waiting = new ArrayList<>();
    private boolean running;

    public void add(Consumer<Runnable> step) {
        steps.add(step);
        if (!running) {
            next();
        }
    }

    public boolean isIdle() {
        return !running && steps.isEmpty();
    }

    /** Runs the callback when all queued steps are finished (right away if nothing is queued). */
    public void whenIdle(Runnable callback) {
        if (isIdle()) {
            callback.run();
        } else {
            waiting.add(callback);
        }
    }

    public void clear() {
        steps.clear();
        waiting.clear();
        running = false;
    }

    private void next() {
        Consumer<Runnable> step = steps.poll();
        if (step == null) {
            running = false;
            List<Runnable> callbacks = new ArrayList<>(waiting);
            waiting.clear();
            callbacks.forEach(Runnable::run);
            return;
        }
        running = true;
        boolean[] called = {false};
        step.accept(() -> {
            if (!called[0]) {
                called[0] = true;
                next();
            }
        });
    }
}
