package simulator.cs.anycast.components;

import simulator.cs.anycast.core.Configuration;

/**
 *
 * Class that models a connection request in the network.
 * 
 * @author carlosnatalino
 */
public class Connection extends AbstractComponent {
    
    private OpticalRoute route;
    private double holdingTime;
    private double arrivalTime;
    private boolean accepted = false;
    private int requiredPUs = 0, requiredSUs = 0, requiredLPs = 1;
    private int source, destination = -1;
    private int lightpaths = 0;
    private boolean blockedByNetwork = false, blockedByIT = false;
//    private int primaryCpu = -1, backupCpu = -1;
//    private int wavelength;
    
    public Connection(int id, int source, double holdingTime, Configuration configuration) {
	this.id = id;
        this.source = source;
	this.holdingTime = holdingTime;
	arrivalTime = configuration.getSimulator().getCurrentTime();
    }
    
    public double getRemainingTime(Configuration configuration) {
        return (getArrivalTime() + getHoldingTime()) - configuration.getSimulator().getCurrentTime();
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public int getHopCount() {
        if (accepted && route == null) {
            System.err.println("/n/nerror\n\n");
        }
        return route != null ? route.getHopCount() : 0;
    }
    
    public double getRouteWeight() {
        return route != null ? route.getWeight(): 0;
    }
    
    public double getHoldingTime() {
	return holdingTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public boolean isAccepted() {
	return accepted;
    }

    public void setAccepted(boolean accepted) {
	this.accepted = accepted;
    }

    public int getRequiredPUs() {
        return requiredPUs;
    }

    public void setRequiredPUs(int requiredPUs) {
        this.requiredPUs = requiredPUs;
    }

    public int getRequiredSUs() {
        return requiredSUs;
    }

    public void setRequiredSUs(int requiredSUs) {
        this.requiredSUs = requiredSUs;
    }

    public int getRequiredLPs() {
        return requiredLPs;
    }

    public void setRequiredLPs(int requiredLPs) {
        this.requiredLPs = requiredLPs;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public OpticalRoute getRoute() {
        return route;
    }

    public void setRoute(OpticalRoute primaryRoute) {
        this.route = primaryRoute;
    }

    public boolean isBlockedByNetwork() {
        return blockedByNetwork;
    }

    public void setBlockedByNetwork(boolean blockedByNetwork) {
        this.blockedByNetwork = blockedByNetwork;
    }

    public boolean isBlockedByIT() {
        return blockedByIT;
    }

    public void setBlockedByIT(boolean blockedByIT) {
        this.blockedByIT = blockedByIT;
    }

    public int getLightpaths() {
        return lightpaths;
    }

    public void setLightpaths(int lightpaths) {
        this.lightpaths = lightpaths;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "Connection: {id: " + getId() + " ; " + source + " -> " + destination + "}";
    }

}
