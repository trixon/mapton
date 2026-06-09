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
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.ButterflyHelper;
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
    private final double mMaxRadius = 10.0;

    public GraphicRendererTrend(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        initScales();

        try {
            if (sCheckModel.isChecked(GraphicItem.TREND_1D_PERIOD)) {
                plotPeriod(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_PERIOD);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_1D_PERIODS)) {
                plotPeriods(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_PERIODS);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_1D_DIFF)) {
                plotDiff(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_DIFF);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_1D_DIFF_PREV)) {
                plotDiffPrev(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_DIFF_PREV);
            }

//
            if (sCheckModel.isChecked(GraphicItem.TREND_2D_PERIOD)) {
                plotPeriod(p, position, BComponent.PLANE, GraphicItem.TREND_2D_PERIOD);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_2D_PERIODS)) {
                plotPeriods(p, position, BComponent.PLANE, GraphicItem.TREND_2D_PERIODS);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_2D_DIFF)) {
                plotDiff(p, position, BComponent.PLANE, GraphicItem.TREND_2D_DIFF);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_2D_DIFF_PREV)) {
                plotDiffPrev(p, position, BComponent.PLANE, GraphicItem.TREND_2D_DIFF_PREV);
            }

//
            if (sCheckModel.isChecked(GraphicItem.TREND_1D_STACK)) {
                plotStack(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_STACK);
            }
            if (sCheckModel.isChecked(GraphicItem.TREND_2D_STACK)) {
                plotStack(p, position, BComponent.PLANE, GraphicItem.TREND_1D_STACK);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_1D_PIE)) {
                plotPie(p, position, BComponent.HEIGHT, GraphicItem.TREND_1D_PIE);
            }

            if (sCheckModel.isChecked(GraphicItem.TREND_2D_PIE)) {
                plotPie(p, position, BComponent.PLANE, GraphicItem.TREND_2D_PIE);
            }

        } catch (Exception e) {
            System.err.println("Plot TopoTrends failed at: " + p.getName());
            System.err.println(e);
        }
    }

    private double getSpeed(TrendHelper.Trend trend) {
        return TrendHelper.getVelocity(trend) / 5;
    }

    private void plotDiff(BTopoControlPoint p, Position position, GraphicItem graphicItem, TrendHelper.Trend trendA, TrendHelper.Trend trendB) {
        if (ObjectUtils.anyNull(trendA, trendB)) {
            return;
        }

        var valueA = TrendHelper.getVelocity(trendA);
        var valueB = TrendHelper.getVelocity(trendB);
        if (valueA != null) {
            var value = valueA - valueB;
            var material = ButterflyHelper.getRangeMaterial(value, 10.0);
            plotShape(position, graphicItem, value, null, material);
        }
    }

    private void plotDiff(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map;
        if (component == BComponent.HEIGHT) {
            map = p.getValue(BKey.TRENDS_H);
        } else {
            map = p.getValue(BKey.TRENDS_P);
        }

        if (map == null
                || isPlotLimitReached(p, graphicItem, position)
                || (p.getDimension() == BDimension._1d && component == BComponent.PLANE)
                || (p.getDimension() == BDimension._2d && component == BComponent.HEIGHT)) {
            return;
        }

        var trendA = map.get(mLayerOptions.getTrendPeriodA());
        var trendB = map.get(mLayerOptions.getTrendPeriodB());

        plotDiff(p, position, graphicItem, trendA, trendB);
    }

    private void plotDiffPrev(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(component == BComponent.HEIGHT ? BKey.TRENDS_H : BKey.TRENDS_P);
        HashMap<BTrendPeriod, TrendHelper.Trend> mapPrev = p.getValue(component == BComponent.HEIGHT ? BKey.TRENDS_PREV_H : BKey.TRENDS_PREV_P);

        if (ObjectUtils.anyNull(map, mapPrev) || isPlotLimitReached(p, graphicItem, position)) {
            return;
        }

        var trendA = map.get(mLayerOptions.getTrendPeriodA());
        var trendB = mapPrev.get(mLayerOptions.getTrendPeriodA());

        plotDiff(p, position, graphicItem, trendA, trendB);
    }

    private void plotPeriod(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map;
        if (component == BComponent.HEIGHT) {
            map = p.getValue(BKey.TRENDS_H);
        } else {
            map = p.getValue(BKey.TRENDS_P);
        }

        if (map == null
                || isPlotLimitReached(p, graphicItem, position)
                || (p.getDimension() == BDimension._1d && component == BComponent.PLANE)
                || (p.getDimension() == BDimension._2d && component == BComponent.HEIGHT)) {
            return;
        }

        var trend = map.get(mLayerOptions.getTrendPeriodA());
        if (trend != null) {
            var value = TrendHelper.getVelocity(trend);
            var material = ButterflyHelper.getRangeMaterial(value, 10.0);
            plotShape(position, graphicItem, value, null, material);
        }
    }

    private void plotPeriods(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map;
        if (graphicItem == GraphicItem.TREND_1D_PERIODS) {
            map = p.getValue(BKey.TRENDS_H);
        } else {
            map = p.getValue(BKey.TRENDS_P);
        }

        if (map == null
                || isPlotLimitReached(p, graphicItem, position)
                || (p.getDimension() == BDimension._1d && component == BComponent.PLANE)
                || (p.getDimension() == BDimension._2d && component == BComponent.HEIGHT)) {
            return;
        }

        int slices = 5;
        var step = 72;

        for (int i = 0; i < slices - 0; i++) {
            var interval = mIndexToIntervalMap.get(i);
            var pos = WWHelper.movePolar(position, i * step, 0.5);
            var trend = map.get(interval);
            var height = 0.5;
            if (trend != null) {
                var tempHeight = Math.abs(TrendHelper.getVelocity(trend));
                if (tempHeight > 0) {
                    height = tempHeight;
                }
            }
            var material = mIntervalToMaterialMap.get(interval);
            plotShape(pos, graphicItem, 1.0, height, material);
        }
    }

    private void plotPie(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map;
        if (graphicItem == GraphicItem.TREND_1D_PIE) {
            map = p.getValue(BKey.TRENDS_H);
        } else {
            map = p.getValue(BKey.TRENDS_P);
        }

        if (map == null
                || isPlotLimitReached(p, graphicItem, position)
                || (p.getDimension() == BDimension._1d && component == BComponent.PLANE)
                || (p.getDimension() == BDimension._2d && component == BComponent.HEIGHT)) {
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
            var outerRadius = innerRadius + Math.min(radius, mMaxRadius);
            var attrs = new BasicAirspaceAttributes();
            attrs.setOutlineWidth(3.0);
            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
            attrs.setDrawOutline(speed < 0);
            attrs.setDrawInterior(true);
            if (radius > mMaxRadius) {
                var maxMaterial = new Material(Color.decode("#800080"));
                attrs.setInteriorMaterial(maxMaterial);
            } else {
                attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(interval, Material.GRAY));
            }

            var partCyl = new PartialCappedCylinder(attrs);
            partCyl.setCenter(position);

            partCyl.setRadii(innerRadius, outerRadius);
            if (component == BComponent.HEIGHT) {
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

    private void plotShape(Position position, GraphicItem graphicItem, Double dZ, Double height, Material material) {
        if (height == null) {
            height = 0.4;
        }
        var pos = WWHelper.positionFromPosition(position, height * 0.5);
        var radius = Math.min(mMaxRadius, Math.abs(dZ) * .1 + 0.05) * .5;

        RigidShape shape;
        if (dZ > 0) {
            shape = new Box(pos, radius, height / 2, radius);
        } else {
            shape = new Cylinder(pos, height, radius);
        }
        var attrs = new BasicShapeAttributes();
        attrs.setDrawOutline(false);
        attrs.setInteriorMaterial(material);
        attrs.setEnableLighting(true);
        shape.setAttributes(attrs);

        addRenderable(shape, true, graphicItem, sMapObjects);
    }

    private void plotStack(BTopoControlPoint p, Position position, BComponent component, GraphicItem graphicItem) {
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
                    radius = Math.min(radius, mMaxRadius);
                }

                var pos = WWHelper.positionFromPosition(position, mAltitude);
                AbstractShape shape = null;
                AbstractShape shapeMax = new Cylinder(pos, 1.0, mMaxRadius * 1.25);
                AbstractAirspace airspace = null;
                if (component == BComponent.HEIGHT) {
                    if (p.getDimension() == BDimension._1d) {
                        if (speed > 0) {
                            shape = new Box(pos, radius, height / 2, radius);
                        } else {
                            shape = new Cylinder(pos, height, radius);
                        }
                    } else {
                        if (speed > 0) {
                            pos = WWHelper.movePolar(pos, 90.0, radius, pos.getElevation());
                            shape = new Box(pos, radius, height / 2, radius);
                        } else {
                            airspace = new PartialCappedCylinder(pos, radius, Angle.fromDegrees(0.0), Angle.fromDegrees(180.0));
                        }
                    }
                } else {
                    var p0 = WWHelper.movePolar(position, 0, radius);
                    var p1 = WWHelper.movePolar(position, 180, radius);
                    var p2 = WWHelper.movePolar(position, 270, radius);

                    airspace = new Polygon(List.of(p0, p1, p2));
                }

                if (shape != null) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setInteriorOpacity(1.0);
                    attrs.setDrawOutline(false);
                    attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(key, Material.GRAY));
                    attrs.setEnableLighting(true);
                    shape.setAttributes(attrs);
                    shapeMax.setAttributes(attrs);
                    addRenderable(shape, true, graphicItem, sMapObjects);
                    if (radius == mMaxRadius) {
                        addRenderable(shapeMax, true, graphicItem, sMapObjects);
                    }
                } else if (airspace != null) {
                    airspace.setAltitudes(mAltitude - height / 2, mAltitude + height / 2);
                    var attrs = new BasicAirspaceAttributes();
                    attrs.setInteriorOpacity(1.0);
                    attrs.setInteriorMaterial(mIntervalToMaterialMap.getOrDefault(key, Material.GRAY));

                    airspace.setAttributes(attrs);
                    addRenderable(airspace, true, graphicItem, sMapObjects);
                    if (radius == mMaxRadius) {
                    }
                }
            }

            mAltitude = mAltitude + height;
        }
    }
}
