package hsbctest.question_two.multi_threaded;

import java.util.function.Consumer;

public class Notification<T extends TimestampedEvent> {

    private final NotificationType type;
    private String eventId;
    private Consumer<T> subscriber;

    public Notification(String eventId) {
        type = NotificationType.NEW_EVENT;
        this.eventId = eventId;
        this.subscriber = null;
    }

    public Notification(Consumer<T> subscriber) {
        type = NotificationType.NEW_SUBSCRIBER;
        this.eventId = null;
        this.subscriber = subscriber;
    }

    public NotificationType getType() {
        return type;
    }

    public String getEventId() {
        return eventId;
    }

    public Consumer<T> getSubscriber() {
        return subscriber;
    }
}
