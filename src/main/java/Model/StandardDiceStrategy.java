package Model;

import java.util.Random;

/** Easy level: one step die only (P-BRD-1, P-DIC-3). */
public final class StandardDiceStrategy implements DiceStrategy {

    @Override
    public DiceRoll roll(Random random) {
        return new DiceRoll(DiceStrategy.stepDie(random));
    }
}
