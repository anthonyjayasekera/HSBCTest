package hsbctest.question_two.multi_threaded;

import org.junit.Test;
import hsbctest.Event;
import hsbctest.question_two.EventBus;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class TestMultiThreadedEventBus {

    @Test
    public void subscribe() throws InterruptedException {

        ArrayBlockingQueue<SortableEvent> allEvents = new ArrayBlockingQueue<>(2000);
        ArrayBlockingQueue<SortableEvent> filteredEvents = new ArrayBlockingQueue<>(2000);

        EventBus<SortableEvent> bus = new MultiThreadedEventBus(5);

        bus.addSubscriber(s -> {
            allEvents.add(s);
        });

        bus.addSubscriberForFilteredEvents(filteredEvents::add, event -> event.getId().startsWith("X"));

        List<SortableEvent> events = IntStream.range(0, 1000).mapToObj(Integer::toString).map(SortableEvent::new).collect(toList());
        List<SortableEvent> xxxEvents = IntStream.range(0, 1000).mapToObj(Integer::toString).map(s -> "XXX" + s).map(SortableEvent::new).collect(toList());
        events.addAll(xxxEvents);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        events.stream().forEach(event -> executor.submit(() -> {
            try {
                bus.publishEvent(event);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        }));

        while(allEvents.size()<events.size()) {
            synchronized(this) {
                wait(100);
            }
        }

        List<SortableEvent> allEventsAsList = new ArrayList<>(allEvents);
        events = new ArrayList<>(events);
        allEventsAsList.sort(Comparator.comparing(Event::getId));
        events.sort(Comparator.comparing(Event::getId));
        assertEquals("", events, allEventsAsList);

        List<SortableEvent> filteredEventsAsList = new ArrayList<>(filteredEvents);
        xxxEvents = new ArrayList<>(xxxEvents);
        filteredEventsAsList.sort(Comparator.comparing(Event::getId));
        xxxEvents.sort(Comparator.comparing(Event::getId));
        assertEquals("", xxxEvents, filteredEventsAsList);

    }


    private class SortableEvent extends Event implements Comparable<SortableEvent> {

        public SortableEvent(String id) {
            super(id);
        }

        @Override
        public int compareTo(SortableEvent o) {
            return getId().compareTo(o.getId());
        }

    }
}
