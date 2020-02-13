package simulator.cs.anycast.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author carlosnatalino
 */
public class SimulatorThreadFactory implements ThreadFactory {

    static final AtomicInteger poolNumber = new AtomicInteger(1);
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final String namePrefix;
    private static final Logger logger = LogManager.getLogger(SimulatorThreadFactory.class);

    public SimulatorThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        SimulatorThread thread = new SimulatorThread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        
        
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
	    @Override
	    public void uncaughtException(Thread t, Throwable e) {
		SimulatorThread thread = (SimulatorThread) t;
		logger.error("An uncaught exception was thrown in thread " + t.getName() + " from experiment " + thread.getConfiguration().getId() + " at simulation time " + thread.getSimulator().getCurrentTime(), e);
	    }
	});
        
        return thread;
    }
    
}
