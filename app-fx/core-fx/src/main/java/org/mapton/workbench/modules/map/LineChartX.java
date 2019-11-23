/*
 * Copyright 2019 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.workbench.modules.map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.mapton.api.MChartLine;

/**
 *
 * @author Patrik Karlström
 */
public class LineChartX {

    private final MChartLine mChart;
    private LineChart<String, Number> mLineChart;
    private NumberAxis mNumberAxis;
    private XYChart.Series[] mSeries;
    private CategoryAxis mXAxis;

    public LineChartX(MChartLine chart) {
        mChart = chart;
        mXAxis = new CategoryAxis();
        mNumberAxis = new NumberAxis();
        mNumberAxis.setLabel(chart.getValueTitle());

        mLineChart = new LineChart<>(mXAxis, mNumberAxis);
        mLineChart.setAxisSortingPolicy(LineChart.SortingPolicy.X_AXIS);
        mLineChart.setCreateSymbols(chart.isPlotSymbols());
        mLineChart.setAnimated(false);
        mXAxis.setStartMargin(0);
        mLineChart.setTitle(chart.getChartTitle());

        mSeries = new XYChart.Series[chart.getTitles().length];
        for (int i = 0; i < mSeries.length; i++) {
            ObservableList<XYChart.Data<String, Number>> data = FXCollections.observableArrayList();
            ObservableList<Number> values = chart.getValues()[i];

            for (int j = 0; j < values.size(); j++) {
                Number n = values.get(j);
                data.add(new XYChart.Data<>(chart.getColumns().get(j), n));
            }

            mSeries[i] = new XYChart.Series<>(chart.getTitles()[i], data);
        }

        mLineChart.getData().setAll(mSeries);
    }

    public Node getNode() {
        return mLineChart;
    }
}
