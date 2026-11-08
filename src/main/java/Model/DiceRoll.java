package Model;

/**
 * The result of one roll.
 * On Easy there is only the step die. The Boost and UltraBoost dice for
 * Medium and Hard will be added here in the next iterations.
 *
 * @param step the step die, 1 to 6
 */
public record DiceRoll(int step) {

    public DiceRoll {
        if (step < 1 || step > 6) {
            throw new IllegalArgumentException("A die shows 1 to 6, got " + step);
        }
    }

    /** A six lets a piece leave the castle and gives another roll (P-DIC-5). */
    public boolean isSix() {
        return step == 6;
    }

    /** How many squares a piece on the track moves. On Easy this is just the step die. */
    public int moveSteps() {
        return step;
    }

    @Override
    public String toString() {
        return String.valueOf(step);
    }
}
