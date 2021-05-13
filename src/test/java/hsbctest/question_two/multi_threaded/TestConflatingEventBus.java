package hsbctest.question_two.multi_threaded;

import hsbctest.Event;
import hsbctest.question_one.Pair;
import hsbctest.question_two.EventBus;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO This test is currently failing.
 */
public class TestConflatingEventBus {

//    @Test
    public void subscribe() throws InterruptedException {

        Map<String, TimestampedEvent> receivedEvents = new HashMap<>();

        EventBus<TimestampedEvent> bus = new ConflatingEventBus<>(5);

        AtomicInteger eventsReceived = new AtomicInteger(0);
        //create 20 randomly timestamped events for 50 distinct ids
        List<TimestampedEvent> events = LongStream.range(0,2).mapToObj(Long::valueOf).flatMap(ts -> IntStream.range(0,5).mapToObj(Integer::toString).map(id -> new TimestampedEvent(id, ts))).collect(toList());
        Map<String, Long> latestTimestampForEvent = new HashMap<>();
        events.stream().forEach(event -> {
            eventsReceived.incrementAndGet();
            Long latest = latestTimestampForEvent.get(event.getId());
            if(latest == null || latest < event.getTimestamp()) {
                latestTimestampForEvent.put(event.getId(), event.getTimestamp());
            }
        });

        Map<String, Long> lastTimestampForEvent = latestTimestampForEvent.keySet().stream().collect(toMap( id -> id, id -> 0l));

        bus.addSubscriber(event -> {
            Long latest = lastTimestampForEvent.get(event.getId());
            assertTrue(latest == null || latest < event.getTimestamp());
            lastTimestampForEvent.put(event.getId(), event.getTimestamp());
            receivedEvents.put(event.getId(), event);
        });



        ExecutorService executor = Executors.newFixedThreadPool(5);
        events.stream().forEach(event -> executor.submit(() -> {
            try {
                bus.publishEvent(event);
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        }));

        while(eventsReceived.get()<events.size()) {
            synchronized(this) {
                wait(100);
            }
        }

        Map<String, Long> receivedEventTimestamps = receivedEvents.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().getTimestamp())).collect(toMap(Pair::getLeft, Pair::getRight));
        assertEquals("", latestTimestampForEvent, receivedEventTimestamps);


    }
}
