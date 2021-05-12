package hsbctest.question_three;

import hsbctest.Event;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestEventThrottler {

    @Test
    public void test() {
        MockClock clock = new MockClock();
        EventThrottler throttler = new EventThrottler(10, clock);
        clock.setNow(100);
        throttler.submitEvent(new Event("a"));
        assertEquals(Throttler.ThrottleResult.PROCEED, throttler.shouldProceed());
        clock.setNow(101);
        throttler.submitEvent(new Event("b"));
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
    }



    static class MockClock implements Clock {

        private long now = 0l;
        private void setNow(long now) {
            this.now = now;
        }
        @Override
        public long now() {
            return now;
        }
    }
}
