package hsbctest.question_one;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestProbabilisticRandomGenImpl {

    private float randomNumber = 0f;

    public ProbabilisticRandomGen setupGenerator(List<ProbabilisticRandomGen.NumAndProbability> sample) {
        return new ProbabilisticRandomGenImpl(sample, (min, max) -> randomNumber);
    }

    @Test
    public void correctSelection() {
        ProbabilisticRandomGen generator = setupGenerator(List.of(
                new ProbabilisticRandomGen.NumAndProbability(1, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(2, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(3, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(4, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(5, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(6, 3.2f)
        ));

        assertGivenRandomNumber(generator, 0f, 1);
        assertGivenRandomNumber(generator, 3.1f, 1);
        assertGivenRandomNumber(generator, 3.2f, 2);
        assertGivenRandomNumber(generator, 6.3f, 2);
        assertGivenRandomNumber(generator, 6.4f, 3);
        assertGivenRandomNumber(generator, 9.5f, 3);
        assertGivenRandomNumber(generator, 9.6f, 4);
        assertGivenRandomNumber(generator, 12.7f, 4);
        assertGivenRandomNumber(generator, 12.8f, 5);
        assertGivenRandomNumber(generator, 15.9f, 5);
        assertGivenRandomNumber(generator, 16.0f, 6);
        assertGivenRandomNumber(generator, 19.1f, 6);
        assertGivenRandomNumber(generator, 19.2f, 6);
    }
    @Test

    public void outOfRange() {
        ProbabilisticRandomGen generator = setupGenerator(List.of(
                new ProbabilisticRandomGen.NumAndProbability(1, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(2, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(3, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(4, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(5, 3.2f),
                new ProbabilisticRandomGen.NumAndProbability(6, 3.2f)
        ));
        try {
            assertGivenRandomNumber(generator, -0.1f, 6);
            fail("Out of range expected.");
        }
        catch(RuntimeException e) {
            assertEquals("Out of range(-0.1): 0 - 19.2", e.getMessage());
        }

        try {
            assertGivenRandomNumber(generator, 19.3f, 6);
            fail("Out of range expected.");
        }
        catch(RuntimeException e) {
            assertEquals("Out of range(19.3): 0 - 19.2", e.getMessage());
        }
    }

    private void assertGivenRandomNumber(ProbabilisticRandomGen generator, float randomNumber, int expectedValue) {

        this.randomNumber = randomNumber;
        assertEquals(expectedValue, generator.nextFromSample());
    }
}
