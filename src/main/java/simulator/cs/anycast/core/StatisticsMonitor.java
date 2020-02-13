package simulator.cs.anycast.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
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
 * @author carlosnatalino
 */
public class StatisticsMonitor {
    
    private final Configuration configuration;
    private ArrayList<ArrayList<Double>> resultVector = new ArrayList<>();
    private int restorationAttempts = 0, restorationSuccessful = 0, relocations = 0, noIT = 0, noNetwork = 0, reloIT = 0, reloNetwork = 0;
    double totalAvailability = 0.0, restoredRemainingTime = 0.0;
    
    private BigInteger proactiveTriggers, activeConnections, checkedConnections, checkedPaths, vulnerableConnections, movedConnections, linksViolated;
    
    // variables for ILPs
    private BigInteger problemsSolved = BigInteger.ZERO;
    private BigDecimal solvingTime = BigDecimal.ZERO;
    
    private int[][] maxConcurrentRelocations;
    private int[] histogram, histogram_sum, histogram_max;
    
    private final Logger logger;

    public StatisticsMonitor(Configuration configuration) {
	this.configuration = configuration;
        reset();
	maxConcurrentRelocations = new int[configuration.getTopology().getNodes().length][configuration.getTopology().getNodes().length];
	histogram = new int[configuration.getWavelengthsPerFiber()];
	histogram_sum = new int[configuration.getWavelengthsPerFiber()];
	histogram_max = new int[configuration.getWavelengthsPerFiber()];
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
		.filter(s -> s.getId() > configuration.getIgnoreFirst() && s.getId() <= configuration.getNumberArrivals() && !s.isAccepted())
		.count();
	double blocking = (blocked / configuration.getNumberArrivals());
	result.add(blocking);
	
	double totalHoldingTime = configuration.getSimulator().getConnections().stream()
                .filter(s -> s.getId() > configuration.getIgnoreFirst() && s.getId() <= configuration.getNumberArrivals() && s.isAccepted())
		.mapToDouble(Connection::getHoldingTime)
		.sum();
        result.add(totalHoldingTime);
        
	double averageHoldingTime = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.getId() > configuration.getIgnoreFirst() && s.getId() <= configuration.getNumberArrivals())
		.mapToDouble(Connection::getHoldingTime)
		.average()
		.getAsDouble();
	result.add(averageHoldingTime);
        
        double averageLinkUtilization = Stream.of(configuration.getTopology().getLinks())
                .mapToDouble(Link::getUtilization)
                .average()
                .getAsDouble();
        result.add(averageLinkUtilization);
        
        double averagePUUtilization = Stream.of(configuration.getTopology().getNodes())
                .mapToDouble(Node::getProcessingUtilization)
                .average()
                .getAsDouble();
        result.add(averagePUUtilization);
        
        double averageSUUtilization = Stream.of(configuration.getTopology().getNodes())
                .mapToDouble(Node::getStorageUtilization)
                .average()
                .getAsDouble();
        result.add(averageSUUtilization);
        
        // consider the DC nodes only
        ArrayList<Node> dcList = new ArrayList();
        for (Node n : configuration.getTopology().getNodes()) {
            if (n.isDatacenter())
                dcList.add(n);
        }
        
        result.add(duration);
	
        result.add(proactiveTriggers.doubleValue());
        result.add(activeConnections.doubleValue());
        result.add(checkedConnections.doubleValue());
        result.add(checkedPaths.doubleValue());
        result.add(vulnerableConnections.doubleValue());
        result.add(movedConnections.doubleValue());
        result.add(linksViolated.doubleValue());
        
        double restoRemTime = 0.0;
        if (restorationSuccessful > 0)
            restoRemTime = restoredRemainingTime / (double) restorationSuccessful;
        result.add(restoRemTime);
        
        double hopCount = configuration.getSimulator().getConnections().stream()
		.filter(s -> s.isAccepted())
                .mapToInt(Connection::getHopCount)
                .average()
                .getAsDouble();
	result.add(hopCount);
	
	double avgSolvingTime = 0.0;
	if (problemsSolved.doubleValue() > 0.0)
	    avgSolvingTime = solvingTime.divide(BigDecimal.valueOf(problemsSolved.longValue()), MathContext.DECIMAL128).doubleValue();
	result.add(avgSolvingTime);
	System.out.println("SolvingTime1: " + solvingTime + "\t" + problemsSolved + "\t" + avgSolvingTime);
	
