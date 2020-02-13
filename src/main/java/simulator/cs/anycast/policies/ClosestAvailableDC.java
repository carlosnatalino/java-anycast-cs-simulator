/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.OpticalRoute;

/**
 *
 * @author carda
 */
public class ClosestAvailableDC extends ProvisioningPolicy {

    public ClosestAvailableDC() {
        name = "CADC";
    }

    @Override
    public Connection assign(Connection connection) {
        OpticalRoute route = Algorithms.getClosestDC(connection);
        if (route != null) {
            connection.setAccepted(true);
            connection.setRoute(route);
            Algorithms.assignResources(connection, route);
//            configuration.println("[assignment]  Connection " + connection.getId() + " accepted with route " + route + " and holding time " + connection.getHoldingTime());
        }
        else {
//            Algorithms.printRouteInformation(connection, -1, null);
            connection.setAccepted(false);
//            configuration.println("[assignment] Connection " + connection.getId() + " rejected");
        }
        return connection;
    }

    @Override
    public Connection release(Connection connection) {
        Algorithms.releaseResources(connection);
        return connection;
    }
    
}
