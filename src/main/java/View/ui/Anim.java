package View.ui;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/**
 * Small helper for animations. All animations share one Swing timer (60 fps)
 * that stops when nothing is animating.
 */
public final class Anim {
    private static final List<Tween> ACTIVE = new ArrayList<>();
    private static final Timer TIMER = new Timer(1000 / 60, e -> tick());
    private static boolean reducedMotion = Boolean.getBoolean("reducedMotion");

    private Anim() { }

    public static boolean reducedMotion() { return reducedMotion; }

    public static void setReducedMotion(boolean value) { reducedMotion = value; }

    /** Calls update with t going from 0 to 1 during the given time, then calls done. */
    public static Tween run(int millis, Ease ease, DoubleConsumer update, Runnable done) {
        Tween t = new Tween(reducedMotion ? Math.min(millis, 60) : millis, ease, update, done);
        ACTIVE.add(t);
        update.accept(0);
        if (!TIMER.isRunning()) TIMER.start();
        return t;
    }

    public static Tween run(int millis, Ease ease, DoubleConsumer update) {
        return run(millis, ease, update, null);
    }

    /** Runs done after a delay. */
    public static Tween delay(int millis, Runnable done) {
        return run(millis, Ease.LINEAR, t -> { }, done);
    }

    private static void tick() {
        long now = System.nanoTime();
        for (Tween t : new ArrayList<>(ACTIVE)) {
            if (t.cancelled) {
                ACTIVE.remove(t);
                continue;
            }
            double p = Math.min(1, (now - t.start) / 1e6 / Math.max(1, t.millis));
            t.update.accept(t.ease.apply(p));
            if (p >= 1) {
                ACTIVE.remove(t);
                if (t.done != null) t.done.run();
            }
        }
        if (ACTIVE.isEmpty()) TIMER.stop();
    }

    public static final class Tween {
        private final long start = System.nanoTime();
        private final int millis;
        private final Ease ease;
        private final DoubleConsumer update;
        private final Runnable done;
        private boolean cancelled;

        Tween(int millis, Ease ease, DoubleConsumer update, Runnable done) {
            this.millis = millis;
            this.ease = ease;
            this.update = update;
            this.done = done;
        }

        public void cancel() { cancelled = true; }
    }

    /** Easing curves (how fast t moves at the start and end). */
    public enum Ease {
        LINEAR {
            public double apply(double t) { return t; }
        },
        OUT_CUBIC {
            public double apply(double t) { return 1 - Math.pow(1 - t, 3); }
        },
        IN_OUT_CUBIC {
            public double apply(double t) {
                return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
            }
        },
        OUT_BACK {
            public double apply(double t) {
                double c1 = 1.70158;
                double c3 = c1 + 1;
                return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
            }
        },
        OUT_EXPO {
            public double apply(double t) { return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t); }
        };

        public abstract double apply(double t);
    }

    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    public static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }
}
