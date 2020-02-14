/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.cs.anycast.utils;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 *
 * @author carda
 */
public class TestSwing {
    
    public static void main(String[] args) {
        double[] xData = new double[] { 0.0, 1.0, 2.0 };
    double[] yData = new double[] { 2.0, 1.0, 0.0 };
 
    // Create Chart
    XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
 
    // Show it
    new SwingWrapper(chart).displayChart();
    }
    
}
