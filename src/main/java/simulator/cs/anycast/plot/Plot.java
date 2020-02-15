package simulator.cs.anycast.plot;

import java.io.IOException;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.ChartTheme;

/**
 *
 * CLASS UNDER DEVELOPMENT. WILL BE COMPLETED LATER.
 * 
 * @author carlosnatalino
 */
public class Plot {
    public static void main(String[] args) throws IOException {
        double[] xData = new double[]{0.01, 1.0, 2.0};
        double[] yData = new double[]{2.0, 1.0, 0.01};

        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
        chart.getStyler().setTheme(ChartTheme.Matlab.newInstance(ChartTheme.Matlab));
        
        chart.addSeries("line 2",xData, new double[]{4.0, 2.0, 1.0});
        
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
       
        chart.getStyler().setYAxisLogarithmic(true);
        
        chart.getStyler().setMarkerSize(0);
        // Show it
        new SwingWrapper(chart).displayChart();
        
        VectorGraphicsEncoder.saveVectorGraphic(chart, "./data/Sample_Chart", VectorGraphicsFormat.SVG);
    }
}
