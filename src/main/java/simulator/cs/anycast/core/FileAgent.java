package simulator.cs.anycast.core;

import com.typesafe.config.Config;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import simulator.cs.anycast.components.Link;
import simulator.cs.anycast.components.Topology;

/**
 *
 * Class that concentrates all the operations of read/write from/to files.
 * 
 * @author carlosnatalino
 */
public class FileAgent {
    
    private static String[] files = new String[]{"results-avg-"};
    private static HashMap<String, ArrayList<String>> readFiles;
    private static String configFileName = "config/exp.txt";
    public static DecimalFormat format;
    public static DecimalFormat timeFormat;
    
    static {
	format = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	format.setMinimumFractionDigits(3);
	format.setMaximumFractionDigits(10);
	timeFormat = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
//	timeFormat.setMinimumFractionDigits(3);
	timeFormat.setMaximumFractionDigits(0);
    }
    
    public static void init(Configuration configuration) {
        try {
            for (String file : files) {
                Path path = Paths.get(configuration.getBaseFolder() + file + configuration.getSuffix()+ ".csv");
                if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
                    Files.createFile(path);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Problem when creating files for the experiment " + configuration.getId());
        }
    }
    
    public static Configuration getConfiguration(Config mainConfig) {
	Configuration config = new Configuration(mainConfig.getInt("simulation.seed"));
        
        config.setSuffix(mainConfig.getString("simulation.suffix"));
        
        config.setBaseFolder(mainConfig.getString("simulation.folder"));
        
        List<Integer> l = mainConfig.getIntList("simulation.strategies");
        Configuration.Policy[] strategies = new Configuration.Policy[l.size()];
	for (int i = 0; i < l.size() ; i++) {
            strategies[i] = Configuration.Policy.fromInteger(l.get(i));
        }
        config.setStrategies(strategies);
        
        config.setRhoProcessing(mainConfig.getDouble("simulation.rho-processing"));
        
        config.setRhoStorage(mainConfig.getDouble("simulation.rho-storage"));
	
	if ("range".equals(mainConfig.getString("simulation.load.type").toLowerCase())) {
            Integer init = mainConfig.getInt("simulation.load.min");
	    Integer step = mainConfig.getInt("simulation.load.step");
	    Integer last = mainConfig.getInt("simulation.load.max");
	    int diff = (last-init)/step;
	    Integer[] intValues = new Integer[diff+1];
            intValues[0] = init;
	    for (int i = 1 ; i < intValues.length ; i++)
		intValues[i] = init + step * i;
            config.setLoads(intValues);
        }
        else {
            //TODO implement new load configurations
            System.err.println("Load method not implemented!");
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        config.setWavelengthsPerFiber(mainConfig.getInt("simulation.resources-per-link"));

        config.setNumberThreads(mainConfig.getInt("simulation.threads"));
        
        config.setExperiments(mainConfig.getInt("simulation.experiments"));

        config.setNumberArrivals(mainConfig.getInt("simulation.arrivals"));
        
        config.setIgnoreFirst(mainConfig.getInt("simulation.ignore-first"));
        
        String topologyFile = mainConfig.getString("simulation.topology");
        
        config.setTopologyName(topologyFile.replace(".xml", "").replace(".txt", ""));
        
        Topology topo = readTopology(topologyFile, config);
        
        config.setTopology(topo);
	
	return config;
    }
    
    public static ArrayList<String> readFile(String file) {
	if (readFiles == null)
	    readFiles = new HashMap<>();
	if (readFiles.containsKey(file))
	    return readFiles.get(file);
	
	ArrayList<String> array = new ArrayList<>();
	try {
	    try (Stream<String> lines = Files.lines(Paths.get(file)).filter(s -> !s.startsWith("#"));) {
		for (Object o : lines.toArray())
		    array.add(o.toString());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	readFiles.put(file, array);
	return array;
    }
    
    private static Topology readTopology(String topologyName, Configuration configuration) {
        String path = "resources/topologies/" + topologyName;
        Topology topology = null;
        
        if (topologyName.endsWith(".txt")) {
        
            ArrayList<String> lines = readFile(path);

            int nNodes = Integer.parseInt(lines.get(0));
            int[] nodeDegree = new int[nNodes];
            int nLinks = Integer.parseInt(lines.get(1));
            int nDatacenters = Integer.parseInt(lines.get(2));

            topology = new Topology(nNodes, nLinks, nDatacenters, configuration);

    //        System.out.println("nLinks: " + nLinks);
            int node0;
            int node1;
            for (int link = 0 ; link < nLinks ; link++) { 
                String[] nodes = lines.get(link + 3).split(" ");
                node0 = Integer.parseInt(nodes[0])-1;
                node1 = Integer.parseInt(nodes[1])-1;

                nodeDegree[node0]++;
                nodeDegree[node1]++;

                topology.getLinks()[link].setSource(node0);
                topology.getLinks()[link].setSourceNode(topology.getNodes()[node0]);
                topology.getLinks()[link].setDestination(node1);
                topology.getLinks()[link].setDestinationNode(topology.getNodes()[node1]);
                topology.getLinks()[link].setWeight(1.0);

                topology.getNodes()[node0].getLinks().add(topology.getLinks()[link]);
                topology.getNodes()[node1].getLinks().add(topology.getLinks()[link]);

                topology.getLinksVector()[node0][node1] = topology.getLinks()[link];
                topology.getLinksVector()[node1][node0] = topology.getLinks()[link];

            }

            String[] dcs = lines.get(nLinks + 3).split(" ");

            for (String dc : dcs) {
                int i = Integer.parseInt(dc);
                topology.getDatacenters()[i-1] = true;
                topology.getNodes()[i-1].setDatacenter(true);
            }
        }
        else if (topologyName.endsWith(".xml")) {
            //TODO read from SNDlib
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        for (Link link : topology.getLinks()) {
            if (topology.getNodes()[link.getSource()].isDatacenter() || topology.getNodes()[link.getDestination()].isDatacenter())
                link.setDCLink(true);
        }
	        
        return topology;
    }
    
    /**
     * Method to write the results for one single experiment.
     * It uses file lock to prevent multiple writes at the same time
     * @param configuration
     * @param results 
     */
    public static void reportExperimentStatistics(Configuration configuration, ArrayList<Double> results) {
	try {
            Path path = Paths.get(configuration.getBaseFolder() + "results-" + configuration.getPolicy() + "-" + configuration.getSuffix() + ".csv");
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
                Files.createFile(path);
            
            String text = configuration.getId() + "\t" + configuration.getExperiment();
            for (Double res : results)
		text += "\t" + res;
            text += "\n";
            
            Files.write(path, text.getBytes(), StandardOpenOption.APPEND);
            
	} catch (Exception ex) {
	    ex.printStackTrace();
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Problem when creating experiment statistics files for the experiment " + configuration.getId());
	}
	
    }
    
    /**
     * Method to write the final results for one experiment.
     * It uses file lock to prevent multiple writes at the same time
     * @param configuration
     * @param results 
     */
    public static void reportFinalStatistics(Configuration configuration, ArrayList<BigDecimal> results) {
	try {
            
            String text = configuration.getBaseName();
            for (BigDecimal res : results)
		text += "\t" + format.format(res);
            text += "\n";
            
            Files.write(Paths.get(configuration.getBaseFolder() + "results-avg-" + configuration.getSuffix()+ ".csv"), text.getBytes(), StandardOpenOption.APPEND);
            
	} catch (Exception ex) {
	    ex.printStackTrace();
            throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "] Problem when creating final statistics files for the experiment " + configuration.getId());
	}
	
    }
    
}
