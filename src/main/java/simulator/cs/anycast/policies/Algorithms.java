/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Link;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * @author carda
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
    
    protected static OpticalRoute getClosestDC(Connection connection) {
	Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();

	OpticalRoute route = null, selectedRoute = null;
	int lowestHopCountDC = Integer.MAX_VALUE, lowestHopCountTot = Integer.MAX_VALUE;
	for (int node = 0; node < configuration.getTopology().getNodes().length; node++) {
	    if (configuration.getTopology().getDatacenters()[node]
		    && configuration.getTopology().getNodes()[node].getFreePUs() >= connection.getRequiredPUs()
		    && configuration.getTopology().getNodes()[node].getFreeSUs() >= connection.getRequiredSUs()) {

		connection.setBlockedByIT(false);
		// configuration.debug("[" + connection.getId() + "] check DC "
		// + (node) + " with " +
		// configuration.getTopology().getNodes()[node].getFreePUs() + "
		// PUs and "
		// + configuration.getTopology().getNodes()[node].getFreeSUs() +
		// " SUs");

		OpticalRoute[] routes = configuration.getRoutesContainer().getRoutes(connection.getSource(), node);
		for (OpticalRoute or : routes) {
		    if (or != null
			    && or.getFreeWavelengths() >= connection.getRequiredLPs()) {

			connection.setBlockedByNetwork(false);

			if (or.getHopCount() < lowestHopCountDC) {
			    lowestHopCountDC = or.getHopCount();
			    route = or;
			}
			// configuration.debug("[" + connection.getId() + "]
			// check path " + or + " with " +
			// or.getFreeWavelengths() + " free WL");
		    }
		    // else {
		    // configuration.debug("[" + connection.getId() + "] invalid
		    // path " + or);
		    // }
		}
		if (lowestHopCountDC < lowestHopCountTot) {
		    lowestHopCountTot = lowestHopCountDC;
		    selectedRoute = route;
		}
	    }
	    // else if (configuration.getTopology().getDatacenters()[node]) {
	    // configuration.debug("[" + connection.getId() + "] not suitable DC
	    // " + (node) + " with " +
	    // configuration.getTopology().getNodes()[node].getFreePUs() + " PUs
	    // and "
	    // + configuration.getTopology().getNodes()[node].getFreeSUs() + "
	    // SUs");
	    // }
	}
	if (selectedRoute != null) {
	    return selectedRoute;
	} else {
	    return null;
	}
    }
    
}