	if (restorationAttempts > 0)
	    avgSolvingTime = solvingTime.divide(BigDecimal.valueOf((double) restorationAttempts), MathContext.DECIMAL128).doubleValue();
	result.add(avgSolvingTime);
	System.out.println("SolvingTime2: " + solvingTime + "\t" + restorationAttempts + "\t" + avgSolvingTime);
        
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
//                    System.out.print("Confidence interval\tseries: {");
//                    ciSequences.get(i).forEach(x -> {
//                        System.out.print(x + ",");
//                    });
//                    System.out.print("}\tAvg: " + avg + "\tstdev: " + stdDev + "\tconfid: " + afValue);
//                    System.out.println("");
            results.add(BigDecimal.valueOf(ciValue));
        }
	
	int maxRelo = Integer.MIN_VALUE;
	for (int i = 0 ; i < maxConcurrentRelocations.length ; i++) {
	    for (int j = 0 ; j < maxConcurrentRelocations.length ; j++) {
		maxRelo = Math.max(maxRelo, maxConcurrentRelocations[i][j]);
	    }
	}
	results.add(BigDecimal.valueOf(maxRelo));
        
	return results;
    }
    
    public void printResults() {
	FileAgent.reportFinalStatistics(configuration, computeAvgStatistics());
    }
    
    public void reportRestorability(int attempts, int successful, int numRelocations, double totalAvailability, double restoredRemTime, int noIT, int noNetwork, int reloIT, int reloNetwork) {
	restorationAttempts += attempts;
        restorationSuccessful += successful;
        relocations += numRelocations;
        this.noIT += noIT;
        this.noNetwork += noNetwork;
        this.reloIT += reloIT;
        this.reloNetwork += reloNetwork;
        this.totalAvailability += totalAvailability;
        this.restoredRemainingTime += restoredRemTime;
	
    }
    
    public void reportSolvingTime(double solvingTime) {
	this.problemsSolved = this.problemsSolved.add(BigInteger.ONE);
	this.solvingTime = this.solvingTime.add(BigDecimal.valueOf(solvingTime));
    }
    
    public void reportProactiveStats(int active, int checkedConns, int checkedPaths, int vulnerable, int moved, int links) {
        this.proactiveTriggers = this.proactiveTriggers.add(BigInteger.ONE);
        this.activeConnections = this.activeConnections.add(BigInteger.valueOf(active));
        this.checkedConnections = this.checkedConnections.add(BigInteger.valueOf(checkedConns));
        this.checkedPaths = this.checkedPaths.add(BigInteger.valueOf(checkedPaths));
        this.vulnerableConnections = this.vulnerableConnections.add(BigInteger.valueOf(vulnerable));
        this.movedConnections = this.movedConnections.add(BigInteger.valueOf(moved));
        this.linksViolated = this.linksViolated.add(BigInteger.valueOf(links));
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
        restorationAttempts = restorationSuccessful = relocations = 0;
        totalAvailability = 0.0;
        restoredRemainingTime = 0.0;
        proactiveTriggers = BigInteger.ZERO;
        activeConnections = BigInteger.ZERO;
        checkedConnections = BigInteger.ZERO;
        checkedPaths = BigInteger.ZERO;
        vulnerableConnections = BigInteger.ZERO;
        movedConnections = BigInteger.ZERO;
        linksViolated = BigInteger.ZERO;
	problemsSolved = BigInteger.ZERO;
	solvingTime = BigDecimal.ZERO;
    }
    
    public void reportConcurrentRelocations(int[][] current) {
//	System.out.println("Max Concurrent Relocations:");
	int sum = 0;
	int max = 0;
	for (int i = 0 ; i < maxConcurrentRelocations.length ; i++) {
	    for (int j = 0 ; j < maxConcurrentRelocations.length ; j++) {
		if (i != j && configuration.getTopology().getNodes()[i].isDatacenter() && configuration.getTopology().getNodes()[j].isDatacenter()) {
		    maxConcurrentRelocations[i][j] = Math.max(maxConcurrentRelocations[i][j], current[i][j]);
		    histogram[current[i][j]]++;
		    sum += current[i][j];
		    max = Math.max(max, current[i][j]);
		}
	    }
	}
	histogram_sum[sum]++;
	histogram_max[max]++;
    }
    
    public void printHistogram() {
	String out = "";
	out += "histogram_" + configuration.getPolicy()+ "_" + configuration.getLoad() + " = [";
	for (int i = 0 ; i < histogram.length ; i++) {
	    out += "\t" + histogram[i];
	}
	out += "\t];";
	System.out.println(out);
	logger.info(out);
	
	out = "";
	out += "histogram_sum_" + configuration.getPolicy()+ "_" + configuration.getLoad() + " = [";
	for (int i = 0 ; i < histogram_sum.length ; i++) {
	    out += "\t" + histogram_sum[i];
	}
	out += "\t];";
	System.out.println(out);
	logger.info(out);
	
	out = "";
	out += "histogram_max_" + configuration.getPolicy()+ "_" + configuration.getLoad() + " = [";
	for (int i = 0 ; i < histogram_max.length ; i++) {
	    out += "\t" + histogram_max[i];
	}
	out += "\t];";
	System.out.println(out);
	logger.info(out);
    }
    
}
