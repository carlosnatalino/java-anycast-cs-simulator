package simulator.cs.anycast.components;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.swing.mxGraphComponent;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
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
    private ArrayList<Node> datacenters;
    private final Node nodes[];
    private final boolean topology[][];
    private Link[][] linksVector;
    private HashMap<String, Node> nodeMap;
    private int type;
    private Configuration configuration;
    private DefaultUndirectedWeightedGraph<Node, Link> grapht;
    private double lowestLatitude, greatestLatitude, lowestLongitude, greatestLongitude;
    
    public Topology(int nNodes, int nLinks, Configuration configuration) {
        this.configuration = configuration;
	nodes = new Node[nNodes];
        nodeMap = new HashMap<>();
	topology = new boolean[nNodes][nNodes];
	linksVector = new Link[nNodes][nNodes];
	links = new Link[nLinks];
	datacenters = new ArrayList<Node>();
        
        lowestLatitude = lowestLongitude = Double.MAX_VALUE;
        greatestLatitude = greatestLongitude = Double.MAX_VALUE;
	
	for (int i = 0 ; i < nNodes ; i++) {
	    nodes[i] = new Node(i, configuration);
	}
	
	for (int i = 0 ; i < nLinks ; i++) {
	    links[i] = new Link(i, configuration);
	}
    }
    
    public void addNode(int i, String name, double latitude, double longitude) {
        nodes[i].setName(name);
        nodes[i].setLatitude(latitude);
        nodes[i].setLongitude(longitude);
        lowestLatitude = Math.min(lowestLatitude, latitude);
        greatestLatitude = Math.max(greatestLatitude, latitude);
        lowestLongitude = Math.min(lowestLongitude, longitude);
        greatestLongitude = Math.max(greatestLongitude, longitude);
        nodeMap.put(name, nodes[i]);
    }
    
    public void addLink(int i, String name, String source, String target, boolean compLength) {
        Node src = nodeMap.get(source);
        src.getLinks().add(links[i]);
        Node dst = nodeMap.get(target);
        dst.getLinks().add(links[i]);
        links[i].setSource(src.getId());
        links[i].setDestination(dst.getId());
        links[i].setWeight(calculateGeographicalDistance(src.getLatitude(), 
                src.getLongitude(), dst.getLatitude(), dst.getLongitude()));
        linksVector[src.getId()][dst.getId()] = links[i];
        linksVector[dst.getId()][src.getId()] = links[i];
    }
    
    public void addDC(Node node) {
        datacenters.add(node);
    }
    
    public void calculateDCResources() {
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
                pus = (int) Math.ceil(node.getDegree() * configuration.getWavelengthsPerFiber() * configuration.getRhoProcessing() * avgPUs);
                sus = (int) Math.ceil(node.getDegree() * configuration.getWavelengthsPerFiber() * configuration.getRhoStorage() * Math.ceil((configuration.getMaxStorageUnits())/2));

                configuration.getLogger().debug("[" + node.getId() + "] with connectivity [" + node.getDegree()+ "] PUs: " + pus + " and SUs: " + sus);

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

    public ArrayList<Node> getDatacenters() {
	return datacenters;
    }

    public Link[][] getLinksVector() {
	return linksVector;
    }

    public void setLinksVector(Link[][] linksVector) {
	this.linksVector = linksVector;
    }

    public DefaultUndirectedWeightedGraph<Node, Link> getGrapht() {
        return grapht;
    }

    public void setGrapht(DefaultUndirectedWeightedGraph<Node, Link> grapht) {
        this.grapht = grapht;
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }
    // </editor-fold>
    
    public void reset() {
        for (Node node : nodes)
            node.reset();
        for (Link link : links)
            link.reset();
    }
    
    public void visualizeGraph() {
        
        Dimension DEFAULT_SIZE = new Dimension(900, 800);
        
        JFrame frame = new JFrame();
        
        frame.setTitle("JGraphT Adapter to JGraphX Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setSize(DEFAULT_SIZE);
        
        JGraphXAdapter<Node, Link> jgxAdapter = new JGraphXAdapter<>(grapht);
        
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        frame.add(component);
        
//        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
//        // center the circle
//        int radius = 10;
//        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
//        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
//        layout.setRadius(radius);
//        layout.setMoveCircle(true);
        
//        mxOrganicLayout layout = new mxOrganicLayout(jgxAdapter);
        
        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);

//        jgxAdapter.get
        
//        mxStackLayout layout = new mxStackLayout(jgxAdapter);
        
        for (Node n : nodes) {
            layout.setVertexLocation(n, 100, 500);
        }

        layout.execute(jgxAdapter.getDefaultParent());
        
    }
    
    public static double calculateGeographicalDistance(double latitude1, 
            double longitude1,
            double latitude2, 
            double longitude2) {
        double r = 6373.0;
        
        double lat1 = Math.toRadians(latitude1);
        double lng1 = Math.toRadians(longitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng2 = Math.toRadians(longitude2);
        
        double dlon = lng2 - lng1;
        double dlat = lat2 - lat1;
        
        double a = Math.pow(Math.sin(dlat / 2), 2) + 
                Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return r * c;
        
    }
    
}
