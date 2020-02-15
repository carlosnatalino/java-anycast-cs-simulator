package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Node;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that implements the least loaded path policy. It selects the least loaded
 * path that leads to a DC with enough processing and storage resources.
 * 
 * @author carlosnatalino
 */
public class LeastLoadedPath extends ProvisioningPolicy {

    public LeastLoadedPath() {
        name = "LLP";
    }

    @Override
    public Connection assign(Connection connection) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();

	OpticalRoute route = null, selectedRoute = null;
	double lowestLoad = Double.MAX_VALUE;
	for (Node node : configuration.getTopology().getDatacenters()) {
	    if (node.getFreePUs() >= connection.getRequiredPUs() &&
		node.getFreeSUs() >= connection.getRequiredSUs()) {

		connection.setBlockedByIT(false);
                
                route = Algorithms.getLeastLoadedPath(connection, connection.getSource(), node.getId());
		
		if (route != null && route.getLoad() < lowestLoad) {
                    connection.setBlockedByNetwork(false);
		    lowestLoad = route.getLoad();
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
