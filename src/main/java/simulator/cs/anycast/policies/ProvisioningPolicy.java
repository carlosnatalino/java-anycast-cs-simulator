package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;
import java.util.Arrays;

/**
 *
 * Abstract class that defines the standard properties and methods that every
 * class that wants to implement a policy should have. These methods are used
 * then in the ConnectionManager class through reflection to dynamically invoke
 * the appropriate policy according to the configuration file.
 * 
 * @author carlosnatalino
 */
public abstract class ProvisioningPolicy {

    public enum Policy {
        CLOSEST_AVAILABLE_DC(1), LEAST_LOADED_PATH(2), LEAST_LOADED_DC(3), FULL_LOAD_BALANCING(4);

        private int id;

        private Policy(int id) {
            this.id = id;
        }

        public static Policy fromInteger(int x) {
            return Arrays.asList(Policy.values()).stream().filter(item -> item.getId() == x).findFirst().get();
        }

        public int getId() {
            return this.id;
        }
    }

    public static String getPolicyClassName(ProvisioningPolicy.Policy plc) {
        switch (plc) {
            case CLOSEST_AVAILABLE_DC:
                return "simulator.cs.anycast.policies.ClosestAvailableDC";
            case LEAST_LOADED_PATH:
                return "simulator.cs.anycast.policies.LeastLoadedPath";
            case LEAST_LOADED_DC:
                return "simulator.cs.anycast.policies.LeastLoadedDC";
            case FULL_LOAD_BALANCING:
                return "simulator.cs.anycast.policies.FullLoadBalancing";

            default:
                return "Error";
        }
    }

    public String name;

    public abstract Connection assign(Connection connection);

    public abstract Connection release(Connection connection);

    public String getName() {
        return name;
    }
}
