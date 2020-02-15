package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Node;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that implements the CADC policy. It chooses the DC with enough resources 
 * available that are reachable through the path with the lowest weight and enough
 * resources available. In other words, it picks the path with lowest weight which
 * reaches a DC with available resources.
 * 
 * @author carlosnatalino
 */
public class ClosestAvailableDC extends ProvisioningPolicy {

    public ClosestAvailableDC() {
        name = "CADC";
    }

    @Override
    public Connection assign(Connection connection) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();

	OpticalRoute route = null, selectedRoute = null;
	double lowestWeight = Double.MAX_VALUE;
	for (Node node : configuration.getTopology().getDatacenters()) {
	    if (node.getFreePUs() >= connection.getRequiredPUs() &&
		node.getFreeSUs() >= connection.getRequiredSUs()) {

		connection.setBlockedByIT(false);
                
                route = Algorithms.getShortestAvailablePath(connection, connection.getSource(), node.getId());
		
		if (route != null && route.getWeight() < lowestWeight) {
                    connection.setBlockedByNetwork(false);
		    lowestWeight = route.getWeight();
		    selectedRoute = route;
		}
	    }
	}
        if (selectedRoute != null) {
            connection.setAccepted(true);
            connection.setRoute(selectedRoute);
            Algorithms.assignResources(connection, selectedRoute);
        }
        else {
            connection.setAccepted(false);
        }
        return connection;
    }

    @Override
    public Connection release(Connection connection) {
        Algorithms.releaseResources(connection);
        return connection;
    }
    
}
