package in.devexis.splitfair;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure-Java bill splitting logic (no Android dependencies, so it is unit-testable on the JVM).
 * All money is handled as whole cents in a long to avoid floating point errors.
 */
public final class SplitCalculator {

    /** Largest bill accepted, in cents (100,000,000.00). Keeps every calculation far from overflow. */
    public static final long MAX_BILL_CENTS = 10_000_000_000L;

    private SplitCalculator() {}

    /** Immutable result of a split. */
    public static final class Result {
        public final long billCents;
        public final long tipCents;
        public final long totalCents;
        public final int people;
        /** What each person pays. Chosen so that the group always covers the total. */
        public final long perPersonCents;
        /** Everything collected (perPerson x people). */
        public final long collectedCents;
        /** Collected minus total: the small extra caused by rounding up. Never negative. */
        public final long extraCents;

        Result(long billCents, long tipCents, long totalCents, int people,
               long perPersonCents, long collectedCents, long extraCents) {
            this.billCents = billCents;
            this.tipCents = tipCents;
            this.totalCents = totalCents;
            this.people = people;
            this.perPersonCents = perPersonCents;
            this.collectedCents = collectedCents;
            this.extraCents = extraCents;
        }
    }

    /**
     * @param billCents      bill before tip, in cents (0 .. MAX_BILL_CENTS)
     * @param tipPercent     tip as a percentage of the bill, e.g. 15 for 15% (must be >= 0)
     * @param people         number of people splitting (>= 1)
     * @param roundUpToWhole if true, each share is rounded up to a whole currency unit
     */
    public static Result calculate(long billCents, double tipPercent, int people, boolean roundUpToWhole) {
        if (billCents < 0 || billCents > MAX_BILL_CENTS) {
            throw new IllegalArgumentException("Bill out of range");
        }
        if (Double.isNaN(tipPercent) || Double.isInfinite(tipPercent) || tipPercent < 0) {
            throw new IllegalArgumentException("Tip must be a non-negative number");
        }
        if (people < 1) {
            throw new IllegalArgumentException("At least one person is required");
        }

        long tip = BigDecimal.valueOf(billCents)
                .multiply(BigDecimal.valueOf(tipPercent))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();
        long total = billCents + tip;

        // Round each share UP so the group never falls short of the total.
        long perPerson = (total + people - 1) / people;
        if (roundUpToWhole) {
            perPerson = ((perPerson + 99) / 100) * 100;
        }
        long collected = perPerson * people;
        return new Result(billCents, tip, total, people, perPerson, collected, collected - total);
    }

    /**
     * Parses user input such as "12.5", "12,50" or ".5" into cents.
     * Blank, invalid or negative input gives 0; oversized input is capped at MAX_BILL_CENTS.
     */
    public static long parseCents(String text) {
        if (text == null) {
            return 0;
        }
        String cleaned = text.trim().replace(',', '.');
        if (cleaned.isEmpty() || cleaned.equals(".")) {
            return 0;
        }
        try {
            BigDecimal value = new BigDecimal(cleaned);
            if (value.signum() <= 0) {
                return 0;
            }
            BigDecimal cents = value.setScale(2, RoundingMode.HALF_UP).movePointRight(2);
            if (cents.compareTo(BigDecimal.valueOf(MAX_BILL_CENTS)) > 0) {
                return MAX_BILL_CENTS;
            }
            return cents.longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            return 0;
        }
    }
}
