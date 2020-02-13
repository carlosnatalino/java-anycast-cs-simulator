package simulator.cs.anycast.components;

import java.util.ArrayList;
import simulator.cs.anycast.core.Configuration;

/**
 *
 * @author carlosnatalino
 */
public class Link extends AbstractComponent {

    /**
     * TODO
     * 
     * implement the assignment to a given wavelength
     * it will also allow for the sharing of wavelengths for backup purposes
     */
    
    private int source;
    private int destination;
    private Node sourceNode, destinationNode;
    private int length = 1086;
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
            /**
             * TODO
             * manage wavelengths
             */
            updateUtilization();
//            getConfiguration().println("Link " + source + " -> " + destination + " (" + free + ") | add " + connection.getId());
        } else {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Connection " + connection.getId() + " tried to connect to a full link " + this);
        }
    }
    
    public void removeConnection(Connection connection) {
//        getConfiguration().println("Link " + source + " -> " + destination + " (" + free + ") | remove " + connection.getId());
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

    public int getLength() {
	return length;
    }

    public void setLength(int length) {
	this.length = length;
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
    // </editor-fold>

    private void updateUtilization() {
        double currentTime = configuration.getSimulator().getCurrentTime();
        double timeDiff = currentTime - lastUpdateTime;
        
        if (currentTime > 0) {
            utilization = ((utilization * lastUpdateTime) + ((((double) configuration.getWavelengthsPerFiber() - (double) free) / (double) configuration.getWavelengthsPerFiber()) * timeDiff)) / currentTime;
//            if (connections.size() + backupConnections.size() > 0) { // to avoid division by zero
//                double currentTotal = 1 - (((double) configuration.getWavelengthsPerFiber() - (double) free) / (double)(connections.size() + backupConnections.size()));
//                totalSharingDegree = ((totalSharingDegree * lastUpdateTime) + (currentTotal * timeDiff)) / currentTime;
//            }
//            if (backupConnections.size() > 0) {
//                double currentDegree = ((double) backupConnections.size() / (double) totalBackupWavelengths());
//                backupSharingDegree = ((backupSharingDegree * lastUpdateTime) + (currentDegree * timeDiff)) / currentTime; 
//            }
            
//            if (id == 5) configuration.println(Double.toString(getBackupSharingDegree()));
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
