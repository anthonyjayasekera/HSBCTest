package hsbctest.question_two.single_threaded;

import hsbctest.Event;
import org.junit.Test;
import hsbctest.question_two.EventBus;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class TestSingleThreadedEventBus {


    @Test
    public void subscribe() {
        EventBus<Event> bus = new SingleThreadedEventBus();
        AtomicReference<Event> eventHolder1 = new AtomicReference<>();
        AtomicReference<Event> eventHolder2 = new AtomicReference<>();
        AtomicReference<Event> eventHolder3 = new AtomicReference<>();

        bus.publishEvent(new Event("first"));

        bus.addSubscriber(eventHolder1::set);
        assertNull(eventHolder1.get());
        bus.publishEvent(new Event("second"));
        assertNotNull(eventHolder1.get());
        assertEquals("second", eventHolder1.get().getId());

        bus.addSubscriber(eventHolder2::set);
        assertNull(eventHolder2.get());
        bus.publishEvent(new Event("third"));
        assertNotNull(eventHolder1.get());
        assertNotNull(eventHolder2.get());
        assertEquals("third", eventHolder1.get().getId());
        assertEquals("third", eventHolder2.get().getId());
    }

    @Test
    public void filteredSubscribe() {
        EventBus<Event> bus = new SingleThreadedEventBus();
        AtomicReference<Event> eventHolder1 = new AtomicReference<>();
        AtomicReference<Event> eventHolder2 = new AtomicReference<>();

        bus.addSubscriberForFilteredEvents(eventHolder1::set, event -> event.getId().contains("th"));
        bus.addSubscriberForFilteredEvents(eventHolder2::set, event -> event.getId().contains("ird"));
        assertNull(eventHolder1.get());
        assertNull(eventHolder2.get());

        bus.publishEvent(new Event("that"));
        assertNotNull(eventHolder1.get());
        assertNull(eventHolder2.get());
        assertEquals("that", eventHolder1.get().getId());

        eventHolder1.set(null);
        eventHolder2.set(null);

        bus.publishEvent(new Event("bird"));
        assertNull(eventHolder1.get());
        assertNotNull(eventHolder2.get());
        assertEquals("bird", eventHolder2.get().getId());

        eventHolder1.set(null);
        eventHolder2.set(null);

        bus.publishEvent(new Event("third"));
        assertNotNull(eventHolder1.get());
        assertNotNull(eventHolder2.get());
        assertEquals("third", eventHolder1.get().getId());
        assertEquals("third", eventHolder2.get().getId());

        eventHolder1.set(null);
        eventHolder2.set(null);

        bus.publishEvent(new Event("boing"));
        assertNull(eventHolder1.get());
        assertNull(eventHolder2.get());

    }
}
