/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_topo.graphics;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererTrend extends GraphicRendererBase {

    private double mAltitude;

    public GraphicRendererTrend(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicItem.TREND_INTERVAL_HEIGHT)) {
            plotTrendInterval(p, position, BComponent.HEIGHT, GraphicItem.TREND_INTERVAL_HEIGHT);
        }
        if (sCheckModel.isChecked(GraphicItem.TREND_INTERVAL_PLANE)) {
            plotTrendInterval(p, position, BComponent.PLANE, GraphicItem.TREND_INTERVAL_HEIGHT);
        }
    }

    private void plotTrendInterval(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        final var height = 25.0;
        mAltitude = height * .5;
        var maxRadius = 15.0;
        var minRadius = 0.1;
        var intervalToColor = Map.of(
                "1w", Color.RED,
                "1m", Color.ORANGE,
                "3m", Color.YELLOW,
                "6m", Color.CYAN,
                "z", Color.MAGENTA,
                "f", Color.BLACK);

        List.of("1w", "1m", "3m", "6m", "z", "f").forEach(key -> {
            HashMap<String, TrendHelper.Trend> map = p.getValue(component == BComponent.HEIGHT ? TopoManager.KEY_TRENDS_H : TopoManager.KEY_TRENDS_P);
            var radius = minRadius;
            double speed = 0.0;
            if (map != null) {
                if (map.get(key) != null) {
                    var trend = map.get(key);
                    var now = LocalDateTime.now();
                    var val1 = trend.function().getValue(ChartHelper.convertToMinute(now.plusYears(1)).getFirstMillisecond());
                    var val2 = trend.function().getValue(ChartHelper.convertToMinute(now).getFirstMillisecond());
                    speed = (val1 - val2) * 1000;
                    radius = Math.max(minRadius, Math.abs(speed * 0.5));
                    radius = Math.min(radius, maxRadius);
                }

                var pos = WWHelper.positionFromPosition(position, mAltitude);
                AbstractShape shape = null;
                AbstractAirspace airspace = null;
                if (component == BComponent.HEIGHT) {
                    if (p.getDimension() == BDimension._1d) {
                        shape = new Cylinder(pos, height, radius);
                    } else {
                        airspace = new PartialCappedCylinder(pos, radius, Angle.fromDegrees(0.0), Angle.fromDegrees(180.0));
                    }
                } else {
                    var p0 = WWHelper.movePolar(position, 0, radius);
                    var p1 = WWHelper.movePolar(position, 180, radius);
                    var p2 = WWHelper.movePolar(position, 270, radius);

                    airspace = new Polygon(List.of(p0, p1, p2));
                }

                if (shape != null) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setInteriorOpacity(0.75);
                    attrs.setDrawOutline(false);
                    attrs.setInteriorMaterial(new Material(intervalToColor.get(key)));
                    attrs.setEnableLighting(true);
                    shape.setAttributes(attrs);
                    addRenderable(shape, true, graphicItem, sMapObjects);
                    if (radius == maxRadius) {
                        attrs.setInteriorOpacity(1.0);
                    }

                    if (speed > 0) {
                        attrs.setDrawOutline(true);
                        if (intervalToColor.get(key).equals(Color.BLACK)) {
                            attrs.setOutlineMaterial(Material.WHITE);
                        }
                    }
                } else if (airspace != null) {
                    airspace.setAltitudes(mAltitude - height / 2, mAltitude + height / 2);
                    var attrs = new BasicAirspaceAttributes();
                    attrs.setInteriorOpacity(0.75);
                    attrs.setInteriorMaterial(new Material(intervalToColor.get(key)));

                    airspace.setAttributes(attrs);
                    addRenderable(airspace, true, graphicItem, sMapObjects);
                    if (radius == maxRadius) {
                        attrs.setInteriorOpacity(1.0);
                    }

                    if (speed > 0) {
                        attrs.setDrawOutline(true);
                        if (intervalToColor.get(key).equals(Color.BLACK)) {
                            attrs.setOutlineMaterial(Material.WHITE);
                        }
                    }
                }
            }

            mAltitude = mAltitude + height;

        });
    }
}
