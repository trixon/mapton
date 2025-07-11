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
import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.time.temporal.ChronoUnit;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererCircle extends GraphicRendererBase {

    public GraphicRendererCircle(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        initScales();
        if (sCheckModel.isChecked(GraphicItem.CIRCLE_1D) && p.getDimension() == BDimension._1d) {
            plot1dCircle(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.CIRCLE_2D) && p.getDimension() != BDimension._1d) {
            plot2dCircle(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.CIRCLE_3D) && p.getDimension() == BDimension._3d) {
            plot3dCircle(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.CIRCLE_VERTICAL_DIRECTION) && p.getDimension() != BDimension._2d) {
            plotVerticalDirection(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.FREQ_BUFFER)) {
            plotFreqBuffer(p, position);
        }
    }

    private void plot1dCircle(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.CIRCLE_1D, position)) {
            return;
        }

        var height = 0.4;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        BTopoControlPointObservation o = p.ext().getObservationFilteredLast();

        var dZ = o.ext().getDeltaZ();
        if (dZ == null) {
            return;
        }
        var radius = Math.min(maxRadius, Math.abs(dZ) * 250 + 0.05);
        var maximus = radius == maxRadius;
        var rise = Math.signum(dZ) > 0;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevelHeight(o);
        var attrs = mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, rise, maximus);
//        if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorOpacity(0.25);
//            attrs.setOutlineOpacity(0.20);
//        }

        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true, GraphicItem.CIRCLE_1D, sMapObjects);
    }

    private void plot2dCircle(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.CIRCLE_2D, position)) {
            return;
        }

        var height = 0.8;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        var o = p.ext().getObservationFilteredLast();

        var delta2d = o.ext().getDelta2d();
        if (delta2d == null) {
            return;
        }
        var radius = Math.min(maxRadius, Math.abs(delta2d) * 250 + 0.05);
        var maximus = radius == maxRadius;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevel(o);
        var attrs = mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, false, maximus);

        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true, GraphicItem.CIRCLE_2D, sMapObjects);
    }

    private void plot3dCircle(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.CIRCLE_3D, position)) {
            return;
        }

        var height = 0.8;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        BTopoControlPointObservation o = p.ext().getObservationFilteredLast();

        var delta3d = o.ext().getDelta3d();
        if (delta3d == null) {
            return;
        }
        var radius = Math.min(maxRadius, Math.abs(delta3d) * 250 + 0.05);
        var maximus = radius == maxRadius;
        var rise = Math.signum(delta3d) > 0;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevel(o);
        var attrs = mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, rise, maximus);

        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true, GraphicItem.CIRCLE_3D, sMapObjects);
    }

    private void plotFreqBuffer(BTopoControlPoint p, Position position) {
        var radius = p.ext().getFrequenceHighBuffer();
        if (radius != null) {
            var circle = new SurfaceCircle(position, Math.max(0.1, radius));
            var attrs = new BasicShapeAttributes();
            attrs.setDrawInterior(false);
            attrs.setDrawOutline(true);
            attrs.setOutlineMaterial(Material.CYAN);
            attrs.setOutlineWidth(1.0);
            attrs.setOutlineOpacity(0.25);
            circle.setAttributes(attrs);

            addRenderable(circle, false, GraphicItem.FREQ_BUFFER, null);

        }
    }

    private void plotVerticalDirection(BTopoControlPoint p, Position position) {
        var days = p.ext().getZeroToLatestMeasurementAge(ChronoUnit.DAYS);
        var minDays = 90.0;
        var dayToMeter = 0.004;
        var minRadius = minDays * dayToMeter;
        var maxRadius = minRadius * 10.0;
        var radius = days * dayToMeter;
        radius = Math.min(Math.max(radius, minRadius), maxRadius);

        var sa = new BasicShapeAttributes();

        sa.setDrawOutline(radius == maxRadius);
//        sa.setOutlineMaterial(Material.MAGENTA);
//        sa.setOutlineWidth(2.0);
        sa.setDrawOutline(false);
        if (radius == minRadius) {
            sa.setInteriorOpacity(0.25);
        }

        var dz = p.ext().deltaZero().getDeltaZ();
        if (dz == null) {
            return;
        }

        sa.setInteriorMaterial(TopoHelper.getVerticalMaterial(p));

        AbstractSurfaceShape shape;
        if (p.getDimension() == BDimension._1d) {
            shape = new SurfaceCircle(sa, position, radius);
        } else {
            var box = new SurfacePolygon(WWHelper.createNodes(position, radius, 3));
            shape = box;
        }
        shape.setAttributes(sa);
        addRenderable(shape, true, GraphicItem.CIRCLE_VERTICAL_DIRECTION, sMapObjects);

        var sa2 = new BasicShapeAttributes();
        sa2.setDrawOutline(false);
        sa2.setInteriorMaterial(Material.PINK);

        if (radius == maxRadius) {
            var circle = new SurfaceCircle(sa, position, minRadius);
            circle.setAttributes(sa2);
            addRenderable(circle, false, GraphicItem.CIRCLE_VERTICAL_DIRECTION, sMapObjects);
        }
    }
}
