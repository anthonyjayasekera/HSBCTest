package hsbctest.question_four;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface SlidingWindowStatistics {

    void add(int measurement);

    // subscriber will have a callback that'll deliver a Statistics instance (push)
    void subscribeForStatistics(Consumer<Statistics> consumer);

    void subscribeForStatistics(Consumer<Statistics> consumer, Predicate<Statistics> filter);

    // get latest statistics (poll)
    Statistics getLatestStatistics();


    public interface Statistics {
        int count();
        double getMean();
        int getMode();
        int getPctile(int pctile);
    }
}
