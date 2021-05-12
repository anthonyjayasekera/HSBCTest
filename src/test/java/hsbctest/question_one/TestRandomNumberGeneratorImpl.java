package hsbctest.question_one;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestRandomNumberGeneratorImpl {

    @Test(expected = RuntimeException.class)
    public void invalidRange() {
        new RandomNumberGeneratorImpl().generate(2f, 1f);
    }

    /**
     * Here's an interesting question. Is this test complete? Can it ever be complete??
     */
    @Test
    public void withinRange() {
        RandomNumberGenerator generator = new RandomNumberGeneratorImpl();
        float num = generator.generate(1f, 2f);
        assertTrue(String.format("num=%s", num),num >= 1f);
        assertTrue(String.format("num=%s", num),num <= 2f);
    }
}
