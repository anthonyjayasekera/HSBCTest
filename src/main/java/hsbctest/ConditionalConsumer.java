package hsbctest;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ConditionalConsumer<T> implements Consumer<T> {

    private final Consumer<T> subscriber;
    private final Predicate<T> condition;
    public ConditionalConsumer(Consumer<T> subscriber, Predicate<T> condition){
        this.subscriber = subscriber;
        this.condition = condition;
    }

    @Override
    public void accept(T t) {
        if(condition.test(t)) {
            subscriber.accept(t);
        }
    }
}
