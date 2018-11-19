package nl.hsac.fitnesse.junit.reportmerge.writer;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Helper to use Google Chart.
 */
public class ChartWriter {
    protected final PrintWriter pw;

    public ChartWriter(PrintWriter pw) {
        this.pw = pw;
    }

    public void writeLoadScriptTag() {
        pw.write("<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>");
    }

    public <T> void writeChartGenerators(List<T> htmls,
                                         BiConsumer<ChartWriter, List<T>> bodyFunction,
                                         String extraJs) {
        pw.write("<script type='text/javascript'>" +
                "if(window.google){google.charts.load('current',{'packages':['corechart']});" +
                "google.charts.setOnLoadCallback(drawChart);" +
                "function drawChart() {");
        bodyFunction.accept(this, htmls);
        pw.write("};");
        pw.write(extraJs);
        pw.write("}</script>");
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
                                           Function<T, Object> valueFunction,
                                           Iterable<T> groups) {
        String dataArray = createDataArray("'Group',''", keyFunction, valueFunction, groups);

        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Data = google.visualization.arrayToDataTable(");
        pw.write(dataArray);
        pw.write(");");

        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Chart = new google.visualization.PieChart(document.getElementById('");
        pw.write(chartElementId);
        pw.write("'));");
        pw.write(chartElementId);
        pw.write("Chart.draw(");
        pw.write(chartElementId);
        pw.write("Data");
        pw.write(",{title:'");
        pw.write(title);
        pw.write("',sliceVisibilityThreshold:0,is3D:true,pieSliceTextStyle:{color:'black'}");
        pw.write(extraOptions);
        pw.write("});");
    }

    public <T> void writeBarChartGenerator(String title,
                                           String chartElementId,
                                           String extraOptions) {
        pw.write("var xhr = new XMLHttpRequest();");
        pw.write("xhr.open('GET', 'test-results.json', true);");
        pw.write("xhr.responseType = 'json';");
        pw.write("xhr.onload = function() {");
        pw.write("var status = xhr.status;");
        pw.write("if (status === 200) {");

        pw.write("var testResults = xhr.response;");
        pw.write("xhr.response = null;");

        writeBarChartContentGenerator(title, chartElementId, extraOptions);

        pw.write("}};");
        pw.write("xhr.send();");
    }

    protected void writeBarChartContentGenerator(String title, String chartElementId, String extraOptions) {
        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Array = [['Test','Runtime (ms)']];");
        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Urls = [];");

        pw.write("for (var index = 0; index < testResults.length; ++index) {");
        pw.write("var testResult = testResults[index];");
        pw.write("if (!testResult.overviewPage) {");
        pw.write(chartElementId);
        pw.write("Array.push([testResult.testName, testResult.time]);");
        pw.write(chartElementId);
        pw.write("Urls.push(testResult.relativePath);");
        pw.write("}}");
        pw.write("testResults = null;");

        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Data = google.visualization.arrayToDataTable(");
        pw.write(chartElementId);
        pw.write("Array);");
        pw.write(chartElementId);
        pw.write("Array = null;");

        pw.write("var ");
        pw.write(chartElementId);
        pw.write("Chart = new google.visualization.ColumnChart(document.getElementById('");
        pw.write(chartElementId);
        pw.write("'));");
        pw.write(chartElementId);
        pw.write("Chart.draw(");
        pw.write(chartElementId);
        pw.write("Data");
        pw.write(",{title:'");
        pw.write(title);
        pw.write("',bar: {groupWidth: '80%'},legend:{position: 'none'}");
        pw.write(extraOptions);
        pw.write("});");

        pw.write(chartElementId);
        pw.write("Data = null;");

        writeColumnClickListener(chartElementId);
    }

    protected void writeColumnClickListener(String chartElementId) {
        pw.write("google.visualization.events.addListener(");
        pw.write(chartElementId);
        pw.write("Chart,'select',function myClickHandler(event) {");
        pw.write("var selection = ");
        pw.write(chartElementId);
        pw.write("Chart.getSelection();");
        pw.write("for (var i = 0; i < selection.length; i++) {");
        pw.write("var item = selection[i];");
        pw.write("if (item.row != null && item.column != null) {");
        pw.write("window.location=");
        pw.write(chartElementId);
        pw.write("Urls[item.row];");
        pw.write("}}});");
    }

    protected <T> String createDataArray(String firstElement, Function<T, String> keyFunction, Function<T, Object> valueFunction, Iterable<T> groups) {
        StringBuilder data = new StringBuilder("[[");
        data.append(firstElement);
        data.append("],");
        groups.forEach(r -> {
            data.append("['");
            data.append(keyFunction.apply(r));
            data.append("',");
            data.append(valueFunction.apply(r));
            data.append("],");
        });
        data.append("]");
        return data.toString();
    }

    protected static <T> List<T> sortBy(Collection<T> values, Function<T, ? extends Comparable> function) {
        return values.stream().sorted((o1, o2) -> function.apply(o1).compareTo(function.apply(o2)))
                .collect(Collectors.toList());
    }
}
