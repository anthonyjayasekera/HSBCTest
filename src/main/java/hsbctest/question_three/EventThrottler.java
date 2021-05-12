package hsbctest.question_three;

import hsbctest.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Notes: I'm not keen on the Throttler interface design.  Throttling logic requires knowledge of the internal state of
 * the class receiving events. As such I think throttling should be implemented as a configurable feature of this class
 * rather than  service provided to a caller who acts upon the throttling response/notification.  If the downstream event
 * processing is delegated to the caller/subscriber of/to the throttling code, events must be made available to it as well.
 * In order to avoid duplicate event processing events needs to be drained as they are provided to the caller/subscriber.
 * As such the pull and push usage patterns of this class cannot really be used together leading to a messy api.
 *
 * My preferred api would be to implement a generic interface modelling event processing e.g. Executor but to allow
 * throttling parameters to be configured, and use like a regular dispatcher. Internally an onward implementation of the same
 * interface would only be invoked if throttling conditions are not met.
 *
 * "Do not assume thread safety in the subscriber." I don't quite understand this comment. The subscriber is always invoked
 * in the same thread as the event submitter so I'm not sure what thread safety concerns there are other than the possiblity
 * of multiple threads submitting events at the same time. If that is the use case then I think I have handled it.
 */
public class EventThrottler implements Throttler{

    List<Consumer<List<Event>>> interestedParties = new CopyOnWriteArrayList<Consumer<List<Event>>>();

    private BlockingQueue<Event> pendingEvents = new LinkedBlockingQueue<>();

    private final Executor dispatcher;

    private final long minLatency;
    private final Clock clock;
    private volatile long lastReceived;

    public EventThrottler(long minLatency, Clock clock) {
        dispatcher = Executors.newSingleThreadExecutor();
        this.minLatency = minLatency;
        this.clock = clock;
        lastReceived = 0l;
    }

    public List<Event> getEvents() {
        return drainEvents();
    }

    @Override
    public ThrottleResult shouldProceed() {
        System.out.println(clock.now() + ", last:" + lastReceived + ", min:" +minLatency + ", diff=" + (clock.now() - lastReceived) + ", proceed=" + ((clock.now() - lastReceived) > minLatency));
        return ((clock.now() - lastReceived) > minLatency) ? ThrottleResult.PROCEED : ThrottleResult.DO_NOT_PROCEED;
    }

    @Override
    public void notifyWhenCanProceed(Consumer<List<Event>> proceed) {
        interestedParties.add(proceed);
    }

    public void submitEvent(Event event) {
        try {
            pendingEvents.put(event);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Submit thread interrupted.");
        }
        checkCanProceed();
    }

    private void checkCanProceed() {
        dispatcher.execute(() -> {
            try{
                long currentTime = clock.now();
                if(shouldProceed() == ThrottleResult.PROCEED) {
                    notifyCanProceed();
                }
                lastReceived = currentTime;
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void notifyCanProceed() {
        List<Event> events = drainEvents();
        interestedParties.stream().forEach(interestedParty -> interestedParty.accept(events));
    }

    private List<Event> drainEvents() {
        List<Event> readyEvents = new ArrayList<>();
        pendingEvents.drainTo(readyEvents);
        return readyEvents;
    }

}
