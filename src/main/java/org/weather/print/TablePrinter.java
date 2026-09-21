package org.weather.print;

import org.weather.model.WeatherResponse;
import org.weather.properties.WeatherParameterRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TablePrinter {

    private TablePrinter() {
    }

    /**
     * Prints table: rows — cities, columns — weather forecast parameters.
     *
     * @param parameters список параметров (колонок) в нужном порядке
     * @param results    карта «город → ответ API», порядок сохраняется
     */
    public static void print(List<String> parameters, Map<String, WeatherResponse> results) {

        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<String, WeatherResponse> entry : results.entrySet()) {
            List<String> row = new ArrayList<>();
            row.add(entry.getKey()); // first column - city

            for (String param : parameters) {
                String value = extractValue(param, entry.getValue());
                row.add(value);
            }
            rows.add(row);
        }

        List<String> header = new ArrayList<>();
        header.add("City");
        header.addAll(parameters);

        int[] widths = columnWidths(header, rows);

        String separator = buildSeparator(widths);
        System.out.println(separator);
        System.out.println(formatRow(header, widths));
        System.out.println(separator);
        for (List<String> row : rows) {
            System.out.println(formatRow(row, widths));
        }
        System.out.println(separator);
    }

    private static String extractValue(String param, WeatherResponse response) {
        Function<WeatherResponse, Object> extractor =
                WeatherParameterRegistry.extractorFor(param)
                        .orElseThrow(() -> new IllegalStateException(
                                "Unsupported parameter reached TablePrinter: " + param));
        try {
            Object value = extractor.apply(response);
            return value == null ? "n/a" : value.toString();
        } catch (Exception e) {
            return "err";
        }
    }

    private static int[] columnWidths(List<String> header, List<List<String>> rows) {
        int[] widths = new int[header.size()];
        for (int i = 0; i < header.size(); i++) {
            widths[i] = header.get(i).length();
        }
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }
        return widths;
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String formatRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < cells.size(); i++) {
            sb.append(" ").append(padRight(cells.get(i), widths[i])).append(" |");
        }
        return sb.toString();
    }

    private static String padRight(String s, int width) {
        return s + " ".repeat(Math.max(0, width - s.length()));
    }
}