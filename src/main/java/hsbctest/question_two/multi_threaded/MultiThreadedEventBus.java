package hsbctest.question_two.multi_threaded;

import hsbctest.ConditionalConsumer;
import hsbctest.Event;
import hsbctest.question_two.EventBus;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MultiThreadedEventBus<T extends Event> implements EventBus<T> {


    private final BlockingQueue<T> queuedEvents;
    private final ExecutorService executor;

    public MultiThreadedEventBus(int threads) {
        this.queuedEvents = new LinkedBlockingQueue<>();
        executor = Executors.newFixedThreadPool(threads);
        new Thread(() -> {
            try {
                while(true) {
                    T event = queuedEvents.take();
                    executor.submit(new processEvent(event));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Dispatcher thread interrupted.");
            }
        }).start();
    }

    private final List<Consumer<T>> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void publishEvent(T event) {
        try {
            queuedEvents.put(event);
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
