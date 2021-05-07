/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mapton.ce_chartfx.api;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.EditAxis;
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
        chart.getPlugins().add(zoomer);
        chart.getPlugins().add(new EditAxis());
        chart.getPlugins().add(new DataPointTooltip());
        chart.getPlugins().add(new TableViewer());
        chart.getPlugins().add(new Panner());

        return chart;
    }

    public static void removeStyleSheetOnSceneChange(Chart chart) {
        chart.sceneProperty().addListener((observable, s0, s1) -> {
            if (s1 != null) {
                s1.getStylesheets().clear();
            }
        });
    }
}
