package hsbctest.question_four;

import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class TestSlidingWindowStatisticsImpl {


    @Test
    public void mean() throws InterruptedException {
        int count = 10000;
        SlidingWindowStatisticsImpl stats = new SlidingWindowStatisticsImpl();
        stats.start();

        Executor multiThreaded = Executors.newFixedThreadPool(5);

        LinkedBlockingQueue<Data> dataPoints = new LinkedBlockingQueue<>();
        stats.subscribeForStatistics(it -> {
            try {
                dataPoints.put(new Data(it.count(), it.getMode(), it.getMean()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        int sum = 0;
        for( int i=0;i<count;i++) {
            int nextValue = random(0, 1000);
            sum += nextValue;
            multiThreaded.execute(() -> stats.add(nextValue));
        }

        while(dataPoints.size()<count) {
            try {
                synchronized (this) {
                    wait(100);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail();
            }
        }

        stats.stop();

        Data lastDataPoint = null;
        for(int i=0;i<count;i++) {
            Data dataPoint = dataPoints.take();
//            assertEquals(i+1, dataPoint.count);
            if(i+1==count) {
                lastDataPoint = dataPoint;
            }
        }
        double expectedMean = (double)sum / count;
        assertEquals(expectedMean, lastDataPoint.mean, 0.001);
    }

    private int random(int min, int max) {
        double diff = (double)(max - min);
        return (int)(diff * Math.random());
    }

    private static class Data {
        private int count;
        private int mode;
        private double mean;

        public Data(int count, int mode, double mean) {
            this.count = count;
            this.mode = mode;
            this.mean = mean;
        }
    }
}
