package hsbctest.question_two.multi_threaded;

import hsbctest.ConditionalConsumer;
import hsbctest.question_two.EventBus;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This implementation will provide new subscribers with a State of the world on initial subscription i.e. will notify
 * new subscribers of the latest versions of all events. In my experience apart from performance concerns this is a
 * key use case for conflation.
 * @param <T>
 */
public class ConflatingSOTWEventBus<T extends TimestampedEvent> implements EventBus<T> {


    private final Map<String, BlockingQueue<T>> queuedEventsById;
    private final Map<String, T> latestEventById;
    private final BlockingDeque<Notification<T>> queuedEventNotifications;
    private final ExecutorService executor;

    public ConflatingSOTWEventBus(int threads) {
        this.queuedEventNotifications = new LinkedBlockingDeque<>();
        this.queuedEventsById = new HashMap<>();
        this.latestEventById = new HashMap<>();
        executor = Executors.newFixedThreadPool(threads);
        new Thread(() -> {
            try {
                while(true) {
                    Notification<T> notification = queuedEventNotifications.take();
                    switch (notification.getType()) {
                        case NEW_EVENT:
                            getLatestEventForId(notification.getEventId()).ifPresent(event -> executor.submit(new processEvent(event)));
                            break;
                        case NEW_SUBSCRIBER:
                            latestEventById.values().stream().forEach(notification.getSubscriber()::accept);
                            break;
                    }
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
        Optional<T> latestEvent = events.stream().reduce((e1,e2) -> e1.getTimestamp()>e2.getTimestamp()?e1:e2);
        latestEvent.ifPresent(event -> latestEventById.put(eventId, event));
        return latestEvent;
    }

    private final List<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void publishEvent(T event) {
        try {
            queuedEventsById.computeIfAbsent(event.getId(), id -> new LinkedBlockingQueue<>()).put(event);
            queuedEventNotifications.put(new Notification<>(event.getId()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Publisher thread interrupted.");
        }
    }

    @Override
    public void addSubscriber(Consumer<T> subscriber) {
        try {
            queuedEventNotifications.putFirst(new Notification<>(subscriber));
            subscribers.add(subscriber);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Add subscriber thread interrupted.");
        }
    }

    @Override
    public void addSubscriberForFilteredEvents(Consumer<T> subscriber, Predicate<T> filter) {
        try {
            queuedEventNotifications.put(new Notification<>(subscriber));
            subscribers.add(new ConditionalConsumer<>(subscriber, filter));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Add subscriber thread interrupted.");
        }
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
