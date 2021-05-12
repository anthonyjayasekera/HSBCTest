package hsbctest.question_two.multi_threaded;

import hsbctest.Event;

public class TimestampedEvent extends Event {

    private final long timestamp;
    public TimestampedEvent(String id, long timestamp) {
        super(id);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TimestampedEvent{" +
                "id='" + getId() + '\'' +
                "timestamp=" + getTimestamp() +
                '}';
    }
}
