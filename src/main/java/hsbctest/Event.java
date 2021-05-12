package hsbctest;

public class Event {

    private String id;

    public Event(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "hsbctest.Event{" +
                "id='" + getId() + '\'' +
                '}';
    }
}
