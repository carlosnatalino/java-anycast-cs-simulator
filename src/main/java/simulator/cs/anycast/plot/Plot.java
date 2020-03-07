package simulator.cs.anycast.plot;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.VectorGraphicsEncoder;
import org.knowm.xchart.VectorGraphicsEncoder.VectorGraphicsFormat;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.ChartTheme;
import simulator.cs.anycast.core.Configuration;
import simulator.cs.anycast.core.FileAgent;
import simulator.cs.anycast.core.MultiThreadSimulator;
import simulator.cs.anycast.core.StatisticsMonitor;

/**
 *
 * CLASS UNDER DEVELOPMENT. WILL BE COMPLETED LATER.
 *
 * @author carlosnatalino
 */
public class Plot {

    private static String finalDir = "final-plots";
    private static String duringDir = "current-plots";
    private static String dir;
    private static List<String> dataFinal;
    private static List<String> dataDuring;

    private static final double FIG_SIZE_X = 8.0;
    private static final double FIG_SIZE_Y = 4.0;
    private static final int FONT_SIZE = 25;
    private static final int LEGEND_FONT_SIZE = 18;
    private static final Color[] PALLET_COLORS = new Color[]{
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
                                                    };

    public static void saveFinal(Configuration configuration) {
        try {
            dataFinal = Files.readAllLines(Paths.get(configuration.getBaseFolder() + "results-avg-" + configuration.getSuffix() + ".csv"));
            List<String> chart = Arrays.asList(StatisticsMonitor.headersAverageFile)
                    .stream()
                    .filter(item-> !item.equals("policy"))
                    .filter(item-> !item.equals("load"))
                    .collect(Collectors.toList());
            dir = configuration.getBaseFolder() + finalDir + File.separator;
            new File(dir).mkdirs();
            chart.stream().parallel().forEach(item -> Plot.plotXY(dataFinal, item));
        } catch (IOException ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    public static void saveDuring(Configuration configuration) {
        try {
            dataDuring = Files.readAllLines(Paths.get(configuration.getBaseFolder() + "results-" + configuration.getSuffix() + ".csv"));
            dataDuring = getAverageValues(dataDuring);
            List<String> chart = Arrays.asList(StatisticsMonitor.headersExperimentFile)
                    .stream()
                    .filter(item-> !item.equals("policy"))
                    .filter(item-> !item.equals("load"))
                    .filter(item-> !item.equals("exp"))
                    .collect(Collectors.toList());
            dir = configuration.getBaseFolder() + duringDir + File.separator;
            new File(dir).mkdirs();
            chart.stream().parallel().forEach(item -> Plot.plotXY(dataDuring, item));
        } catch (IOException ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    /**
     * Method that receives a list with the lines of the individual experiments
     * result file and averages the statistics for every policy and load.
     * @param data: The list of lines of the file
     * @return List of averaged statistics per policy and load
     */
    private static List<String> getAverageValues(List<String> data) {
        List<String> header = Arrays.asList(data.get(0).split(FileAgent.columnSeparator));
        List<String> policies = getPolicies(data);
        List<Double> loads = getLoads(data);
        List<String> chart = Arrays.asList(StatisticsMonitor.headersExperimentFile)
                    .stream()
                    .filter(item-> !item.equals("policy"))
                    .filter(item-> !item.equals("load"))
                    .filter(item-> !item.equals("exp"))
                    .collect(Collectors.toList());
        ArrayList<String> returnValues = new ArrayList<>();
        returnValues.add(Arrays.asList(StatisticsMonitor.headersExperimentFile)
                    .stream().filter(s -> !s.equals("exp")).collect(Collectors.joining(FileAgent.columnSeparator)));
        for (String policy : policies) {
            for (Double load : loads) {
                String lineValue = policy + FileAgent.columnSeparator + load;
                for (String column : chart) {
                    int columnIndex = header.indexOf(column);
                    OptionalDouble optAvg = data.stream()
                            .skip(1)
                            .filter(s -> s.contains(policy) && s.contains(FileAgent.columnSeparator + load))
                            .map(line -> Arrays.asList(line.split(",")))
                            .map(list -> list.get(columnIndex))
                            .filter(Objects::nonNull)
                            .filter(s -> s.trim().length() > 0)
                            .mapToDouble(Double::parseDouble)
                            .average();
                    if (!optAvg.isPresent())
                        lineValue += FileAgent.columnSeparator + 0.0;
                    else
                        lineValue += FileAgent.columnSeparator + optAvg.getAsDouble();
                }
                returnValues.add(lineValue);
            }
        }
        return returnValues;
    }
    
    private static List<Double> getPolicyDataOrderBy(List<String> data, String policy, String column, String orderBy) {
        List<String> header = Arrays.asList(data.get(0).split(FileAgent.columnSeparator));
        int columnIndex = header.indexOf(column);
        int orderByIndex = header.indexOf(orderBy);
        List<Double> resultSet = data.stream()
                .skip(1)
                .filter(s -> s.contains(policy))
                .sorted((a, b) -> Double.compare(
                    Double.valueOf(Arrays.asList(a.split(FileAgent.columnSeparator)).get(orderByIndex)),
                    Double.valueOf(Arrays.asList(b.split(FileAgent.columnSeparator)).get(orderByIndex)))
                )
                .map(line -> Arrays.asList(line.split(",")))
                .map(list -> list.get(columnIndex))
                .filter(Objects::nonNull)
                .filter(s -> s.trim().length() > 0)
                .mapToDouble(Double::parseDouble)
                .boxed()
                .collect(Collectors.toList());
        if (resultSet.isEmpty())
            resultSet.add(0.0);
        return resultSet;
    }

    private static List<String> getPolicies(List<String> data) {
        List<String> header = Arrays.asList(data.get(0).split(FileAgent.columnSeparator));
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

    private static List<Double> getLoads(List<String> data) {
        List<String> header = Arrays.asList(data.get(0).split(FileAgent.columnSeparator));
        int loadIndex = header.indexOf("load");
        List<Double> loads = data.stream()
                .skip(1)
                .map(line -> Arrays.asList(line.split(FileAgent.columnSeparator)))
                .map(list -> list.get(loadIndex))
                .filter(Objects::nonNull)
                .filter(s -> s.trim().length() > 0)
                .mapToDouble(Double::valueOf)
                .boxed()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return loads;
    }

    private static void plotXY(List<String> data, String column) {

        try {
            List<String> policies = getPolicies(data);
            List<Double> loads = getLoads(data);
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
            chart.getStyler().setMarkerSize(10);
            policies.stream()
                    .forEach(item -> chart.addSeries(item, loads, getPolicyDataOrderBy(data, item, column, "load")));
            chart.getSeriesMap().values().stream().forEach(item->item.setLineWidth(3));
            VectorGraphicsEncoder.saveVectorGraphic(chart, dir + File.separator + column + ".svg", VectorGraphicsFormat.SVG);
            BitmapEncoder.saveBitmap(chart, dir + File.separator + column + ".png", BitmapFormat.PNG);
        } catch (IOException ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    public static void main(String[] args) {
        try {
            Config mainConfig = ConfigFactory.parseFile(new File(MultiThreadSimulator.configFile));
            Configuration mainConf = FileAgent.getConfiguration(mainConfig); // reading the configuration file

            File[] directories = new File(mainConf.getBaseFolder()).listFiles(File::isDirectory);
            String lastDir = directories[directories.length - 1].getPath(); // get latest folder created
            mainConf.setBaseFolder(lastDir + File.separator);
            Plot.saveDuring(mainConf);
            Plot.saveFinal(mainConf);
        } catch (Exception ex) {
            Logger.getLogger(Plot.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
