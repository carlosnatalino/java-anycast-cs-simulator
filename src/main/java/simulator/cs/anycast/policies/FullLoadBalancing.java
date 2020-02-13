package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.OpticalRoute;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that implements a policy that considers the load of the path, processing
 * and storage at the DC. The weight is computed as the multiplication of these 
 * three loads. Finally, the option with the lowest multiplication factor is chosen.
 * 
 * @author carlosnatalino
 */
public class FullLoadBalancing extends ProvisioningPolicy {

    public FullLoadBalancing() {
        name = "FLB";
    }

    @Override
    public Connection assign(Connection connection) {
        Configuration configuration = ((SimulatorThread) Thread.currentThread()).getConfiguration();

	OpticalRoute route = null, selectedRoute = null;
	double lowestLoad = Double.MAX_VALUE, load;
	for (int node = 0; node < configuration.getTopology().getNodes().length; node++) {
	    if (configuration.getTopology().getDatacenters()[node]
		    && configuration.getTopology().getNodes()[node].getFreePUs() >= connection.getRequiredPUs()
		    && configuration.getTopology().getNodes()[node].getFreeSUs() >= connection.getRequiredSUs()) {

		connection.setBlockedByIT(false);
                
                route = Algorithms.getLeastLoadedPath(connection, connection.getSource(), node);
		
		if (route != null) {
                    load = route.getLoad() * configuration.getTopology().getNodes()[node].getLoad();
                    if (load < lowestLoad) {
                        connection.setBlockedByNetwork(false);
                        lowestLoad = route.getLoad();
                        selectedRoute = route;
                    }
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
