/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mapton.ce_chartfx.api;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.Axis;
import io.fair_acc.chartfx.plugins.CrosshairIndicator;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.TableViewer;
import io.fair_acc.chartfx.plugins.Zoomer;
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
                var format = "%%.%df".formatted(decimals);
                return format.formatted(t.doubleValue());
            }
        };
    }

    public static XYChart createXYChart(final Axis... axes) {
        var chart = new XYChart(axes);
        chart.getLegend().getNode().setVisible(true);
        var zoomer = new Zoomer();
        zoomer.setPannerEnabled(true);
        var dataPointTooltip = new DataPointTooltip();
        var tableViewer = new TableViewer();
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
                tableViewer
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
