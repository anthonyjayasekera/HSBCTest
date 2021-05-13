package hsbctest.question_four;

import hsbctest.ConditionalConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * "The add method may be called by a different thread than the callback should be made on."
 * This is determined by the implementation, and in the implementation below this is not true. However add() may be called
 * by multiple threads at the same time. We can use the dispatcher approach used in the answer to question three to ensure
 * that in this case notification calls occur serially.
 */
public class SlidingWindowStatisticsImpl implements SlidingWindowStatistics {


    private BlockingQueue<Integer> data = new LinkedBlockingQueue<>();
    private List<Integer> allData = new ArrayList<>();

    private List<Consumer<Statistics>> statsSubscribers = new CopyOnWriteArrayList<>();

    private Thread dispatcher;

    public SlidingWindowStatisticsImpl() {
        dispatcher = new Thread(() -> {
            try {
                while(true) {
                    allData.add(data.take());
                    notifyStatistics();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void start() {
        dispatcher.start();
    }

    public void stop() {
        dispatcher.interrupt();
    }

    @Override
    public void add(int measurement) {
        data.add(measurement);
    }

    @Override
    public void subscribeForStatistics(Consumer<Statistics> consumer) {
        statsSubscribers.add(consumer);
    }

    @Override
    public void subscribeForStatistics(Consumer<Statistics> consumer, Predicate<Statistics> filter) {
        subscribeForStatistics(new ConditionalConsumer<>(consumer, filter));
    }

    @Override
    public Statistics getLatestStatistics() {

        //reduction sums all integers. Then we cast to double and divide by the number of values to get the mean
        final double mean = allData.stream().reduce((i1,i2) -> i1 + i2).map(d -> ((double)d)/allData.size()).orElse(0d);

        //Aggregate values into a histogram
        final Map<Integer, Integer> histogram = new HashMap<>();
        allData.stream().forEach(value -> {
            int frequency = histogram.computeIfAbsent(value, it -> 0);
            histogram.put(value,++frequency);
        });

        final int count = allData.size();

        final List<Integer> distinctValues = histogram.keySet().stream().sorted().collect(Collectors.toList());

        final int mode = histogram.entrySet().stream().reduce((e1,e2)-> e1.getValue() > e2.getValue()?e1:e2).map(Map.Entry::getKey).orElse(0);

        return new Statistics() {
            @Override
            public int count() {
                return count;
            }

            @Override
            public double getMean() {
                return mean;
            }

            @Override
            public int getMode() {
                return mode;
            }

            /**
             * The api specified in the question requires that the caller can query for any percentile
             * @param pctile
             * @return
             */
            @Override
            public int getPctile(int pctile) {
                if(pctile < 0 || pctile > 99) {
                    throw new RuntimeException("Out of range");
                }
                //the int cast rounds down so we add 1 to the result to give us the position of the first occurance above the stated percentile
                int index = (int)(count * (((double)pctile)/100d))+1;
                //iterate through the histogram in value order to find the distinct value matching the position we are looking for
                for(int dv = 0; dv < distinctValues.size() ; dv++) {
                    int distinctValue = distinctValues.get(dv);
                    int frequency = histogram.get(distinctValue);
                    index -= frequency;
                    if(index <= 0) {
                        return distinctValue;
                    }
                }
                throw new RuntimeException("Unable to evaluate percentile");
            }
        };
    }

    private void notifyStatistics() {

            Statistics latestStatistics = getLatestStatistics();
            statsSubscribers.stream().forEach(statsSubscribers -> statsSubscribers.accept(latestStatistics));
    }
}
