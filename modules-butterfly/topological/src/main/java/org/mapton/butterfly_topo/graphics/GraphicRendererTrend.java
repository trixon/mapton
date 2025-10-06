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
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sMapObjects;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererTrend extends GraphicRendererBase {

    public static final Map<Integer, BTrendPeriod> mIndexToIntervalMap = Map.of(
            0, BTrendPeriod.ZERO,
            1, BTrendPeriod.HALF_YEAR,
            2, BTrendPeriod.QUARTER,
            3, BTrendPeriod.MONTH,
            4, BTrendPeriod.WEEK
    );
    public static final Map<BTrendPeriod, Material> mIntervalToMaterialMap = Map.of(
            BTrendPeriod.WEEK, Material.RED,
            BTrendPeriod.MONTH, Material.ORANGE,
            BTrendPeriod.QUARTER, Material.YELLOW,
            BTrendPeriod.HALF_YEAR, Material.CYAN,
            BTrendPeriod.ZERO, Material.MAGENTA,
            BTrendPeriod.FIRST, Material.BLACK);
    private double mAltitude;
    private final double maxRadius = 10.0;

    public GraphicRendererTrend(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        initScales();
        if (sCheckModel.isChecked(GraphicItem.TREND_1D_STACK)) {
            plotTrendStack(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_STACK);
        }
        if (sCheckModel.isChecked(GraphicItem.TREND_2D_STACK)) {
            plotTrendStack(p, position, BComponent.PLANE, GraphicItem.TREND_1D_STACK);
        }

        if (sCheckModel.isChecked(GraphicItem.TREND_1D_PIE)) {
            plotTrendPie(p, position, BDimension._1d, GraphicItem.TREND_1D_PIE);
        }

        if (sCheckModel.isChecked(GraphicItem.TREND_2D_PIE)) {
            plotTrendPie(p, position, BDimension._2d, GraphicItem.TREND_2D_PIE);
        }

    }

    private double getSpeed(TrendHelper.Trend trend) {
        return TrendHelper.getMmPerYear(trend) / 5;
    }

    private void plotTrendPie(BTopoControlPoint p, Position position, BDimension dimension, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map;
        if (graphicItem == GraphicItem.TREND_1D_PIE) {
            map = p.getValue(BKey.TRENDS_H);
        } else {
            map = p.getValue(BKey.TRENDS_P);
        }

        if ((map == null || isPlotLimitReached(p, graphicItem, position))
                || (p.getDimension() == BDimension._1d && dimension == BDimension._2d)
                || (p.getDimension() == BDimension._2d && dimension == BDimension._1d)) {
            return;
        }

        int slices = 5;

        for (int i = 0; i < slices - 0; i++) {
            var interval = mIndexToIntervalMap.get(i);
            var trend = map.get(interval);
            if (trend == null) {
                continue;
            }
            var innerRadius = 0.0;
            var speed = getSpeed(trend);
            var radius = Math.abs(speed);
            var outerRadius = innerRadius + Math.min(radius, maxRadius);
            var attrs = new BasicAirspaceAttributes();
            attrs.setOutlineWidth(3.0);
            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
            attrs.setDrawOutline(speed < 0);
            attrs.setDrawInterior(true);
            if (radius > maxRadius) {
                var maxMaterial = new Material(Color.decode("#800080"));
                attrs.setInteriorMaterial(maxMaterial);
            } else {
                attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(interval, Material.GRAY));
            }

            var partCyl = new PartialCappedCylinder(attrs);
            partCyl.setCenter(position);

            partCyl.setRadii(innerRadius, outerRadius);
            if (dimension == BDimension._1d) {
                partCyl.setAltitudes(0.0, 0.25);
            } else {
                partCyl.setAltitudes(5.0, 5.25);
                var groundPath = new Path(position, WWHelper.positionFromPosition(position, 5.0));
                groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                addRenderable(groundPath, true, null, sMapObjects);
            }
            var step = 72;
            var left = -1.5 * step + (i + 1) * step;
            var right = left + step;
            partCyl.setAzimuths(Angle.fromDegrees(left), Angle.fromDegrees(right));

            addRenderable(partCyl, true, null, sMapObjects);
        }
    }

    private void plotTrendStack(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        final var height = 25.0;
        mAltitude = height * .5;
        var minRadius = 0.1;
        for (var key : BTrendPeriod.values()) {
            HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(component == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P);
            var radius = minRadius;
            double speed = 0.0;
            if (map != null) {
                if (map.get(key) != null) {
                    var trend = map.get(key);
                    speed = getSpeed(trend);
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
                    attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(key, Material.GRAY));
                    attrs.setEnableLighting(true);
                    shape.setAttributes(attrs);
                    addRenderable(shape, true, graphicItem, sMapObjects);
                    if (radius == maxRadius) {
                        attrs.setInteriorOpacity(1.0);
                    }

                    if (speed > 0) {
                        attrs.setDrawOutline(true);
                        if (mIntervalToMaterialMap.get(key).equals(Material.BLACK)) {
                            attrs.setOutlineMaterial(Material.WHITE);
                        }
                    }
                } else if (airspace != null) {
                    airspace.setAltitudes(mAltitude - height / 2, mAltitude + height / 2);
                    var attrs = new BasicAirspaceAttributes();
                    attrs.setInteriorOpacity(0.75);
                    attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(key, Material.GRAY));

                    airspace.setAttributes(attrs);
                    addRenderable(airspace, true, graphicItem, sMapObjects);
                    if (radius == maxRadius) {
                        attrs.setInteriorOpacity(1.0);
                    }

                    if (speed > 0) {
                        attrs.setDrawOutline(true);
                        if (mIntervalToMaterialMap.get(key).equals(Material.BLACK)) {
                            attrs.setOutlineMaterial(Material.WHITE);
                        }
                    }
                }
            }

            mAltitude = mAltitude + height;
        }
    }
}
