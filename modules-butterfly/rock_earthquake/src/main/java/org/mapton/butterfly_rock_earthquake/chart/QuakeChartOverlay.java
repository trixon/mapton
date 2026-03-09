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
package org.mapton.butterfly_rock_earthquake.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.time.LocalDate;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.mapton.api.MChartOverlay;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BChartOverlay;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_rock_earthquake.QuakeChartSOSB;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class QuakeChartOverlay extends BChartOverlay {

    public static final double DEFAULT_DISTANCE_LIMIT = 30_000.0;

    public QuakeChartOverlay() {
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(QuakeChartSOSB.class, false)) {
            return;
        }

        var lastDate = LocalDate.now().plusDays(1);
        var currentStroke = new BasicStroke(4f);
        var otherStroke = new BasicStroke(1.2f);
        var pointLatLon = new MLatLon(p.getLat(), p.getLon());

        ButterflyManager.getInstance().getButterfly().rock().getEarthquakes().stream()
                .filter(b -> {
                    return DateHelper.isBetween(
                            aStartDate,
                            lastDate,
                            b.getDateLatest().toLocalDate());
                })
                .sorted((q1, q2) -> {
                    var q1LatLon = new MLatLon(q1.getLat(), q1.getLon());
                    var q1Distance = q1LatLon.distance(pointLatLon);
                    var q2LatLon = new MLatLon(q2.getLat(), q2.getLon());
                    var q2Distance = q2LatLon.distance(pointLatLon);

                    return Double.compare(Math.pow(10, q2.getMag()) / q2Distance, Math.pow(10, q1.getMag()) / q1Distance);
                })
                .limit(10)
                .forEachOrdered(q -> {
                    var blastLatLon = new MLatLon(q.getLat(), q.getLon());
                    var distance = blastLatLon.distance(pointLatLon);
                    var minute = ChartHelper.convertToMinute(q.getDateLatest());
                    var marker = new ValueMarker(minute.getFirstMillisecond());
                    var color = Color.ORANGE;

                    if (q == p) {
                        color = Color.RED;
                        marker.setStroke(currentStroke);
                    } else {
                        marker.setStroke(otherStroke);
                        var value = p.getValue("PLOT_BLAST_LABEL");
                        if (value != Boolean.FALSE) {
                            marker.setLabel("%.1f%s @ %.0f km".formatted(q.getMag(), q.getMagType(), distance / 1000.0));
                            marker.setLabelFont(new Font("Dialog", Font.PLAIN, SwingHelper.getUIScaled(10)));
                            marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                            marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                        }
                    }
                    marker.setPaint(color);
                    plot.addDomainMarker(marker);
                });
    }
}
