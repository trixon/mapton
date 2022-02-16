/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mapton.ce_chartfx.api;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.CrosshairIndicator;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.plugins.TableViewer;
import de.gsi.chart.plugins.Zoomer;
import javafx.util.StringConverter;

/**
 *
 * @author Patrik Karlström
 */
public class ChartHelper {

    public static StringConverter<Number> createTickLabelFormatter(int decimals) {
        return new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String toString(Number t) {
                var format = String.format("%%.%df", decimals);
                return String.format(format, t.doubleValue());
            }
        };
    }

    public static XYChart createXYChart(final Axis... axes) {
        var chart = new XYChart(axes);
        chart.legendVisibleProperty().set(true);
        var zoomer = new Zoomer();
        var dataPointTooltip = new DataPointTooltip();
        var tableViewer = new TableViewer();
        var panner = new Panner();
        var crosshairIndicator = new CrosshairIndicator();
        StringConverter<Number> stringConverter = new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                return null;
            }

            @Override
            public String toString(Number t) {
                return "";
            }
        };
        crosshairIndicator.setXValueFormatter(stringConverter);
        crosshairIndicator.setYValueFormatter(stringConverter);
        chart.getPlugins().addAll(zoomer,
                dataPointTooltip,
                tableViewer,
                panner
        //                crosshairIndicator
        );

        return chart;
    }

    public static void removeStyleSheetOnSceneChange(Chart chart) {
        chart.sceneProperty().addListener((observable, s0, s1) -> {
            if (s1 != null) {
                s1.getStylesheets().clear();
            }
        });
    }

    public static void zoomOrigin(Chart chart) {
        for (var plugin : chart.getPlugins()) {
            if (plugin instanceof Zoomer zoomer) {
                zoomer.zoomOrigin();
                break;
            }
        }
    }
}
