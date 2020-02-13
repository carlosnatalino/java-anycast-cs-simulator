package simulator.cs.anycast.components;

import java.util.ArrayList;
import simulator.cs.anycast.core.Configuration;

/**
 *
 * Class that models a network link in the network, and its resources. In the 
 * case of a link in a circuit-switched network, the resources are discrete.
 * 
 * @author carlosnatalino
 */
public class Link extends AbstractComponent {
    
    private int source;
    private int destination;
    private Node sourceNode, destinationNode;
    private double weight = 1086;
    private ArrayList<Connection> connections;
    private int free;
    private boolean dcLink = false;
    private int totalWavelengths;
    
    public Link(int id, Configuration configuration) {
	this.id = id;
	this.configuration = configuration;
	this.totalWavelengths = configuration.getWavelengthsPerFiber();
        reset();
    }
    
    public void addConnection(Connection connection) {
        if (connection.getLightpaths() <= free) {
            connections.add(connection);
            free --;
            updateUtilization();
        } else {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Connection " + connection.getId() + " tried to connect to a full link " + this);
        }
    }
    
    public void removeConnection(Connection connection) {
        if (connections.contains(connection)) {
            connections.remove(connection);
            free++;
            updateUtilization();
        } else {
            configuration.println("Connection " + connection.getId() + " is not primary assigned to link " + this);
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Connection " + connection.getId() + " is not primary assigned to link " + this);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
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

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Node getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(Node destinationNode) {
        this.destinationNode = destinationNode;
    }

    public double getWeight() {
	return weight;
    }

    public void setWeight(double w) {
	this.weight = w;
    }

    public ArrayList<Connection> getConnections() {
	return connections;
    }

    public int getFreeWavelengths() {
        return free;
    }
    
    public double getUtilization() {
        return utilization;
    }

    public int getTotalWavelengths() {
	return totalWavelengths;
    }
    
    public boolean isDCLink() {
        return dcLink;
    }

    public void setDCLink(boolean dcLink) {
        this.dcLink = dcLink;
    }
    
    public double getLoad() {
        return (((double) configuration.getWavelengthsPerFiber() - (double) free) / (double) configuration.getWavelengthsPerFiber());
    }
    // </editor-fold>

    private void updateUtilization() {
        double currentTime = configuration.getSimulator().getCurrentTime();
        double timeDiff = currentTime - lastUpdateTime;
        
        if (currentTime > 0) {
            utilization = ((utilization * lastUpdateTime) + ((((double) configuration.getWavelengthsPerFiber() - (double) free) / (double) configuration.getWavelengthsPerFiber()) * timeDiff)) / currentTime;
        }
        
        lastUpdateTime = currentTime;
    }
    
    public double getCurrentUtilization() {
        return ((double) configuration.getWavelengthsPerFiber() - (double) free) / (double) configuration.getWavelengthsPerFiber();
    }
    
    public boolean isAssociated(Connection connection) {
        return connections.contains(connection);
    }
    
    @Override
    public void reset() {
        super.reset();
        free = totalWavelengths;
        connections = new ArrayList<>();
    }

    @Override
    public String toString() {
        return " Link " + id + " [" + source + " - " + destination + "]"; //To change body of generated methods, choose Tools | Templates.
    }
    
}
