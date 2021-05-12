package hsbctest.question_two;

import hsbctest.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventBus<T extends Event> {

    void publishEvent(T event);

    void addSubscriber(Consumer<T> subscriber);

    void addSubscriberForFilteredEvents(Consumer<T> subscriber, Predicate<T> filter);


}
