package simulator.cs.anycast.components;

import java.util.ArrayList;

/**
 *
 * Class that models a route in the network. It contains the links traversed
 * by the route, and also provides methods to extract minimum and maximum loads
 * in the links which are traversed by it.
 * 
 * @author carlosnatalino
 */
public class OpticalRoute {
    
    private ArrayList<Link> routeLinks;
    private int source = -1, destination = -1;//, cpu = -1;
    private double weight = 0.0;
    
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
    
    public void addLink(Link link) {
        routeLinks.add(link);
        weight += link.getWeight();
    }
    
    public int getHopCount() {
	return routeLinks.size();
    }

    public double getWeight() {
        return weight;
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
    
    public double getLoad() {
        double load = Integer.MIN_VALUE;
        for (Link link : routeLinks)
            load = Math.max(load, link.getLoad());
        return load;
    }

    public ArrayList<Link> getRouteLinks() {
        return routeLinks;
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
		    string += " -(r: " + link.getFreeWavelengths() + ", l: " + link.getWeight() + ")> " + link.getDestination();
		    last = link.getDestination();
		    counter++;
		}
		else if (first && link.getDestination()== source) {
		    first = false;
		    string += " -(r: " + link.getFreeWavelengths() + ", l: " + link.getWeight() + ")> " + link.getSource();
		    last = link.getSource();
		    counter++;
		}
		else {
		    if (last == link.getSource()) {
			string += " -(r: " + link.getFreeWavelengths() + ", l: " + link.getWeight() + ")> " + link.getDestination();
			last = link.getDestination();
			counter++;
		    } else if (last == link.getDestination()) {
			string += " -(r: " + link.getFreeWavelengths() + ", l: " + link.getWeight() + ")> " + link.getSource();
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
