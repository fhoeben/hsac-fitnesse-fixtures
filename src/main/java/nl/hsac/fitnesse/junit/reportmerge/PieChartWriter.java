package nl.hsac.fitnesse.junit.reportmerge;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Helper to use Google Pie Chart.
 */
public class PieChartWriter {
    protected final PrintWriter pw;

    public PieChartWriter(PrintWriter pw) {
        this.pw = pw;
    }

    public void writeLoadScriptTag() {
        pw.write("<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>");
    }

    public <T> void writeChartGenerators(List<T> htmls,
                                     BiConsumer<PieChartWriter, List<T>> bodyFunction) {
        pw.write("<script type='text/javascript'>" +
                "if(window.google){google.charts.load('current',{'packages':['corechart']});" +
                "google.charts.setOnLoadCallback(drawChart);" +
                "function drawChart() {");
        bodyFunction.accept(this, htmls);
        pw.write("}}</script>");
    }

    public <T> void writePieChartGenerator(String title,
                                       String chartId,
                                       List<T> values,
                                       Function<T, String> keyFunction,
                                       Collector<T, ?, Long> groupValueCollector) {
        List<Map.Entry<String, Long>> sums = sortBy(
                values.stream()
                        .collect(Collectors.groupingBy(
                                keyFunction,
                                groupValueCollector))
                        .entrySet(),
                r -> r.getKey());
        writePieChartGenerator(title, chartId,
                "",
                r -> r.getKey(), r -> r.getValue(), sums);
    }

    public <T> void writePieChartGenerator(String title,
                                           String chartElementId,
                                           String extraOptions,
                                           Function<T, String> keyFunction,
                                           Function<T, Number> valueFunction,
                                           Iterable<T> groups) {
        StringBuilder data = new StringBuilder("[['Group',''],");
        groups.forEach(r -> {
            data.append("['");
            data.append(keyFunction.apply(r));
            data.append("',");
            data.append(valueFunction.apply(r));
            data.append("],");
        });
        data.append("]");
        String dataArray = data.toString();

        pw.write("new google.visualization.PieChart(document.getElementById('");
        pw.write(chartElementId);
        pw.write("')).draw(google.visualization.arrayToDataTable(");
        pw.write(dataArray);
        pw.write("),{title:'");
        pw.write(title);
        pw.write("',sliceVisibilityThreshold:0,pieSliceTextStyle:{color:'black'}");
        pw.write(extraOptions);
        pw.write("});");
    }

    protected static <T> List<T> sortBy(Collection<T> values, Function<T, ? extends Comparable> function) {
        return values.stream().sorted((o1, o2) -> function.apply(o1).compareTo(function.apply(o2)))
                .collect(Collectors.toList());
    }
}
