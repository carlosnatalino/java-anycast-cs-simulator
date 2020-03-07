package simulator.cs.anycast.core;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private PriorityQueue<Event<Connection>> events;
    private List<Connection> connections; // all connections handled
    private List<Connection> activeConnections; // only the ones currently active
    private double currentTime = 0.0;
    private boolean log = false;
    private Logger logger;

    public Simulator(Configuration configuration) {
	this.configuration = configuration;
	this.statisticsMonitor = new StatisticsMonitor(configuration);
        this.events = new PriorityQueue<>();
	configuration.setStatisticsMonitor(statisticsMonitor);
	configuration.setSimulator(this);
    }
    
    public void addEvent(Event<Connection> event) {
        events.add(event);
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
	    Event<Connection> evt;
            try { // capturing exceptions thrown by the simulation loop
                while ((evt = events.poll()) != null) {
                    currentTime = evt.getTime();
                    evt.getAp().activity(evt);
                }
            }
            catch (Exception e) {
                logger.fatal("Simulation " + configuration.getId() + " triggered an exception during event loop: " + e.getLocalizedMessage());
                continue;
            }
            duration = Duration.between(lt, LocalDateTime.now()).getSeconds();
            totalDuration += duration;
	    ArrayList<Double> resultExp = statisticsMonitor.computeStatistics(duration); // compute statistics for each experiment
            FileAgent.reportExperimentStatistics(configuration, resultExp);
            statisticsMonitor.reset();
            
            logger.debug("Finishing experiment " + experiment);
	    logger.debug("Start simulation for " + configuration.getBaseName() + "-" + experiment + "-finish");
	}
	logger.debug("Finishing simulation for " + configuration.getBaseName()+ " after " + totalDuration + " seconds");
    }
    

    @Override
    public Boolean call() {
	
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
        this.events = new PriorityQueue<>();
    }

    public boolean isLog() {
	return log;
    }
    
}
