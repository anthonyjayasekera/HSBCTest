package hsbctest.question_three;

import hsbctest.Event;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class TestEventThrottler {

    MockClock clock;
    EventThrottler throttler;

    @Before
    public void setup() {
        clock = new MockClock();
        throttler = new EventThrottler(10, clock);
    }

    @Test
    public void initialEventPull() {
        clock.setNow(100);
        throttler.submitEvent(new Event("a"));
        assertEquals(Throttler.ThrottleResult.PROCEED, throttler.shouldProceed());
        clock.setNow(108);
        assertEquals(Throttler.ThrottleResult.PROCEED, throttler.shouldProceed());
        List<Event> events;
        events = throttler.getEvents();
        assertEquals("a", events.get(0).getId());
        clock.setNow(112);
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
    }

    @Test
    public void unthrottlePull() {
        clock.setNow(100);
        throttler.getEvents();
        clock.setNow(101);
        throttler.submitEvent(new Event("a"));
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
        clock.setNow(103);
        throttler.submitEvent(new Event("b"));
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
        clock.setNow(104);
        throttler.submitEvent(new Event("c"));
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
        clock.setNow(110);
        assertEquals(Throttler.ThrottleResult.DO_NOT_PROCEED, throttler.shouldProceed());
        clock.setNow(111);
        assertEquals(Throttler.ThrottleResult.PROCEED, throttler.shouldProceed());
        List<Event> events;
        events = throttler.getEvents();
        assertEquals("a", events.get(0).getId());
        assertEquals("b", events.get(1).getId());
        assertEquals("c", events.get(2).getId());
    }

    @Test
    public void initialEventPush() throws InterruptedException {
        final List<Event> events = new ArrayList<>();
        throttler.notifyWhenCanProceed(evts -> {
            events.clear();
            events.addAll(evts);
        });
        clock.setNow(100);
        throttler.submitEvent(new Event("a"));
        //sleep is not ideal!!
        sleep(10);
        assertEquals("a", events.get(0).getId());
        events.clear();
        clock.setNow(102);
        throttler.submitEvent(new Event("b"));
        sleep(10);
        assertTrue(events.isEmpty());
    }

    @Test
    public void unthrottlePush() throws InterruptedException {
        final List<Event> events = new ArrayList<>();
        throttler.notifyWhenCanProceed(evts -> {
            events.clear();
            events.addAll(evts);
        });
        clock.setNow(100);
        throttler.getEvents();
        clock.setNow(101);
        throttler.submitEvent(new Event("a"));
        sleep(10);
        assertTrue(events.isEmpty());
        clock.setNow(103);
        throttler.submitEvent(new Event("b"));
        sleep(10);
        assertTrue(events.isEmpty());
        clock.setNow(104);
        throttler.submitEvent(new Event("c"));
        sleep(10);
        assertTrue(events.isEmpty());
        clock.setNow(110);
        throttler.submitEvent(new Event("d"));
        sleep(10);
        assertTrue(events.isEmpty());
        clock.setNow(111);
        throttler.submitEvent(new Event("e"));
        sleep(10);
        assertFalse(events.isEmpty());
        assertEquals("a", events.get(0).getId());
        assertEquals("b", events.get(1).getId());
        assertEquals("c", events.get(2).getId());
        assertEquals("d", events.get(3).getId());
        assertEquals("e", events.get(4).getId());

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
