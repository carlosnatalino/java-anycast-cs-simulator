package simulator.cs.anycast.plot;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.core.StatisticsMonitor;

/**
 *
 * CLASS UNDER DEVELOPMENT. WILL BE COMPLETED LATER.
 *
 * @author carlosnatalino
 */
public class Plot {

    private static String dir;
    private static List<String> data;

    private static final double FIG_SIZE_X = 8.0;
    private static final double FIG_SIZE_Y = 4.0;
    private static final int FONT_SIZE = 25;
    private static final int LEGEND_FONT_SIZE = 18;
    private static final Color[] PALLET_COLORS = (Color[])Arrays.asList(
                    new Color(31, 119, 180),
                    new Color(255, 127, 14),
                    new Color(44, 160, 44),
                    new Color(214, 39, 40),
                    new Color(148, 103, 189),
                    new Color(140, 86, 75),
                    new Color(227, 119, 194),
                    new Color(127, 127, 127),
                    new Color(188, 189, 34),
                    new Color(23, 190, 207)
                    ).toArray();

    public static void save(Configuration configuration) {
        try {
            data = Files.readAllLines(Paths.get(configuration.getBaseFolder() + "results-avg-" + configuration.getSuffix() + ".csv"));
            List<String> chart = Arrays.asList(StatisticsMonitor.headersAverageFile)
                    .stream()
                    .filter(item-> !item.equals("policy"))
                    .filter(item-> !item.equals("load"))
                    .collect(Collectors.toList());
            dir = configuration.getBaseFolder();
            chart.stream().parallel().forEach(item -> Plot.plotXY(item));
            
        } catch (IOException ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    private static List<Double> getPolicyDataOrderBy(String policy, String column, String OrderBy) {
        List<String> header = Arrays.asList(data.get(0).split(","));
        int columnIndex = header.indexOf(column);
        int orderByIndex = header.indexOf(OrderBy);
        List<Double> resultSet = data.stream()
                .skip(1)
                .filter(s -> s.contains(policy))
                .sorted((a, b) -> Double.compare(
                Double.valueOf(Arrays.asList(a.split(",")).get(orderByIndex)),
                Double.valueOf(Arrays.asList(b.split(",")).get(orderByIndex)))
                )
                .map(line -> Arrays.asList(line.split(",")))
                .map(list -> list.get(columnIndex))
                .filter(Objects::nonNull)
                .filter(s -> s.trim()
                .length() > 0)
                .mapToDouble(Double::parseDouble)
                .boxed()
                .collect(Collectors.toList());
        return resultSet;
    }

    private static List<String> getPolicies() {
        List<String> header = Arrays.asList(data.get(0).split(","));
        int policyIndex = header.indexOf("policy");
        List<String> policies = data.stream()
                .skip(1)
                .map(line -> Arrays.asList(line.split(",")))
                .map(list -> list.get(policyIndex))
                .filter(Objects::nonNull)
                .filter(s -> s.trim()
                .length() > 0)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return policies;
    }

    private static List<Double> getLoads() {
        List<String> header = Arrays.asList(data.get(0).split(","));
        int loadIndex = header.indexOf("load");
        List<Double> loads = data.stream()
                .skip(1)
                .map(line -> Arrays.asList(line.split(",")))
                .map(list -> list.get(loadIndex))
                .filter(Objects::nonNull)
                .filter(s -> s.trim()
                .length() > 0)
                .mapToDouble(Double::valueOf)
                .boxed()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return loads;
    }

    private static void plotXY(String column) {

        try {
            List<String> policies = getPolicies();
            List<Double> loads = getLoads();
            XYChart chart = new XYChartBuilder().width((int) FIG_SIZE_X * 96).height((int) FIG_SIZE_Y * 96).theme(ChartTheme.Matlab).build();
            
            chart.getStyler().setLegendVisible(true);
            chart.getStyler().setMarkerSize(0);
            chart.getStyler().setChartTitleFont(new Font("TimesRoman", Font.PLAIN, FONT_SIZE));
            chart.getStyler().setLegendFont(new Font("TimesRoman", Font.PLAIN, LEGEND_FONT_SIZE));
            chart.getStyler().setAxisTitleFont(new Font("TimesRoman", Font.PLAIN, FONT_SIZE));
            chart.getStyler().setAxisTickLabelsFont(new Font("TimesRoman", Font.PLAIN, FONT_SIZE));
            chart.setXAxisTitle("Load [Erlang]");
            chart.setYAxisTitle(column);
            chart.getStyler().setSeriesColors(PALLET_COLORS);
            policies.stream()
                    .forEach(item -> chart.addSeries(item, loads, getPolicyDataOrderBy(item, column, "load")));
            chart.getSeriesMap().values().stream().forEach(item->item.setLineWidth(3));
            VectorGraphicsEncoder.saveVectorGraphic(chart, dir + File.separator + column + ".svg", VectorGraphicsFormat.SVG);
        } catch (IOException ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.WARNING, null, ex);
        }
    }
}
