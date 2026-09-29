package in.devexis.splitfair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class SplitCalculatorTest {

    @Test
    public void singlePersonWithTip() {
        SplitCalculator.Result r = SplitCalculator.calculate(10000, 15, 1, false);
        assertEquals(1500, r.tipCents);
        assertEquals(11500, r.totalCents);
        assertEquals(11500, r.perPersonCents);
        assertEquals(0, r.extraCents);
    }

    @Test
    public void evenSplitNoTip() {
        SplitCalculator.Result r = SplitCalculator.calculate(8000, 0, 4, false);
        assertEquals(0, r.tipCents);
        assertEquals(2000, r.perPersonCents);
        assertEquals(8000, r.collectedCents);
        assertEquals(0, r.extraCents);
    }

    @Test
    public void unevenSplitNeverFallsShort() {
        SplitCalculator.Result r = SplitCalculator.calculate(10000, 0, 3, false);
        assertEquals(3334, r.perPersonCents);
        assertEquals(10002, r.collectedCents);
        assertEquals(2, r.extraCents);
    }

    @Test
    public void roundUpToWholeUnit() {
        SplitCalculator.Result r = SplitCalculator.calculate(4250, 10, 3, true);
        assertEquals(425, r.tipCents);
        assertEquals(4675, r.totalCents);
        assertEquals(1600, r.perPersonCents);
        assertEquals(4800, r.collectedCents);
        assertEquals(125, r.extraCents);
    }

    @Test
    public void roundUpKeepsExactWholeShares() {
        SplitCalculator.Result r = SplitCalculator.calculate(9000, 0, 3, true);
        assertEquals(3000, r.perPersonCents);
        assertEquals(0, r.extraCents);
    }

    @Test
    public void tipRoundsHalfUp() {
        // 10% of 10.05 = 1.005 -> 1.01
        assertEquals(101, SplitCalculator.calculate(1005, 10, 1, false).tipCents);
    }

    @Test
    public void zeroBillGivesZeros() {
        SplitCalculator.Result r = SplitCalculator.calculate(0, 20, 5, true);
        assertEquals(0, r.totalCents);
        assertEquals(0, r.perPersonCents);
        assertEquals(0, r.extraCents);
    }

    @Test
    public void collectedAlwaysCoversTotal() {
        for (long bill = 0; bill <= 5000; bill += 137) {
            for (int people = 1; people <= 12; people++) {
                for (int tip = 0; tip <= 30; tip += 5) {
                    for (boolean round : new boolean[] {false, true}) {
                        SplitCalculator.Result r = SplitCalculator.calculate(bill, tip, people, round);
                        assertEquals(r.perPersonCents * people, r.collectedCents);
                        assertEquals(r.collectedCents - r.totalCents, r.extraCents);
                        assertEquals(true, r.extraCents >= 0);
                        assertEquals(true, r.extraCents < (round ? 100L * people : (long) people));
                    }
                }
            }
        }
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> SplitCalculator.calculate(-1, 10, 2, false));
        assertThrows(IllegalArgumentException.class, () -> SplitCalculator.calculate(100, -5, 2, false));
        assertThrows(IllegalArgumentException.class, () -> SplitCalculator.calculate(100, Double.NaN, 2, false));
        assertThrows(IllegalArgumentException.class, () -> SplitCalculator.calculate(100, 10, 0, false));
        assertThrows(IllegalArgumentException.class,
                () -> SplitCalculator.calculate(SplitCalculator.MAX_BILL_CENTS + 1, 10, 2, false));
    }

    @Test
    public void parsesUserInput() {
        assertEquals(1250, SplitCalculator.parseCents("12.5"));
        assertEquals(1250, SplitCalculator.parseCents("12,50"));
        assertEquals(1250, SplitCalculator.parseCents("  12.50 "));
        assertEquals(50, SplitCalculator.parseCents(".5"));
        assertEquals(1, SplitCalculator.parseCents("0.005"));
        assertEquals(4200, SplitCalculator.parseCents("42"));
    }

    @Test
    public void parseHandlesBadInput() {
        assertEquals(0, SplitCalculator.parseCents(null));
        assertEquals(0, SplitCalculator.parseCents(""));
        assertEquals(0, SplitCalculator.parseCents("."));
        assertEquals(0, SplitCalculator.parseCents("abc"));
        assertEquals(0, SplitCalculator.parseCents("-3"));
        assertEquals(0, SplitCalculator.parseCents("1.2.3"));
        assertEquals(SplitCalculator.MAX_BILL_CENTS, SplitCalculator.parseCents("99999999999999999999"));
    }
}
