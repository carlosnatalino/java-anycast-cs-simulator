package simulator.cs.anycast.components;

import java.util.ArrayList;
import simulator.cs.anycast.core.Configuration;

/**
 *
 * Class that models the nodes in the network. Nodes can be datacenters in the
 * anycast paradigm considered in this simulation, and therefore have
 * storage and processing resources. Similar to the links, resources here
 * are discrete.
 * 
 * @author carlosnatalino
 */
public class Node extends AbstractComponent {

    private boolean datacenter = false;
    private int PUs, usedPUs;
    private int SUs, usedSUs;
    private ArrayList<Connection> connectionsOnThisDC;

    private double processingUtilization = 0.0, storageUtilization = 0.0, 
            curProcessingUtilization = 0.0,
            curStorageUtilization = 0.0;

    private ArrayList<Link> links = new ArrayList<>();
    
    public Node(int id, Configuration configuration) {
        this.id = id;
        this.configuration = configuration;
        reset();
    }

    public void addConnection(Connection connection) {
        if (!connectionsOnThisDC.contains(connection) && connection.getRequiredPUs() <= getFreePUs()
                && connection.getRequiredSUs() <= getFreeSUs()) {

            updateUtilization();

            connectionsOnThisDC.add(connection);

            usedPUs += connection.getRequiredPUs();
            usedSUs += connection.getRequiredSUs();

            // configuration.println("Connection " + connection.getId() + " is
            // fully associated to primary DC " + id);
        } else {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Connection " + connection.getId() + " is already primary fully assigned to DC " + id);
        }
    }

    public void removeConnection(Connection connection) {
        if (connectionsOnThisDC.contains(connection)) {

            updateUtilization();

            connectionsOnThisDC.remove(connection);
            // configuration.println("Connection " + connection.getId() + " is
            // fully unassociated to primary DC " + id);

            usedPUs -= connection.getRequiredPUs();
            usedSUs -= connection.getRequiredSUs();
        } else {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Connection " + connection.getId() + " is not fully assigned to DC " + id);
        }
    }

    public boolean isAssociated(Connection connection) {
        return connectionsOnThisDC.contains(connection);
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public boolean isDatacenter() {
        return datacenter;
    }

    public void setDatacenter(boolean datacenter) {
        this.datacenter = datacenter;
    }

    public int getPUs() {
        return PUs;
    }

    public void setPUs(int PUs) {
        this.PUs = PUs;
    }

    public int getFreePUs() {
        return PUs - usedPUs;
    }

    public int getUsedPUs() {
        return usedPUs;
    }

    public int getSUs() {
        return SUs;
    }

    public void setSUs(int SUs) {
        this.SUs = SUs;
    }

    public int getFreeSUs() {
        return SUs - usedSUs;
    }

    public int getUsedSUs() {
        return usedSUs;
    }

    public double getStorageUtilization() {
        return storageUtilization;
    }

    public double getProcessingUtilization() {
        return processingUtilization;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }
    
    public double getLoad() {
        return ((double) usedPUs / (double) PUs) * ((double) usedSUs / (double) SUs);
    }
    // </editor-fold>

    private void updateUtilization() {
        double currentTime = configuration.getSimulator().getCurrentTime();
        double timeDiff = currentTime - lastUpdateTime;

        // usedSUs = primaryStorageConnectionsOnThisDC.stream()
        // .mapToInt(Connection::getRequiredSUs)
        // .sum();
        // usedSUs += backupStorageConnectionsOnThisDC.stream()
        // .mapToInt(Connection::getRequiredSUs)
        // .sum();
        // usedPUs = primaryProcessingConnectionsOnThisDC.stream()
        // .mapToInt(Connection::getRequiredPUs)
        // .sum();
        // usedPUs += backupProcessingConnectionsOnThisDC.stream()
        // .mapToInt(Connection::getRequiredPUs)
        // .sum();
        if (currentTime > 0) {
            curProcessingUtilization = ((double) usedPUs / (double) PUs);
            curStorageUtilization = ((double) usedSUs / (double) SUs);
            processingUtilization = ((processingUtilization * lastUpdateTime) + (curProcessingUtilization * timeDiff)) / currentTime;
            storageUtilization = ((storageUtilization * lastUpdateTime) + (curStorageUtilization * timeDiff)) / currentTime;

        }
//        System.out.println("Used PU's in node " + id + ": " + usedPUs);
//        System.out.println("UTILIZATION Node " + id + "\t"
//                + processingUtilization + "\t" + storageUtilization + "\t"
//                + curProcessingUtilization + "\t" + curStorageUtilization);
        if (getFreePUs() < 0 || getFreeSUs() < 0) {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Node " + id + " has strange usage");
        }

        lastUpdateTime = currentTime;
    }

    public double getCurrentProcessingUtilization() {
        return (double) usedPUs / (double) PUs;
    }

    public double getCurrentStorageUtilization() {
        return (double) usedSUs / (double) SUs;
    }

    @Override
    public void reset() {
        super.reset();
        processingUtilization = 0.0;
        storageUtilization = 0.0;
        connectionsOnThisDC = new ArrayList<>();
        
        usedPUs = 0;
        usedSUs = 0;
    }

    @Override
    public String toString() {
        return "Node " + id + "(free pu's " + getFreePUs() + ", free SU's " + getFreeSUs()+ ")"; // To change body of generated methods, choose
        // Tools | Templates.
    }
    
}
