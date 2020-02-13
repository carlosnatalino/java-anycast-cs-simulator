package simulator.cs.anycast.utils;

import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.core.Simulator;
import simulator.cs.anycast.core.StatisticsMonitor;

/**
 *
 * Class that extends the standard Thread class from Java to include some specific
 * functionalities useful for the simulation, such as to hold the configuration
 * to be considered for this particular experiment, as well as a reference to the
 * statistics object.
 * 
 * @author carlosnatalino
 */
public class SimulatorThread extends Thread {
    
    private Configuration configuration;
    private Simulator simulator;
    private StatisticsMonitor statisticsMonitor;

    public SimulatorThread(ThreadGroup g, Runnable r, String name, int p) {
        super(g, r, name, p);
        if (this.isDaemon())
            this.setDaemon(false);
        this.setPriority(Thread.MAX_PRIORITY);
        this.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("Thread " + t.getName() + " has an exception: ");
                e.printStackTrace();
                System.err.flush();
            }
        });
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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
    
}
