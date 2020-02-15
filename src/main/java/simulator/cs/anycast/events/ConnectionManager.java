package simulator.cs.anycast.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.policies.ProvisioningPolicy;

/**
 *
 * Class that manages the connections, i.e., it is responsible for generating
 * randomly the arrivals and selecting source. Then, it processes arrivals
 * and departures.
 * 
 * @author carlosnatalino
 */
public class ConnectionManager extends ActiveProcess {
    
    private int arrivedConnections = 0;
    
    private static final int ARRIVAL = 1;
    private static final int RELEASE = 2;
    private Connection temp;
    
    // objects to invoke restore method dynamically
    private ProvisioningPolicy policyObj;
    
    private final Logger logger;
    
    public static void init() {
//	if (connectionManager == null)
	new ConnectionManager();
    }

    private ConnectionManager() {
	super();
        logger = LogManager.getLogger("[" + configuration.getId() + "] ConnectionManager");
	configuration.setConnectionManager(this);
        scheduleNextArrival();
        try {
            Class<ProvisioningPolicy> strategyCls = (Class<ProvisioningPolicy>) Class.forName(ProvisioningPolicy.getPolicyClassName(configuration.getPolicy()));
            policyObj = strategyCls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.fatal("ConnectionManager:ConnectionManager: something bad is about to happen!!!! ", e);
//            System.exit(10);
        }
        
    }
    
    public String getPolicyName() {
        return policyObj.getName();
    }
    
    private void scheduleNextArrival() {
	double nextConnectionArrivalTime = getSimulator().getCurrentTime() + 
                configuration.getRandPoisson(configuration.getMeanConnectionInterArrivalTime());
	getSimulator().addEvent(new Event(nextConnectionArrivalTime, this, ConnectionManager.ARRIVAL));
    }

    @Override
    public void activity(Event event) {
	if (event.getEventType() == ARRIVAL)
	    processArrival(event);
	else if (event.getEventType() == RELEASE)
	    processRelease(event);
    }
    
    private void processArrival(Event event) {
	double holdingTime = configuration.getExponential() * configuration.getMeanConnectionHoldingTime();
        
        int source = -1;
        
        do {
            source = configuration.getUniform(0, configuration.getTopology().getNodes().length-1);
        } while (configuration.getTopology().getNodes()[source].isDatacenter());
        
//        int storage = configuration.getUniform(configuration.getMinStorageUnits(), configuration.getMaxStorageUnits());
        int storage = getStorageUnits();
        int processing = 0;
        
        processing = configuration.getProcessingUnitValues()[getProcessingUnits()];
//        int processing = 1; // TODO For testing purposes only. Original code: int processing = configuration.getProcessingUnitValues()[getProcessingUnits()];
        
	Connection newConnection = new Connection(++arrivedConnections, source, holdingTime, configuration);
        newConnection.setRequiredSUs(storage);
        newConnection.setRequiredPUs(processing);
	getSimulator().connectionArrived(newConnection);
	
//	if (getSimulator().isLog())
//	    configuration.println("ConnectionManager::processArrival\t" + arrivedConnections);
	
	// provision the connection
        try {
            policyObj.assign(newConnection);
        } catch (Exception e) {
            logger.fatal("ConnectionManager:processArrival: error processing arrival: ", e);
//            System.exit(10);
        }
	// schedule connection finish
	if (newConnection.isAccepted()) {
	    getSimulator().addEvent(new Event<Connection>(getSimulator().getCurrentTime() + holdingTime, newConnection, ConnectionManager.RELEASE, this));
        }
	
	// schedule next arrival
	if (arrivedConnections < configuration.getNumberArrivals()) {
	    // schedule new connection
	    scheduleNextArrival();
	    
	    if (arrivedConnections % 1000 == 0) {
		logger.info("[ " + arrivedConnections + " ] arrived connections");
            }
	    
	}
    }
    
    private void processRelease(Event event) {
        Connection connection = (Connection) event.getContext();

        try {
            policyObj.release(connection);
        } catch (Exception e) {
            logger.fatal("[" + configuration.getSimulator().getCurrentTime() + "] ConnectionManager:processRelease: Method name incorrect for connection [" + connection.getId() + "].", e);
//                e.printStackTrace();
//                System.exit(10);
        }

        configuration.getSimulator().releaseConnection(connection);

    }

    public int getArrivedConnections() {
	return arrivedConnections;
    }
    
    private int getProcessingUnits() {
        double avg = configuration.getProcessingUnitValues().length / 2;
        double stdDev = avg / 2;
        return (int) Math.abs(Math.max(0, Math.min(configuration.getNormal(avg, stdDev), configuration.getProcessingUnitValues().length-1)));
    }
    
    private int getStorageUnits() {
	double avg = (configuration.getMinStorageUnits() + configuration.getMaxStorageUnits())/2;
        return (int) Math.abs(configuration.getNormal(avg, avg/2));
    }
    
}
