package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Link;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that contains several static helper methods such as the ones to find
 * the shortest available path, the least load path.
 * 
 * @author carlosnatalino
 */
public class Algorithms {
    
    public static void assignResources(Connection connection, OpticalRoute route) {
	connection.setDestination(route.getDestination());
	for (Link link : route.getRouteLinks()) {
	    link.addConnection(connection);
	}
	((SimulatorThread) Thread.currentThread()).getConfiguration().getTopology().getNodes()[connection
		.getRoute().getDestination()].addConnection(connection);
    }
    
    public static void releaseResources(Connection connection) {
	SimulatorThread thread = (SimulatorThread) Thread.currentThread();

	if (connection.getRoute() != null) {
	    for (Link link : connection.getRoute().getRouteLinks()) {
		// System.out.println(connection.getId() + " [ " +
		// link.getSource() + " -> " + link.getDestination() + " ]
		// release");
		link.removeConnection(connection);
	    }
	}
	thread.getConfiguration().getTopology().getNodes()[connection.getDestination()].removeConnection(connection);
    }
    
    protected static OpticalRoute getShortestAvailablePath(Connection connection, int src, int dst) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();
        OpticalRoute[] routes = configuration.getRoutesContainer().getRoutes(src, dst);
        double lowestWeight = Double.MAX_VALUE;
        OpticalRoute route = null;
        for (OpticalRoute or : routes) {
            if (or != null
                && or.getFreeWavelengths() >= connection.getRequiredLPs()) {
                if (or.getWeight() < lowestWeight) {
                    lowestWeight = or.getWeight();
                    route = or;
                }
            }
        }
        return route;
    }
    
    protected static OpticalRoute getLeastLoadedPath(Connection connection, int src, int dst) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();
        OpticalRoute[] routes = configuration.getRoutesContainer().getRoutes(src, dst);
        double leastLoad = Double.MIN_VALUE;
        OpticalRoute route = null;
        for (OpticalRoute or : routes) {
            if (or != null
                && or.getFreeWavelengths() >= connection.getRequiredLPs()) {
                if (or.getLoad() < leastLoad) {
                    leastLoad = or.getLoad();
                    route = or;
                }
            }
        }
        return route;
    }
    
}
