package simulator.cs.anycast.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
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
    
    private static String configFile = "resources/config/simulation.conf";
    
    static {
        Locale.setDefault(new Locale("en", "US"));
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"));
        Config mainConfig = ConfigFactory.parseFile(new File(configFile));
        System.setProperty("log4j.configurationFile", "resources/config/log4j2.xml");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        Config mainConfig = ConfigFactory.parseFile(new File("resources/config/simulation.conf"));
        
        if (args.length > 0 && args[0] == "validate-config") {
            //TODO implement the config validation
            throw new UnsupportedOperationException("Not supported yet.");
        }
        else {
        
            try {
                ArrayList<Future<Boolean>> listInstances = new ArrayList<>(); // listInstances of all threads
                
                Configuration mainConf = FileAgent.getConfiguration(mainConfig); // reading the configuration file
                
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS");
                String dateTime = dtf.format(LocalDateTime.now());
                
                // creating folder and copying configuration files
                mainConf.setBaseFolder(mainConf.getBaseFolder() + "/" + dateTime + "/");
                new File(mainConf.getBaseFolder()).mkdirs();
                
                System.setProperty("logFileName", mainConf.getBaseFolder() + mainConfig.getString("simulation.suffix"));
                logger = LogManager.getLogger(MultiThreadSimulator.class);
                
                Files.copy(Path.of(configFile), Path.of(mainConf.getBaseFolder() + "simulation.conf"));
                
                dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                
                String text = "Date: " + dtf.format(LocalDateTime.now()) + "\n";
                text += "args (" + args.length + "):";
                
                for (String arg : args) {
                    text += " " + arg;
                }
                text += "\n";
                
                Path path = Paths.get(mainConf.getBaseFolder() + "0-info.txt");
            
                Files.write(path, text.getBytes(), StandardOpenOption.CREATE_NEW);
                
                RoutesContainer.init(mainConf); // starting the routing container
                ExecutorService service = Executors.newFixedThreadPool(mainConf.getNumberThreads(), new SimulatorThreadFactory());
                
                String baseName;
                logger.debug("Starting simulation at " + Configuration.getFormatter().format(LocalDateTime.now()));
                
                for (int s = 0; s < mainConf.getStrategies().length; s++) {
                    
                    Configuration conf = FileAgent.getConfiguration(mainConfig);
                    conf.setBaseFolder(mainConf.getBaseFolder());
                    conf.setPolicy(conf.getStrategies()[s]);
                                
                    FileAgent.initScenario(conf);
                        
                    for (int l = 0; l < mainConf.getLoads().length; l++) {
                        
                        for (int rhoProcessing = 0; rhoProcessing < mainConf.getRhosProcessing().length; rhoProcessing++) {
                            for (int rhoStorage = 0; rhoStorage < mainConf.getRhosStorage().length; rhoStorage++) {
                                
                                conf = FileAgent.getConfiguration(mainConfig);
                                conf.setBaseFolder(mainConf.getBaseFolder());
                                conf.setPolicy(conf.getStrategies()[s]);
                                
                                conf.setLoad(conf.getLoads()[l]);
                                conf.setRhoProcessing(mainConf.getRhosProcessing()[rhoProcessing]);
                                conf.setRhoStorage(mainConf.getRhosStorage()[rhoStorage]);
                                
                                RoutesContainer.init(conf);
                                
                                baseName = "sim-" + conf.getStrategies()[s] + "-" + conf.getLoads()[l];
                                
                                if (mainConf.getRhosProcessing().length > 1) {
                                    baseName += "-" + mainConf.getRhosProcessing()[rhoProcessing];
                                }
                                
                                if (mainConf.getRhosStorage().length > 1) {
                                    baseName += "-" + mainConf.getRhosStorage()[rhoStorage];
                                }
                                
                                conf.setBaseName(baseName);
                                listInstances.add(service.submit(new Simulator(conf)));
                            }
                        }
                        
                    }
                }
                
                //	listInstances.add(service.submit(new Simulator(mainConf)));
                //	listInstances.add(service.submit(new Simulator(conf2)));
                for (int i = 0 ; i < listInstances.size() ; i++)
                    try {
                        if (listInstances.get(i).get() == true) logger.info(i + " done!"); else logger.info(i + " not done!");
                    } catch (Exception e) {
                        logger.error("Bad Exception, full stack following:", e);
                        e.printStackTrace();
                    }
                
                service.shutdown();
                logger.info("Finishing simulation at " + Configuration.getFormatter().format(LocalDateTime.now()));
                logger.info("All done :)");
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MultiThreadSimulator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
