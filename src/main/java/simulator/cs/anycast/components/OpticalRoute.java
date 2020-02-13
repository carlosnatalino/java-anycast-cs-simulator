package simulator.cs.anycast.components;

import java.util.ArrayList;

/**
 *
 * @author carlosnatalino
 */
public class OpticalRoute {
    
    private int cost = -1;
    private ArrayList<Link> routeLinks;
    private int source = -1, destination = -1;//, cpu = -1;
    
    public OpticalRoute(int src, int dst) {
	this.source = src;
	this.destination = dst;
	routeLinks = new ArrayList<>();
    }
    
    public boolean isDisjoint(OpticalRoute route) {
        if (route == null)
            return true;
//        return routeLinks.stream().filter(route.getRouteLinks()::contains).count() == 0;
        for (Link link1 : routeLinks)
            for (Link link2 : route.getRouteLinks())
                if (link1.equals(link2))
                    return false;
        return true;
    }
    
    public int getHopCount() {
	return routeLinks.size();
    }
    
    public double getMeanUtilization() {
        return routeLinks.stream().mapToDouble(s -> s.getCurrentUtilization()).average().getAsDouble();
    }
    
    public double getMinUtilization() {
        return routeLinks.stream().mapToDouble(s -> s.getCurrentUtilization()).min().getAsDouble();
    }
    
    public double getMaxUtilization() {
        double max = Double.MIN_VALUE;
        for (Link link : routeLinks)
            max = Math.max(max, link.getCurrentUtilization());
        return max;
    }
    
    public int getFreeWavelengths() {
        int free = Integer.MAX_VALUE;
        for (Link link : routeLinks)
            free = Math.min(free, link.getFreeWavelengths());
        return free;
    }
    
    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public ArrayList<Link> getRouteLinks() {
        return routeLinks;
    }

    public void setRouteLinks(ArrayList<Link> routeLinks) {
        this.routeLinks = routeLinks;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

//    public int getCPU() {
//        return cpu;
//    }
//
//    public void setCPU(int cpu) {
//        if (destination == 5)
//            System.out.println("CPU changed from (5, " + this.cpu + " to (" + destination + ", " + cpu + ")");
//        this.cpu = cpu;
//    }

    @Override
    public String toString() {
        String string = "";
        string += source;
        boolean first = true;
        int last = -1, counter = 0;
	while (counter < routeLinks.size()) {
	    for (Link link : routeLinks) {
		if (first && link.getSource() == source) {
		    first = false;
		    string += " -(" + link.getFreeWavelengths() + ")> " + link.getDestination();
		    last = link.getDestination();
		    counter++;
		}
		else if (first && link.getDestination()== source) {
		    first = false;
		    string += " -(" + link.getFreeWavelengths() + ")> " + link.getSource();
		    last = link.getSource();
		    counter++;
		}
		else {
		    if (last == link.getSource()) {
			string += " -(" + link.getFreeWavelengths() + ")> " + link.getDestination();
			last = link.getDestination();
			counter++;
		    } else if (last == link.getDestination()) {
			string += " -(" + link.getFreeWavelengths() + ")> " + link.getSource();
			last = link.getSource();
			counter++;
		    }
		}
	    }
	}
	if (counter != routeLinks.size())
	    System.err.println("Route sizes does not match! " + string + " with links " + routeLinks);
//        string += ")";
        return string;
    }
    
}
