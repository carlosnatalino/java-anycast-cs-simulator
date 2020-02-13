package simulator.cs.anycast.components;

import simulator.cs.anycast.core.Configuration;

/**
 *
 * @author carlosnatalino
 */
public abstract class AbstractComponent {
    
    protected int id = -1;
    protected Configuration configuration;
    protected double lastUpdateTime = 0;
    protected double utilization = 0.0;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public Configuration getConfiguration() {
	return configuration;
    }

    public void setConfiguration(Configuration configuration) {
	this.configuration = configuration;
    }

    public double getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(double lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public void reset() {
        lastUpdateTime = utilization = 0;
    }
    
}
