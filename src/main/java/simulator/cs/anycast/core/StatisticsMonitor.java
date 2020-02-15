package simulator.cs.anycast.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import simulator.cs.anycast.components.Connection;
import simulator.cs.anycast.components.Link;
import simulator.cs.anycast.components.Node;

/**
 *
 * Class responsible for keeping statistics from each simulation run (seed).
 * At the end of the simulation, it is also responsible for computing and 
 * consolidating the final statistics.
 * 
 * Uses lambda expressions to compute the statistics.
 * 
 * @author carlosnatalino
 */
public class StatisticsMonitor {
    
    private final Configuration configuration;
    private ArrayList<ArrayList<Double>> resultVector = new ArrayList<>();
    
    private final Logger logger;

    public StatisticsMonitor(Configuration configuration) {
	this.configuration = configuration;
        reset();
	logger = LogManager.getLogger("[" + configuration.getId() + "] StatisticsMonitor");
    }
    
    /**
     * Method to compute the statistics after one experiment
     */
    public void computeStatistics(double duration) {
	ArrayList<Double> result = new ArrayList<>();
	result.add(configuration.getLoad().doubleValue());
	
        logger.info("Total connections arrived: " + configuration.getSimulator().getConnections().size());
        
	double blocked = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.getId() > configuration.getIgnoreFirst() && !s.isAccepted())
		.count();
	double blocking = (blocked / configuration.getNumberArrivals());
	result.add(blocking);
	
        double averageLinkUtilization = Stream.of(configuration.getTopology().getLinks())
                .mapToDouble(Link::getUtilization)
                .average()
                .getAsDouble();
        result.add(averageLinkUtilization);
        
        double averagePUUtilization = Stream.of(configuration.getTopology().getNodes())
                .filter(n -> n.isDatacenter())
                .mapToDouble(Node::getProcessingUtilization)
                .average()
                .getAsDouble();
        result.add(averagePUUtilization);
        
        double averageSUUtilization = Stream.of(configuration.getTopology().getNodes())
                .filter(n -> n.isDatacenter())
                .mapToDouble(Node::getStorageUtilization)
                .average()
                .getAsDouble();
        result.add(averageSUUtilization);
        
        double hopCount = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.isAccepted())
                .mapToInt(Connection::getHopCount)
                .average()
                .getAsDouble();
	result.add(hopCount);
        
        double avgWeight = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.isAccepted())
                .mapToDouble(Connection::getRouteWeight)
                .average()
                .getAsDouble();
	result.add(avgWeight);
        
        result.add(duration);
        
        double totalHoldingTime = configuration.getSimulator().getConnections().stream()
                .filter(s -> s.getId() > configuration.getIgnoreFirst() && s.isAccepted())
		.mapToDouble(Connection::getHoldingTime)
		.sum();
        result.add(totalHoldingTime);
        
	double averageHoldingTime = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.getId() > configuration.getIgnoreFirst())
		.mapToDouble(Connection::getHoldingTime)
		.average()
		.getAsDouble();
	result.add(averageHoldingTime);
        
	FileAgent.reportExperimentStatistics(configuration, result);
	resultVector.add(result);
        
    }
    
    private ArrayList<BigDecimal> computeAvgStatistics() {
	ArrayList<BigDecimal> results = new ArrayList<>();
        for (int i = 0 ; i < resultVector.get(0).size() ; i++) {
	    results.add(BigDecimal.ZERO);
	}
        
        // list of indexes which should have confidence interval calculated
        ArrayList<ArrayList<Double>> ciSequences = new ArrayList<>();
        double avg, stdDev, ciValue;
        ArrayList<Integer> calculateInterval = new ArrayList<>();
        calculateInterval.add(1);
//        calculateInterval.add(5);
        // populate the initial confidence interval sequences
        for (int i = 0; i < calculateInterval.size(); i++)
            ciSequences.add(new ArrayList<>());
	
	for (ArrayList<Double> intermediary : resultVector) {
	    for (int i = 0 ; i < intermediary.size() ; i++) {
		results.set(i, results.get(i).add(BigDecimal.valueOf(intermediary.get(i))));
                
                if (calculateInterval.contains(i))
                    ciSequences.get(calculateInterval.indexOf(i)).add(intermediary.get(i));
	    }
	}
        
	for (int i = 0; i < results.size(); i++) {
	    results.set(i, results.get(i).divide(BigDecimal.valueOf(resultVector.size()), 8, RoundingMode.HALF_UP));
	}
        
        for (int i = 0; i < calculateInterval.size(); i++) {
            avg = results.get(calculateInterval.get(i)).doubleValue();
            stdDev = stdDev(ciSequences.get(i), avg);
            ciValue = confidenceValue(stdDev, resultVector.size(), 95);
            results.add(BigDecimal.valueOf(ciValue));
        }
        
	return results;
    }
    
    public void printResults() {
	FileAgent.reportFinalStatistics(configuration, computeAvgStatistics());
    }
    
    private static double stdDev(ArrayList<Double> values, double mean) {
	double sum = 0;
	for (double value : values)
	    sum += Math.pow((value - mean), 2);
	if (values.size() > 1)
	    return Math.sqrt(sum / (values.size() - 1));
	else return 0;
    }
    
    private static double confidenceValue(double stdDev, int size, int level) {
	double confidence = 0;
	if (level == 95) {
	    confidence = (1.96 * stdDev) / Math.sqrt(size);
	}
	return confidence;
    }
    
    /**
     * Method used to reset the counters between two experiments
     */
    public void reset() {
    }
    
}
