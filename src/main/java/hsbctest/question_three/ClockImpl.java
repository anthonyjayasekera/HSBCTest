package hsbctest.question_three;

public class ClockImpl implements Clock{
    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
