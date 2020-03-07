package simulator.cs.anycast.events;
import simulator.cs.anycast.components.Connection;

/**
 *
 * Class that models events to be executed by the simulation loop. A particular
 * active process (event) is associated and will be executed when the simulation
 * clock reaches the particular event time.
 * 
 * @author carlosnatalino
 */
public class Event<E> implements Comparable<Event<Connection>>{
    
    private double time;
    private E context;
    private int eventType;
    private ActiveProcess ap;

    public Event(double time, ActiveProcess ap, int eventType) {
	this.time = time;
	this.ap = ap;
	this.eventType = eventType;
    }

    public Event(double time, E context, int eventType, ActiveProcess ap) {
	this.time = time;
	this.context = context;
	this.eventType = eventType;
	this.ap = ap;
    }

    public double getTime() {
	return time;
    }

    public void setTime(double time) {
	this.time = time;
    }

    public E getContext() {
	return context;
    }

    public void setContext(E context) {
	this.context = context;
    }

    public int getEventType() {
	return eventType;
    }

    public void setEventType(int eventType) {
	this.eventType = eventType;
    }

    public ActiveProcess getAp() {
	return ap;
    }

    /**
     * Implementation of the method that allows the ordering of the priority
     * queue. We use the event time to perform the comparison.
     * 
     * @param o the other event to be compared to this one
     * @return 
     */
    @Override
    public int compareTo(Event<Connection> o) {
        if (time < o.getTime()) // if this event happens before the other
            return -1;
        else
            return 1;
    }
    
}
