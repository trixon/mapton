/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.time.LocalDate;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.mapton.api.MChartOverlay;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BChartOverlay extends MChartOverlay {

    protected final NumberAxis mAxis;

    public BChartOverlay(String title) {
        mAxis = new NumberAxis(title);
        mAxis.setAutoRangeIncludesZero(false);
        mAxis.setAutoRange(true);
    }

    public void init(XYPlot plot, TimeSeriesCollection dataset, XYItemRenderer renderer) {
        plot.setDataset(mIndex, dataset);
        plot.setRangeAxis(mIndex, mAxis);
        plot.mapDatasetToRangeAxis(mIndex, mIndex);
        plot.setRangeAxisLocation(mIndex, AxisLocation.BOTTOM_OR_RIGHT);
        plot.setRenderer(mIndex, renderer);
    }

    public abstract void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate);

    public void resetDatasetIfExisting(XYPlot plot, int index) {
        if (plot.getDatasets().keySet().contains(index)) {
            plot.setRangeAxis(index, null);
//            plot.setDataset(index, null);
        }
    }
}
