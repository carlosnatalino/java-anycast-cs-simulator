package simulator.cs.anycast.core;

import java.util.List;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import simulator.cs.anycast.components.Link;
import simulator.cs.anycast.components.Node;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.components.Topology;

/**
 *
 * Class that manipulates jGraphT library to map the topology to a graph and
 * compute the shortest paths.
 * 
 * @author carlosnatalino
 */
public class RoutesContainer {
    
    private Configuration configuration;
    private static int k = 10; // number of k shortest paths
    private OpticalRoute[][][] routes;
    private DefaultUndirectedWeightedGraph<Node, Link> grapht;
    
    private RoutesContainer(Configuration configuration) {
	this.configuration = configuration;
        grapht = new DefaultUndirectedWeightedGraph<>(Link.class);
        buildGraph();
	computeAllRoutes();
    }
    
    public static void init(Configuration configuration) {
	RoutesContainer instance = new RoutesContainer(configuration);
        configuration.setRoutesContainer(instance);
    }
    
    private void buildGraph() {
        Topology topology = configuration.getTopology();

        for (Node node : topology.getNodes())
            grapht.addVertex(node);
        
        for (Link link : topology.getLinks()) {
            grapht.addEdge(topology.getNodes()[link.getSource()], topology.getNodes()[link.getDestination()], link);
            grapht.setEdgeWeight(link, 10.);
        }
        
    }
    
    private void computeAllRoutes() {
        Topology topology = configuration.getTopology();
	routes = new OpticalRoute[topology.getNodes().length][topology.getNodes().length][k];
	for (int x = 0 ; x < topology.getNodes().length ; x++) {
	    for (int y = 0 ; y < topology.getNodes().length ; y++) {
		if (x != y)
		    routes[x][y] = computeKSPRoutes(x, y);
	    }
	}
    }
    
    private OpticalRoute[] computeKSPRoutes(int src, int dst) {
        Topology topology = configuration.getTopology();
	List<GraphPath> kpaths;
	OpticalRoute[] ors = new OpticalRoute[k];
        YenKShortestPath ksp = new YenKShortestPath(grapht);
        kpaths = ksp.getPaths(topology.getNodes()[src], topology.getNodes()[dst], k);
//	System.out.println(src + " -> " + dst);
	Node nSrc, nDst;
	int ipath = 0;
	int temp = 0, previous;
	for (GraphPath gp : kpaths) {
	    OpticalRoute or = new OpticalRoute(src, dst);
            temp = 0;
            previous = src;
	    for (Object edge : gp.getEdgeList()) {
		nSrc = (Node) grapht.getEdgeSource((Link) edge);
		nDst = (Node) grapht.getEdgeTarget((Link) edge);
		or.addLink(topology.getLinksVector()[nSrc.getId()][nDst.getId()]);
                
		temp ++;
	    }
	    temp = 0;
//            configuration.debug(or.toString());
//	    System.out.println(or);
            
            ors[ipath] = or;
            ipath ++;
	}
	
	return ors;
    }

    public OpticalRoute[] getRoutes(int src, int dst) {
        return routes[src][dst];
    }
    
    public static void showRoute(OpticalRoute route) {
        for (Link link : route.getRouteLinks()) {
            System.out.println("Link " + link.getSource() + " -> " + link.getDestination() + " (" + link.getFreeWavelengths() + ")");
        }
    }
    
    public void showStats() {
        double avgPaths;
        int npaths;
        int counter[][] = new int[configuration.getTopology().getNodes().length][8];
        int greater = Integer.MIN_VALUE;
        int totalPaths = 0;
        double nDCs = 0, nNodes = 0;
        OpticalRoute[] routes;
        for (Node dc : configuration.getTopology().getNodes()) {
            avgPaths = 0.0;
            npaths = 0;
            if (dc.isDatacenter()) {
                nDCs++;
                for (Node node : configuration.getTopology().getNodes()) {
                    if (!node.isDatacenter()) {
                        nNodes++;
                        routes = getRoutes(node.getId(), dc.getId());
                        for (OpticalRoute route : routes) {
                            if (route != null) {
                                totalPaths++;
                                npaths++;
                                avgPaths += route.getHopCount();
                                greater = Math.max(greater, route.getHopCount());
                                counter[dc.getId()][route.getHopCount()]++;
                            }
                        }
                    }
                }
                System.out.println(dc.getId() + "\t" + (avgPaths / npaths));
                avgPaths = 0;
                npaths = 0;
            }
            
        }
        System.out.println("Avg paths: " + (totalPaths / (nNodes)));
        System.out.println("Greater: " + greater);
        
        System.out.print("DC");
        for (int i = 0 ; i < counter[0].length ; i++) {
            System.out.print("\t" + i);
        }
        System.out.println("");
        for (int i = 0 ; i < counter.length ; i++) {
            System.out.print(i);
            for (int j = 0 ; j < counter[i].length ; j++) {
                System.out.print("\t" + counter[i][j]);
            }
            System.out.println("");
        }
        
        System.out.println("Reachability of DCs");
        for (Node dc : configuration.getTopology().getNodes()) {
            if (dc.isDatacenter()) {
                System.out.print(dc.getId());
                for (int hops = 1 ; hops < 5 ; hops++) {
                    int n = 0;
                    
                    for (Node node : configuration.getTopology().getNodes()) {
                        if (!node.isDatacenter()) {
                            routes = getRoutes(node.getId(), dc.getId());
                            for (OpticalRoute route : routes) {
                                if (route != null) {
                                    if (route.getHopCount() <= hops) {
                                        n++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    System.out.print("\t" + n);
                }
                System.out.println("");
            }
        }
    }
    
}
