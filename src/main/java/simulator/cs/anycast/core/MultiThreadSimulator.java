package simulator.cs.anycast.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import simulator.cs.anycast.plot.Plot;
import simulator.cs.anycast.utils.SimulatorThreadFactory;

/**
 *
 * Main class of this project. This class is responsible for:
 * 1. Load the configuration from the .conf file
 * 2. Create the folder structure of the simulation
 * 3. Generate all the simulation scenarios to be executed
 * 4. Create the thread pool that will execute the scenarios
 * 5. Consolidate statistics and write final file
 * 
 * @author carlosnatalino
 */
public class MultiThreadSimulator {
    
    private static Logger logger;
    
    public static final String configFile = "resources" + File.separator + 
                                            "config" + File.separator + 
                                            "simulation.conf";
    
    static {
        Locale.setDefault(new Locale("en", "US"));
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"));
        Config mainConfig = ConfigFactory.parseFile(new File(configFile));
        System.setProperty("log4j.configurationFile", "resources" + File.separator + 
                                            "config" + File.separator + 
                                            "log4j2.xml");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Config mainConfig = null;
        try {
            // verifying if all the files are in place
            // configuration file
            mainConfig = ConfigFactory.parseFile(new File("resources" + File.separator + 
                                            "config" + File.separator + 
                                            "simulation.conf"));
            mainConfig.getInt("simulation.seed");
            
            // topology file
            String path = "resources" + File.separator + 
                                            "topologies" + File.separator + 
                                            mainConfig.getString("simulation.topology");
            FileAgent.readFile(path);
        }
        catch (Exception e) {
            System.err.println("Configuration file not found in: resources/config/simulation.conf.");
            System.err.println("Make sure that the file has the correct configuration.");
            System.err.println("Make sure you have all your configuration and topology files in place before continuing.");
            System.exit(2);
        }
        
        try {
            // topology file
            String path = "resources" + File.separator + 
                                            "topologies" + File.separator + 
                                            mainConfig.getString("simulation.topology");
            FileAgent.readFile(path);
        }
        catch (Exception e) {
            System.err.println("Topology file not found in: " + 
                    "resources/topologies/" + mainConfig.getString("simulation.topology"));
            System.err.println("Make sure that the file has the correct syntax.");
            System.err.println("Make sure you have all your configuration and topology files in place before continuing.");
            System.exit(3);
        }
        
        if (args.length > 0 && args[0] == "validate-config") {
            //TODO implement the config validation
            throw new UnsupportedOperationException("Not supported yet.");
        }
        else {
        
            try {
                ArrayList<Future<Boolean>> listInstances = new ArrayList<>(); // listInstances of all threads
                
                Configuration mainConf = FileAgent.getConfiguration(mainConfig); // reading the configuration file
                
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'UTC'");
                String dateTime = dtf.format(OffsetDateTime.now(ZoneOffset.UTC));
                
                // creating folder and copying configuration files
                mainConf.setBaseFolder(mainConf.getBaseFolder() + File.separator + dateTime + File.separator);
                new File(mainConf.getBaseFolder()).mkdirs();
                
                System.setProperty("logFileName", mainConf.getBaseFolder() + mainConfig.getString("simulation.suffix"));
                logger = LogManager.getLogger(MultiThreadSimulator.class);
                
                Files.copy(Paths.get(configFile), Paths.get(mainConf.getBaseFolder() + "simulation.conf"));
                
                dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                
                String text = "Date: " + dtf.format(LocalDateTime.now()) + "\n";
                text += "args (" + args.length + "):";
                
                for (String arg : args) {
                    text += " " + arg;
                }
                text += "\n";
                
                Path path = Paths.get(mainConf.getBaseFolder() + "0-info.txt");
                Files.write(path, text.getBytes(), StandardOpenOption.CREATE_NEW);
                
                // saves the current version used within the folder
                String base = MultiThreadSimulator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                boolean IS_WINDOWS = System.getProperty( "os.name" ).contains( "indow" );
                base = (IS_WINDOWS && base.startsWith("/")) ? base.substring(1) : base;
                if (base.endsWith(".jar")) {
                    Files.copy(Paths.get(base), Paths.get(mainConf.getBaseFolder() + "java-simulator.jar"));
                }
                else {
                    FileAgent.copyFolder(Paths.get(base), Paths.get(mainConf.getBaseFolder() + "java-simulator" + File.separator));
                }
                
                FileAgent.init(mainConf);
                
                RoutesContainer.init(mainConf); // starting the routing container
                ExecutorService service = Executors.newFixedThreadPool(mainConf.getNumberThreads(), new SimulatorThreadFactory());
                
                String baseName;
                logger.debug("Starting simulation at " + Configuration.getFormatter().format(LocalDateTime.now()));
                
                for (int s = 0; s < mainConf.getPolicies().length; s++) {

                    for (int l = 0; l < mainConf.getLoads().length; l++) {

                        Configuration conf = FileAgent.getConfiguration(mainConfig);
                        conf = FileAgent.getConfiguration(mainConfig);
                        conf.setBaseFolder(mainConf.getBaseFolder());
                        conf.setPolicy(conf.getPolicies()[s]);

                        conf.setLoad(conf.getLoads()[l]);

                        RoutesContainer.init(conf);

                        baseName = "sim-" + conf.getPolicies()[s] + "-" + conf.getLoads()[l];

                        conf.setBaseName(baseName);
                        listInstances.add(service.submit(new Simulator(conf)));

                    }
                }
                
                //	listInstances.add(service.submit(new Simulator(mainConf)));
                //	listInstances.add(service.submit(new Simulator(conf2)));
                
                // watching the thread pool and plotting results during the simulation
                while (true) {
                    // are all the jobs done?
                    if (!listInstances.stream().filter(i -> !i.isDone()).findAny().isPresent())
                        break;
                    Thread.sleep(1000);
                    /**
                     * TODO
                     * make a configurable parameter out of the plot_every
                     */
                    Plot.saveDuring(mainConf);
                }
                
                service.shutdown();
                logger.info("Finishing simulation at " + Configuration.getFormatter().format(LocalDateTime.now()));
                logger.info("Generating charts ...");
                Plot.saveFinal(mainConf);
                logger.info("All done :)");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MultiThreadSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
