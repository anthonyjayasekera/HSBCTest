package hsbctest.question_two.single_threaded;

import hsbctest.Event;
import hsbctest.ConditionalConsumer;
import hsbctest.question_two.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 1. Only single threaded use is expected, therefore publishEvent is never executed concurrently with addSubscriber
 * or addSubscriberForFilteredEvents, therefore the current consumer list does not change during the execution of a
 * call to publishEvent.
 * 2. There is no stated requirement to remove subscribers.  Therefore thee list only grows.
 * 3. No stated requirement for new subscribers to be initiailised with "state of the world".
 *
 * Given the above, a publishEvent execution can safely push its event to all current subscribers, and the addition of
 * a nerw subscriber does not need to trigger any event processing.
 * @param <T>
 */
public class SingleThreadedEventBus<T extends Event> implements EventBus<T> {

    private final List<Consumer<T>> subscribers = new ArrayList<>();

    @Override
    public void publishEvent(T event) {
        subscribers.stream().forEach(subscriber -> subscriber.accept(event));
    }

    @Override
    public void addSubscriber(Consumer<T> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void addSubscriberForFilteredEvents(Consumer<T> subscriber, Predicate<T> filter) {
        subscribers.add(new ConditionalConsumer<>(subscriber, filter));
    }

}
