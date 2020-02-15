package simulator.cs.anycast.components;

import java.util.ArrayList;
import simulator.cs.anycast.core.Configuration;

/**
 *
 * Class that models the entire topology, composed of links and nodes. This class
 * also implements the means to compute the resources at each datacenter
 * based on their connectivity and the rho parameters from the configuration file.
 * 
 * @author carlosnatalino
 */
public class Topology {
    
    private final Link links[];
    private Boolean datacenters[];
    private final Node nodes[];
    private int nDatacenters;
    private final boolean topology[][];
    private Link[][] linksVector;
    private int type;
    private ArrayList<Link>[] zoneLinks;
    private ArrayList<Node>[] zoneNodes;
    private ArrayList<int[]> zonesNode;
    private Configuration configuration;
    
    public Topology(int nNodes, int nLinks, int nDCs, Configuration configuration) {
        this.configuration = configuration;
	nodes = new Node[nNodes];
	topology = new boolean[nNodes][nNodes];
	linksVector = new Link[nNodes][nNodes];
	links = new Link[nLinks];
	datacenters = new Boolean[nNodes];
	nDatacenters = nDCs;
	
	for (int i = 0 ; i < nNodes ; i++) {
	    nodes[i] = new Node(i, configuration);
	    datacenters[i] = false;
	}
	
	for (int i = 0 ; i < nLinks ; i++) {
	    links[i] = new Link(i, configuration);
	}
    }
    
    public void calculateDCResources() {
        int[] nodeDegree = new int[nodes.length];
        for (Link link : links) {
            nodeDegree[link.getSource()]++;
            nodeDegree[link.getDestination()]++;
        }
        
        configuration.getLogger().debug("Configuring DC IT resources for rhos (" + configuration.getRhoProcessing() + "," + configuration.getRhoStorage()+ ")");
        
        int pus = 0;
        int sus = 0;
        int avgPUs = configuration.getAveragePUValue();
        
        /**
         * TODO
         * improve this code
         */
	for (Node node : nodes) {
            if (node.isDatacenter()) {
                pus = (int) Math.ceil(nodeDegree[node.getId()] * configuration.getWavelengthsPerFiber() * configuration.getRhoProcessing() * avgPUs);
                sus = (int) Math.ceil(nodeDegree[node.getId()] * configuration.getWavelengthsPerFiber() * configuration.getRhoStorage() * Math.ceil((configuration.getMaxStorageUnits())/2));

                configuration.getLogger().debug("[" + node.getId() + "] with connectivity [" + nodeDegree[node.getId()] + "] PUs: " + pus + " and SUs: " + sus);

                node.setPUs(pus);
                node.setSUs(sus);
            }
	}
        
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Node[] getNodes() {
	return nodes;
    }

    public Link[] getLinks() {
	return links;
    }

    public Boolean[] getDatacenters() {
	return datacenters;
    }

    public void setDatacenters(Boolean[] datacenters) {
	this.datacenters = datacenters;
    }

    public int getnDatacenters() {
	return nDatacenters;
    }

    public void setnDatacenters(int nDatacenters) {
	this.nDatacenters = nDatacenters;
    }

    public Link[][] getLinksVector() {
	return linksVector;
    }

    public void setLinksVector(Link[][] linksVector) {
	this.linksVector = linksVector;
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

    public ArrayList<Link>[] getZoneLinks() {
	return zoneLinks;
    }

    public void setZoneLinks(ArrayList<Link>[] zoneLinks) {
	this.zoneLinks = zoneLinks;
    }

    public ArrayList<Node>[] getZoneNodes() {
	return zoneNodes;
    }

    public void setZoneNodes(ArrayList<Node>[] zoneNodes) {
	this.zoneNodes = zoneNodes;
    }

    public ArrayList<int[]> getZonesNode() {
	return zonesNode;
    }

    public void setZonesNode(ArrayList<int[]> zonesNode) {
	this.zonesNode = zonesNode;
    }
    // </editor-fold>
    
    public void reset() {
        for (Node node : nodes)
            node.reset();
        for (Link link : links)
            link.reset();
    }
    
}
