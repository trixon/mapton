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
package org.mapton.butterfly_geo_reinforcement.chart;

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
import org.mapton.butterfly_format.types.BStep;
import org.mapton.butterfly_geo_reinforcement.ReinforcementChartSOSB;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MChartOverlay.class)
public class ReinforcementChartOverlay extends BChartOverlay {

    public static final double DEFAULT_DISTANCE_LIMIT = 20.0;

    public ReinforcementChartOverlay() {
    }

    @Override
    public synchronized void plot(XYPlot plot, BBasePoint p, LocalDate aStartDate) {
        if (!mObjectStorageManager.getBoolean(ReinforcementChartSOSB.class, false)) {
            return;
        }

        var lastDate = LocalDate.now().plusDays(1);

        var currentStroke = new BasicStroke(4f);
        var otherStroke = new BasicStroke(1.2f);
        var pointLatLon = new MLatLon(p.getLat(), p.getLon());

        ButterflyManager.getInstance().getButterfly().geotechnical().getReinforcementPoints().stream()
                .filter(b -> b.getStep() == BStep.DONE)
                .filter(b -> b.getDateLatest() != null)
                .filter(b -> {
                    return DateHelper.isBetween(
                            aStartDate,
                            lastDate,
                            b.getDateLatest().toLocalDate());
                })
                .forEachOrdered(b -> {
                    var blastLatLon = new MLatLon(b.getLat(), b.getLon());
                    var distance = blastLatLon.distance(pointLatLon);

                    if (distance <= DEFAULT_DISTANCE_LIMIT) {
                        var minute = ChartHelper.convertToMinute(b.getDateLatest());
                        var marker = new ValueMarker(minute.getFirstMillisecond());
                        Color color;

                        if (b == p) {
                            color = Color.RED;
                            marker.setStroke(currentStroke);
                        } else {
                            var distanceQuota = (DEFAULT_DISTANCE_LIMIT - distance) / (DEFAULT_DISTANCE_LIMIT - 5.0);
                            marker.setStroke(otherStroke);
                            distanceQuota = Math.min(1, distanceQuota);
                            int alpha = (int) (Math.max(distanceQuota, 0.25) * 255d);
                            color = new Color(200, 100, 0, alpha);

                            var value = p.getValue("PLOT_REINFORCEMENT_LABEL");
                            if (value != Boolean.FALSE) {
                                marker.setLabel("%.0f".formatted(distance));
                                marker.setLabelFont(new Font("Dialog", Font.PLAIN, SwingHelper.getUIScaled(10)));
                                marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
                                marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
                            }
                        }
                        marker.setPaint(color);
                        plot.addDomainMarker(marker);
                    }
                });
    }
}
