package hsbctest.question_one;

import java.util.ArrayList;
import java.util.List;

public class ProbabilisticRandomGenImpl implements ProbabilisticRandomGen {

    private final List<Pair<Float, NumAndProbability>> sample = new ArrayList<>();
    private final float totalProbability;
    private final RandomNumberGenerator generator;


    public ProbabilisticRandomGenImpl(List<NumAndProbability> sample) {
        this(sample, new RandomNumberGeneratorImpl());
    }

    /**
     * Given the requirements so far I see no compelling reason to use factory methods yet.  Maybe if there are
     * patterns that can be identified regarding how the provided sample lists are populated we could introduce
     * descriptive factory methods but that is not yet the case.
     *
     * @param sample
     */
    protected ProbabilisticRandomGenImpl(List<NumAndProbability> sample, RandomNumberGenerator generator) {
        if(sample.size()==0) {
            throw new RuntimeException("Empty sample provided.");
        }
        this.generator = generator;
        //I'm not using a stream here as I need to accumulate the probablility whilst retaining individual entries
        float accumulatedProbability = 0f;
        for(int i=0;i<sample.size();i++) {
            accumulatedProbability += sample.get(i).getProbabilityOfSample();
            this.sample.add(Pair.of(accumulatedProbability, sample.get(i)));
        }
        totalProbability = this.sample.get(this.sample.size()-1).getLeft();
    }

    @Override
    public int nextFromSample() {
        float randomNumber = generator.generate(0, totalProbability);
        if(randomNumber <0 || randomNumber>totalProbability) {
            throw new RuntimeException(String.format("Out of range(%s): %s - %s", randomNumber, 0, totalProbability));
        }
        return find(randomNumber, 0, sample.size()-1);
    }

    private int find(float value, int start, int end) {
        if(start==end) return sample.get(start).getRight().getNumber();
        int mid = start + (end-start)/2;
        return sample.get(mid).getLeft()>value ?
                find(value, start, mid):
                find(value, mid+1, end);
    }
}
