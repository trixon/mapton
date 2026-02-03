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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.butterfly_topo.TopoLayerBundle;
import static org.mapton.butterfly_topo.graphics.GraphicRendererBase.sMapObjects;
import org.mapton.ce_jfreechart.api.ChartHelper;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererVector extends GraphicRendererBase {

    public GraphicRendererVector(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        initScales();

        var dimension = p.getDimension();

        if (sCheckModel.isChecked(GraphicItem.VECTOR_1D) && (dimension == BDimension._1d || dimension == BDimension._3d)) {
            plot1d(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VECTOR_1D) && sCheckModel.isChecked(GraphicItem.VECTOR_1D_ALARM) && dimension != BDimension._2d) {
            plot1dVectorAlarm(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VECTOR_1D) && sCheckModel.isChecked(GraphicItem.VECTOR_1D_LABEL) && dimension != BDimension._2d) {
            plot1dVectorLabel(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VECTOR_1D) && sCheckModel.isChecked(GraphicItem.VECTOR_1D_TREND) && dimension != BDimension._2d) {
            plot1dVectorTrend(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VECTOR_3D) && dimension == BDimension._3d) {
            plot3d(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.PIN) && dimension != BDimension._2d) {
            plotPoint(p, position);
        }
    }

    private void plot1d(BTopoControlPoint p, Position position) {
        plot1dVector(p, position);
    }

    private void plot1dVector(BTopoControlPoint p, Position position) {
        var zeroZ = p.getZeroZ();
        if (zeroZ == null) {
            return;
        }
        var ZERO_SIZE = 0.4;
        var CURRENT_SIZE = 1.0;
        var zeroPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.getZOffset());
        var zeroEllipsoid = new Ellipsoid(zeroPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
        zeroEllipsoid.setAttributes(mAttributeManager.getComponentZeroAttributes());
        addRenderable(zeroEllipsoid, true, null, sMapObjects);

        var currentPosition = zeroPosition;
        var o = p.ext().getObservationsTimeFiltered().getLast();
        var direction = o.ext().getDeltaZ() != null && o.ext().getDeltaZ() < 0 ? -1 : 1;

        if (o.ext().getDeltaZ() != null) {
            var z = zeroZ
                    + TopoLayerBundle.getZOffset()
                    + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * mScale3dH;

            currentPosition = WWHelper.positionFromPosition(currentPosition, z);
        }

        var currentEllipsoid = new Ellipsoid(currentPosition, CURRENT_SIZE, CURRENT_SIZE, CURRENT_SIZE);
        currentEllipsoid.setAttributes(mAttributeManager.getComponentVectorCurrentHeightAttributes(p));
        addRenderable(currentEllipsoid, true, null, sMapObjects);

        var currentPositionTop = WWHelper.positionFromPosition(currentPosition, currentPosition.getAltitude() - CURRENT_SIZE * direction);
        var deltaPath = new Path(zeroPosition, currentPositionTop);
        deltaPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(deltaPath, true, null, sMapObjects);

        var currentPositionBottom = WWHelper.positionFromPosition(currentPosition, currentPosition.getAltitude() + CURRENT_SIZE * direction);
        var groundPath = new Path(position, currentPositionBottom);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, null, sMapObjects);
    }

    private void plot1dVectorAlarm(BTopoControlPoint p, Position position) {
        var zeroZ = p.getZeroZ();
        var alarm = p.ext().getAlarm(BComponent.HEIGHT);

        if (ObjectUtils.anyNull(alarm, zeroZ) || TopoHelper.getAlarmLevelHeight(p) < 0) {
            return;
        }

        var min0 = alarm.ext().getRange0().getMinimum();
        var max0 = alarm.ext().getRange0().getMaximum();
        var span0 = max0 - min0;
        var scaledSpan0 = span0 * mScale3dH;
        var nonSymmetricAdjustment = span0 / 2 + min0;

        var z0 = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustment * mScale3dH;
        var zeroPosition = WWHelper.positionFromPosition(position, z0);
        var radius = 0.5;
        var cylinder0 = new Cylinder(zeroPosition, radius, scaledSpan0 * 0.5, radius);
        cylinder0.setAttributes(mAttributeManager.getComponentVectorAlarmAttributes(0));
        addRenderable(cylinder0, true, null, sMapObjects);

        if (TopoHelper.getAlarmLevelHeight(p) > 0) {
            var attrs = mAttributeManager.getComponentVectorAlarmAttributes(1);

            //Plot bottom cylinder
            var bottomLower = alarm.ext().getRange1().getMinimum();
            var bottomUpper = min0;
            var spanBottom = bottomUpper - bottomLower;
            var scaledSpanBottom = spanBottom * mScale3dH;
            var nonSymmetricAdjustmentBottom = spanBottom / 2 + bottomLower;
            var zBottom = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustmentBottom * mScale3dH;
            var bottomPosition = WWHelper.positionFromPosition(position, zBottom);
            try {
                var cylinderBottom = new Cylinder(bottomPosition, radius, scaledSpanBottom * 0.5, radius);
                cylinderBottom.setAttributes(attrs);
                addRenderable(cylinderBottom, true, null, sMapObjects);
            } catch (IllegalArgumentException e) {
                System.out.println(p.getName());
                System.out.println(e);
            }

            //Plot top cylinder
            var topLower = max0;
            var topUpper = alarm.ext().getRange1().getMaximum();
            var spanTop = topUpper - topLower;
            var scaledSpanTop = spanTop * mScale3dH;
            var nonSymmetricAdjustmentTop = spanTop / 2 + topLower;
            var zTop = zeroZ + TopoLayerBundle.getZOffset() + nonSymmetricAdjustmentTop * mScale3dH;
            var topPosition = WWHelper.positionFromPosition(position, zTop);
            try {
                var cylinderTop = new Cylinder(topPosition, radius, scaledSpanTop * 0.5, radius);
                cylinderTop.setAttributes(attrs);
                addRenderable(cylinderTop, true, null, sMapObjects);
            } catch (IllegalArgumentException e) {
                System.out.println(p.getName());
                System.out.println(e);
            }
        }
    }

    private void plot1dVectorLabel(BTopoControlPoint p, Position position) {
        var zeroZ = p.getZeroZ();
        var o = p.ext().getObservationsTimeFiltered().getLast();

        if (ObjectUtils.anyNull(zeroZ, o.ext().getDeltaZ())) {
            return;
        }
        var zeroPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.getZOffset());

        var z = zeroZ
                + TopoLayerBundle.getZOffset()
                + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * mScale3dH;

        var currentPosition = WWHelper.positionFromPosition(zeroPosition, z);
        plotLabel(currentPosition, p.ext().deltaZero().getDelta1(0, 1000, true));
    }

    private void plot1dVectorTrend(BTopoControlPoint p, Position position) {
        HashMap<BTrendPeriod, TrendHelper.Trend> map = p.getValue(BKey.TRENDS_H);
        var zeroZ = p.getZeroZ();
        if (ObjectUtils.anyNull(map, zeroZ)) {
            return;
        }
        var TREND_SIZE = 0.3;
        var zeroPosition = WWHelper.positionFromPosition(position, zeroZ + TopoLayerBundle.getZOffset());

        var now = LocalDateTime.now();
        var positions = new TreeSet<Position>(Comparator.comparingDouble(Position::getElevation));

        for (var entry : GraphicRendererTrend.mIntervalToMaterialMap.entrySet()) {
            var key = entry.getKey();
            var material = entry.getValue();
            var trend = map.get(key);
            if (trend == null || entry.getKey() == BTrendPeriod.FIRST) {
                continue;
            }

            var trendDiff = trend.function().getValue(ChartHelper.convertToMinute(now.plusMonths(1)).getFirstMillisecond());

            var trendPosition = zeroPosition;

            var z = zeroZ
                    + TopoLayerBundle.getZOffset()
                    + trendDiff * mScale3dH;
            trendPosition = WWHelper.positionFromPosition(trendPosition, z);
            positions.add(trendPosition);

            var trendBox = new Box(trendPosition, TREND_SIZE, TREND_SIZE, TREND_SIZE);
            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentVectorCurrentHeightAttributes(p));
            attrs.setInteriorMaterial(material);
            attrs.setInteriorOpacity(1.0);
            trendBox.setAttributes(attrs);
            addRenderable(trendBox, true, null, sMapObjects);
        }

        if (!positions.isEmpty()) {
            for (var pos : List.of(positions.first(), positions.last())) {
                var groundPath = new Path(pos, zeroPosition);
                groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                addRenderable(groundPath, true, null, sMapObjects);
            }
        }
    }

    private void plot2d(BTopoControlPoint p, Position position) {
    }

    private void plot3d(BTopoControlPoint p, Position position) {
        if (!isValidFor3dPlot(p)) {
            return;
        }

        var positions = plot3dOffsetPole(p, position, true, 1.0, true);
        var startPosition = positions[0];
        var endPosition = positions[1];

        var path = new Path(startPosition, endPosition);
        path.setAttributes(mAttributeManager.getComponentVector3dAttributes(p));
        addRenderable(path, true, null, sMapObjects);

        //plot dZ
        var endDeltaZ = Position.fromDegrees(startPosition.latitude.degrees, startPosition.longitude.degrees, endPosition.getAltitude());
        var pathDeltaZ = new Path(startPosition, endDeltaZ);
        pathDeltaZ.setAttributes(mAttributeManager.getComponentVector1dAttributes(p));
        addRenderable(pathDeltaZ, true, null, sMapObjects);

        //plot dR
        var pathDeltaR = new Path(endDeltaZ, endPosition);
        pathDeltaR.setAttributes(mAttributeManager.getComponentVector2dAttributes(p));
        addRenderable(pathDeltaR, true, null, sMapObjects);

        plotLabel(p, positions[0]);
    }

    private void plotPoint(BTopoControlPoint p, Position position) {
        if (ObjectUtils.anyNull(p.getZeroX(), p.getZeroY(), p.getZeroZ())) {
            return;
        }

        var positions = plot3dOffsetPole(p, position, true, 0.75, false);
        plotLabel(p, positions[0]);
    }

}
