package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;

/**
 *
 * Abstract class that defines the standard properties and methods that every
 * class that wants to implement a policy should have. These methods are used
 * then in the ConnectionManager class through reflection to dynamically
 * invoke the appropriate policy according to the configuration file.
 * 
 * @author carlosnatalino
 */
public abstract class ProvisioningPolicy {
    
    public String name;
    
    public abstract Connection assign(Connection connection);
    
    public abstract Connection release(Connection connection);
    
}
