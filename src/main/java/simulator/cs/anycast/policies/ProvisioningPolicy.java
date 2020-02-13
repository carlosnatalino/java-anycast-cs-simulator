/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.cs.anycast.policies;

import simulator.cs.anycast.components.Connection;

/**
 *
 * @author carda
 */
public abstract class ProvisioningPolicy {
    
    public String name;
    
    public abstract Connection assign(Connection connection);
    
    public abstract Connection release(Connection connection);
    
}
