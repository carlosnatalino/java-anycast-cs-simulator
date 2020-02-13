package simulator.cs.anycast.events;

/**
 *
 * @author carlosnatalino
 */
public class Event<E> {
    
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
    
}
