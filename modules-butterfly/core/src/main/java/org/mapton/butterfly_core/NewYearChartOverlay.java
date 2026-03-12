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
package org.mapton.butterfly_core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.time.LocalDate;
import java.time.Month;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.mapton.api.MChartOverlay;
import org.mapton.butterfly_core.api.BChartOverlay;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class NewYearChartOverlay extends BChartOverlay {

    public static final Color COLOR = Color.GREEN.brighter();

    public NewYearChartOverlay() {
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(NewYearChartSOSB.class, false)) {
            return;
        }

        var lastDate = LocalDate.now().plusDays(1);
        var stroke = new BasicStroke(10.0f);
        var color = GraphicsHelper.colorAddAlpha(COLOR, 30);

        for (int year = aStartDate.getYear(); year <= lastDate.getYear(); year++) {
            var januaryFirst = LocalDate.of(year, Month.JANUARY, 1);
            if (DateHelper.isBetween(aStartDate, lastDate, januaryFirst)) {
                var minute = ChartHelper.convertToMinute(januaryFirst.atStartOfDay());
                var marker = new ValueMarker(minute.getFirstMillisecond(), color, stroke);
                plot.addDomainMarker(marker);
            }
        }
    }
}
