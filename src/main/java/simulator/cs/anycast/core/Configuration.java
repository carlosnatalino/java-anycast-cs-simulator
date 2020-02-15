package simulator.cs.anycast.core;

import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import simulator.cs.anycast.components.Topology;
import simulator.cs.anycast.events.ConnectionManager;

/**
 * This class should be spread for all the objects in the simulator
 * which need to have a context and save statistics.
 * This class also should contain the statistics for a given simulation
 * @author carlosnatalino
 */
public class Configuration {
    
    private Logger logger;
    
    public enum Policy {
        CLOSEST_AVAILABLE_DC(1),
        LEAST_LOADED_PATH(2),
        LEAST_LOADED_DC(3),
        FULL_LOAD_BALANCING(4);
	private int numVal;
	private Policy(int numVal) {
	    this.numVal = numVal;
	}
	public static Policy fromInteger(int x) {
	    switch(x) {
		case 1:
		    return CLOSEST_AVAILABLE_DC;
                case 2:
                    return LEAST_LOADED_PATH;
                case 3:
                    return LEAST_LOADED_DC;
                case 4:
                    return FULL_LOAD_BALANCING;
                
		default:
		    return null;
	    }
	}
    }
    
    public static String getPolicyClassName(Configuration.Policy plc) {
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
    
    private static final DateTimeFormatter formatter;
    static {
	formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    }
    
    private Integer[] loads = {0}; // the set of loads configured in the configuration file
    private Integer load = 0; // the load currently used
    
    private Policy[] strategies;
    private Policy policy;
    
    private long seed;
    private Random generator;
    private int numberArrivals = 1000000;
    private int ignoreFirst = 10;
    private Simulator simulator;
    private StatisticsMonitor statisticsMonitor;
    private Topology topology;
    private RoutesContainer routesContainer;
    private ConnectionManager connectionManager;
    
    private String id = "novo";
    private String baseName = "test";
    private String topologyName;
    private String suffix;
    private String baseFolder;
    private int experiment = 0;
    
    private int experiments = 30; // number of experiments to be averaged
    
    private int wavelengthsPerFiber = 80;

    private double meanConnectionInterArrivalTime = 0.0; // depending on the load
    private double meanConnectionHoldingTime = 216000.0; // 60 minutes
    
    private double rhoStorage = 1.8;
    private double rhoProcessing = 1.2;
    private Double rhosStorage[] = {1.8};
    private Double rhosProcessing[] = {1.2};
    
    private int minStorageUnits = 1;
    private int maxStorageUnits = 1240;
    private double averageSUSize = 80; // Gb
    
    private int[] processingUnitValues = {1, 2, 4, 8, 12, 16, 24, 32, 40};
    private int averagePUValue = 15;
    
    private int numberThreads = 1;
    
    // relocation parameters
    private double speedOnFiber = 2E5; // km/s
    private double lightpathThroughtput = 100; // Gbps

    public Configuration(long seed) {
	this.seed = seed;
	this.generator = new Random(seed);
        
    }
    
    public double getUniform() {
	return generator.nextDouble();
    }
    
    public int getUniform(int min, int max) {
	return min + generator.nextInt(max - min + 1);
    }
    
    public double getUniform(double min, double max) {
	return min + (generator.nextDouble() * (max - min));
    }
    
    public double getRandPoisson(double mean) {
	double product = 0;
	int count = 0;
	while (product < 1.0) {
	    product -= Math.log(generator.nextDouble()) / mean;
	    count++; // keep result one behind
	}
	return (count - 1);
    }
    
    public double getExponential() {
	return -Math.log(1 - generator.nextDouble());
    }
    
    public double getNormal(double mean, double stdDev) {
        return generator.nextGaussian() * stdDev + mean;
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public int getNumberArrivals() {
	return numberArrivals;
    }

    public void setNumberArrivals(int connectionLimit) {
	this.numberArrivals = connectionLimit;
    }

    public int getIgnoreFirst() {
        return ignoreFirst;
    }

    public void setIgnoreFirst(int ignoreFirst) {
        this.ignoreFirst = ignoreFirst;
    }

    public double getMeanConnectionInterArrivalTime() {
	return meanConnectionInterArrivalTime;
    }

    public double getMeanConnectionHoldingTime() {
	return meanConnectionHoldingTime;
    }

    public int getNumberThreads() {
	return numberThreads;
    }

    public void setNumberThreads(int numberThreads) {
	this.numberThreads = numberThreads;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
        logger = LogManager.getLogger("[" + id + "] Configuration");
    }

    public String getBaseName() {
	return baseName;
    }

    public void setBaseName(String baseName) {
	this.baseName = baseName;
    }

    public Simulator getSimulator() {
	return simulator;
    }

    public void setSimulator(Simulator simulator) {
	this.simulator = simulator;
    }

    public StatisticsMonitor getStatisticsMonitor() {
	return statisticsMonitor;
    }

    public void setStatisticsMonitor(StatisticsMonitor statisticsMonitor) {
	this.statisticsMonitor = statisticsMonitor;
    }

    public int getExperiments() {
	return experiments;
    }

    public void setExperiments(int experiments) {
	this.experiments = experiments;
    }

    public long getSeed() {
	return seed;
    }
    
    public void setSeed(long seed) {
	this.seed = seed;
	this.generator = new Random(seed);
    }

    public Integer[] getLoads() {
	return loads;
    }

    public void setLoads(Integer[] loads) {
	this.loads = loads;
    }

    public Policy[] getStrategies() {
	return strategies;
    }

    public void setStrategies(Policy[] strategies) {
	this.strategies = strategies;
    }

    public Integer getLoad() {
	return load;
    }

    public Policy getPolicy() {
	return policy;
    }

    public void setPolicy(Policy strategy) {
	this.policy = strategy;
    }

    public String getTopologyName() {
	return topologyName;
    }

    public void setTopologyName(String topologyName) {
	this.topologyName = topologyName;
    }

    public int getWavelengthsPerFiber() {
	return wavelengthsPerFiber;
    }

    public void setWavelengthsPerFiber(int wavelengthsPerFiber) {
	this.wavelengthsPerFiber = wavelengthsPerFiber;
    }

    public Topology getTopology() {
	return topology;
    }

    public void setTopology(Topology topology) {
	this.topology = topology;
    }

    public ConnectionManager getConnectionManager() {
	return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
	this.connectionManager = connectionManager;
    }

    public String getSuffix() {
	return suffix;
    }

    public void setSuffix(String suffix) {
	this.suffix = suffix;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    public int getExperiment() {
	return experiment;
    }

    public void setExperiment(int experiment) {
	this.experiment = experiment;
    }

    public Double[] getRhosStorage() {
        return rhosStorage;
    }

    public void setRhosStorage(Double[] rhosStorage) {
        this.rhosStorage = rhosStorage;
    }

    public Double[] getRhosProcessing() {
        return rhosProcessing;
    }

    public void setRhosProcessing(Double[] rhosProcessing) {
        this.rhosProcessing = rhosProcessing;
    }

    public double getRhoStorage() {
        return rhoStorage;
    }

    public void setRhoStorage(double rhoStorage) {
        this.rhoStorage = rhoStorage;
    }

    public double getRhoProcessing() {
        return rhoProcessing;
    }

    public void setRhoProcessing(double rhoProcessing) {
        this.rhoProcessing = rhoProcessing;
    }

    public double getSpeedOnFiber() {
        return speedOnFiber;
    }

    public double getLightpathThroughtput() {
        return lightpathThroughtput;
    }

    public double getAverageSUSize() {
        return averageSUSize;
    }

    public int getMaxStorageUnits() {
        return maxStorageUnits;
    }

    public int getMinStorageUnits() {
        return minStorageUnits;
    }

    public int[] getProcessingUnitValues() {
        return processingUnitValues;
    }

    public int getAveragePUValue() {
        return averagePUValue;
    }

    public RoutesContainer getRoutesContainer() {
        return routesContainer;
    }

    public void setRoutesContainer(RoutesContainer routesContainer) {
        this.routesContainer = routesContainer;
    }

    public static DateTimeFormatter getFormatter() {
	return formatter;
    }
    // </editor-fold>
    
    public void setLoad(Integer load) {
	this.load = load;
	/**
	 * TODO
	 * validate inter arrival time
	 */
	meanConnectionInterArrivalTime = 1 / (load / meanConnectionHoldingTime);
    }
    
    public Logger getLogger() {
        if (logger == null)
            logger = LogManager.getLogger(id);
        return logger;
    }
    
}
