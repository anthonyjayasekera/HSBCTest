package hsbctest.question_two.multi_threaded;

import hsbctest.ConditionalConsumer;
import hsbctest.question_two.EventBus;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConflatingEventBus<T extends TimestampedEvent> implements EventBus<T> {


    private final Map<String, BlockingQueue<T>> queuedEventsById;
    private final BlockingQueue<String> queuedEventNotifications;
    private final ExecutorService executor;

    public ConflatingEventBus(int threads) {
        this.queuedEventNotifications = new LinkedBlockingQueue<>();
        this.queuedEventsById = new ConcurrentHashMap<>();
        executor = Executors.newFixedThreadPool(threads);
        new Thread(() -> {
            try {
                while(true) {
                    String eventId = queuedEventNotifications.take();
                    getLatestEventForId(eventId).ifPresent(event -> executor.submit(new processEvent(event)));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Dispatcher thread interrupted.");
            }
        }).start();
    }

    private Optional<T> getLatestEventForId(String eventId) {
        Collection<T> events = new HashSet<>();
        queuedEventsById.computeIfAbsent(eventId, id -> new LinkedBlockingQueue<>()).drainTo(events);
        return events.stream().reduce((e1,e2) -> e1.getTimestamp()>e2.getTimestamp()?e1:e2);
    }

    private final List<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void publishEvent(T event) {
        try {
            queuedEventsById.computeIfAbsent(event.getId(), id -> new LinkedBlockingQueue<>()).put(event);
            queuedEventNotifications.put(event.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Publisher thread interrupted.");
        }
    }

    @Override
    public void addSubscriber(Consumer<T> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void addSubscriberForFilteredEvents(Consumer<T> subscriber, Predicate<T> filter) {
        subscribers.add(new ConditionalConsumer<>(subscriber, filter));
    }

    private class processEvent implements Runnable {

        private T event;

        public processEvent(T event) {
            this.event = event;
        }

        @Override
        public void run() {
            subscribers.iterator().forEachRemaining(subscriber -> subscriber.accept(event));
        }
    }

}
