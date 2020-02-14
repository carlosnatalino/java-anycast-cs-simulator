package simulator.cs.anycast.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.events.ConnectionManager;
import simulator.cs.anycast.events.Event;
import simulator.cs.anycast.utils.SimulatorThread;

/**
 *
 * Class that represents one thread that runs a particular scenario of simulation.
 * All the seeds (runs) of the same scenario are run by the same thread.
 * 
 * @author carlosnatalino
 */
public class Simulator implements Callable<Boolean> {
    
    private Configuration configuration;
    private StatisticsMonitor statisticsMonitor;
    private FibonacciHeap<Event> events; //TODO update the heap
    private List<Connection> connections; // all connections handled
    private List<Connection> activeConnections; // only the ones currently active
    private double currentTime = 0.0;
    private double endTime = 0.0;
    private boolean log = false;
    private Logger logger;

    public Simulator(Configuration configuration) {
	this.configuration = configuration;
	this.statisticsMonitor = new StatisticsMonitor(configuration);
	this.events = new FibonacciHeap<>();
	configuration.setStatisticsMonitor(statisticsMonitor);
	configuration.setSimulator(this);
    }
    
    public void addEvent(Event event) {
	events.insert(new FibonacciHeapNode<>(event), event.getTime());
    }
    
    public Event getNextEvent() {
	return events.removeMin().getData();
    }

    public double getCurrentTime() {
	return currentTime;
    }

    public List<Connection> getConnections() {
	return connections;
    }

    public List<Connection> getActiveConnections() {
        return activeConnections;
    }
    
    public void connectionArrived(Connection conn) {
        connections.add(conn);
        activeConnections.add(conn);
    }
    
    public void releaseConnection(Connection conn) {
        if (activeConnections == null || conn == null)
            System.out.println("resolvendo");
        activeConnections.remove(conn);
    }
    
    public void startSimulation() {
	double totalDuration = 0.0, duration = 0.0;
	long seed = configuration.getSeed();
        
	for (int experiment = 0 ; experiment < configuration.getExperiments() ; experiment++) {
            LocalDateTime lt = LocalDateTime.now();
            configuration.getTopology().reset();
            reset();
	    configuration.setId(configuration.getBaseName()+ "-" + experiment);
	    configuration.setSeed(seed + experiment);
            logger = LogManager.getLogger("[" + configuration.getId() + "] Simulator");
            
            logger.debug("Start simulation for " + configuration.getBaseName() + "-" + experiment + "-start - " + Configuration.getFormatter().format(LocalDateTime.now()) + " with seed " + configuration.getSeed());
            logger.debug("Strategy: " + configuration.getPolicy());
	    configuration.setExperiment(experiment);
	    currentTime = 0.0;
	    ConnectionManager.init();
	    Event evt;
	    while (!events.isEmpty()) {
		evt = getNextEvent();
		currentTime = evt.getTime();
		evt.getAp().activity(evt);
	    }
	    endTime = currentTime;
            duration = Duration.between(lt, LocalDateTime.now()).getSeconds();
            totalDuration += duration;
	    statisticsMonitor.computeStatistics(duration); // compute statistics for each experiment
            statisticsMonitor.reset();
            
            logger.debug("Finishing experiment " + experiment);
	    logger.debug("Start simulation for " + configuration.getBaseName() + "-" + experiment + "-finish");
	}
	logger.debug("Finishing simulation for " + configuration.getBaseName()+ " after " + totalDuration + " seconds");
    }
    

    @Override
//    @Loggable
    public Boolean call() {
//	System.out.println("\n#################");
//	for (int i = 0 ; i < 5 ; i++) {
//	    System.out.println(configuration.getUniform());
//	}
	
        SimulatorThread thread = (SimulatorThread) Thread.currentThread();
        thread.setSimulator(this);
        thread.setConfiguration(configuration);
        thread.setStatisticsMonitor(statisticsMonitor);

        configuration.getTopology().calculateDCResources();
        
	this.startSimulation();
	// compute statistics
	statisticsMonitor.printResults();
	return true;
    }
    
    private void reset() {
        this.connections = new ArrayList<>();
        this.activeConnections = new ArrayList<>();
        this.events = new FibonacciHeap<>();
    }

    public boolean isLog() {
	return log;
    }
    
}
