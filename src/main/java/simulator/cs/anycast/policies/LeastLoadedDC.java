package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Node;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that implements the least loaded DC policy. It selects the shortest path
 * that leads to the least loaded DC, in terms of both processing and storage.
 * 
 * @author carlosnatalino
 */
public class LeastLoadedDC extends ProvisioningPolicy {

    public LeastLoadedDC() {
        name = "LLDC";
    }

    @Override
    public Connection assign(Connection connection) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();

	OpticalRoute route = null, selectedRoute = null;
	double leastLoad = Double.MAX_VALUE;
	for (Node node : configuration.getTopology().getDatacenters()) {
	    if (node.getFreePUs() >= connection.getRequiredPUs() &&
		node.getFreeSUs() >= connection.getRequiredSUs()) {

		connection.setBlockedByIT(false);
                
                route = Algorithms.getShortestAvailablePath(connection, connection.getSource(), node.getId());
		
		if (route != null && route.getLoad() < leastLoad) {
                    connection.setBlockedByNetwork(false);
                    leastLoad = route.getLoad();
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
