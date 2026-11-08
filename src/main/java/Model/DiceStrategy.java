package Model;

import java.util.Random;

/**
 * How a level rolls its dice (Strategy pattern, SDP 6.2).
 * Each difficulty gets its own strategy, so the game doesn't need an
 * if/else on the level every time it rolls.
 */
public interface DiceStrategy {

    DiceRoll roll(Random random);

    /** One fair step die: 1 to 6, each face with the same chance (P-DIC-3). */
    static int stepDie(Random random) {
        return random.nextInt(6) + 1;
    }
}
