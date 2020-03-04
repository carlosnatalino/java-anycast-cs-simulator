package simulator.cs.anycast.events;

import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.core.Simulator;
import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 * Class defines the methods any class that want to process events
 * need to extend.
 * @author carlosnatalino
 */
public abstract class ActiveProcess {
    
    private double time;
    protected Configuration configuration;

    public ActiveProcess() {
        configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();
    }
    
    /**
     * Method called by the simulator event processor when processing this event.
     * @param event is the event object for the current event
     */
    public abstract void activity(Event<Connection> event);

    public double getTime() {
	return time;
    }

    public void setTime(double time) {
	this.time = time;
    }

    public void setConfiguration(Configuration configuration) {
	this.configuration = configuration;
    }
    
    public Simulator getSimulator() {
	return configuration.getSimulator();
    }
    
}
