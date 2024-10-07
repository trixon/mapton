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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import static org.mapton.butterfly_topo.GraphicRendererBase.sPlotLimiter;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererAlarmLevel extends GraphicRendererBase {

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_LEVEL)) {
            plotAlarmLevel(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TRACE_ALARM_LEVEL)) {
            plotAlarmLevelTrace(p, position, BComponent.HEIGHT);
            plotAlarmLevelTrace(p, position, BComponent.PLANE);
        }
    }

    private void plotAlarmConsumption(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION, position)) {
            return;
        }

        var height = 50.0;
        var radius = 1.5;
        var o = p.ext().getObservationFilteredLast();

        Integer percentP = p.ext().getAlarmPercent(BComponent.PLANE);
        if (p.getDimension() != BDimension._1d && percentP != null) {
            var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelPlane(o));
            var attrs = new BasicAirspaceAttributes();
            attrs.setInteriorMaterial(material);

            var pos = WWHelper.positionFromPosition(position, height * percentP / 100.0);
            var pyramid = new Pyramid(pos, radius, radius);
            pyramid.setAttributes(attrs);
            addRenderable(pyramid, true);
        }

        Integer percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
        if (p.getDimension() != BDimension._2d && percentH != null) {
            var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelHeight(o));
            var attrs = new BasicAirspaceAttributes();
            attrs.setInteriorMaterial(material);
            radius = radius * 0.6;
            var pos = WWHelper.positionFromPosition(position, height * percentH / 100.0);
            var ellipsoid = new Ellipsoid(pos, radius, radius, radius);
            ellipsoid.setAttributes(attrs);
            addRenderable(ellipsoid, true);
        }

        Integer percent = p.ext().getAlarmPercent();
        if (percent != null) {
            var pos = WWHelper.positionFromPosition(position, height * Math.max(percent, 100) / 100.0);
            var groundPath = new Path(WWHelper.positionFromPosition(position, 0.0), pos);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(groundPath, true);
        }
    }

    private void plotAlarmLevel(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_LEVEL, position)) {
            return;
        }

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            plotAlarmLevelH(p, position);
        }
        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            plotAlarmLevelP(p, position);
        }

    }

    private void plotAlarmLevelH(BTopoControlPoint p, Position position) {
        var o = p.ext().getObservationFilteredLast();
        var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelHeight(o));
        var attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(material);

        var partCyl = new PartialCappedCylinder(attrs);
        partCyl.setCenter(position);
        partCyl.setRadii(1, 2);
        partCyl.setAltitudes(0.0, 0.25);
        if (p.getDimension() == BDimension._1d) {
            partCyl.setAzimuths(Angle.fromDegrees(10.0), Angle.fromDegrees(350.0));
        } else {
            partCyl.setAzimuths(Angle.fromDegrees(0.0), Angle.fromDegrees(180.0));
        }

        addRenderable(partCyl, true);
    }

    private void plotAlarmLevelP(BTopoControlPoint p, Position position) {
        var o = p.ext().getObservationFilteredLast();
        var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelPlane(o));
        var attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(material);

        var partCyl = new PartialCappedCylinder(attrs);
        partCyl.setCenter(position);
        partCyl.setRadii(0, 1);
        partCyl.setAltitudes(0.0, 0.25);
        if (p.getDimension() == BDimension._2d) {
            partCyl.setAzimuths(Angle.fromDegrees(10.0), Angle.fromDegrees(350.0));
        } else {
            partCyl.setAzimuths(Angle.fromDegrees(180.0), Angle.fromDegrees(360.0));
        }

        addRenderable(partCyl, true);
    }

    private void plotAlarmLevelTrace(BTopoControlPoint p, Position position, BComponent component) {
        var changes = new ArrayList<AlarmLevelChange>();
        Integer prevLevel = null;

        for (var o : p.ext().getObservationsTimeFiltered()) {
            int alarmLevel = p.ext().getAlarmLevel(component, o);
            if (prevLevel == null || alarmLevel != prevLevel) {
                changes.add(new AlarmLevelChange(o.getDate(), alarmLevel));
                prevLevel = alarmLevel;
            }
        }

        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < changes.size(); i++) {
            var o = changes.reversed().get(i);
            var timeSpan = ChronoUnit.MINUTES.between(o.localDateTime(), prevDate);
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.localDateTime;
            prevHeight = height;

            var dimension = p.getDimension();
            var pos = WWHelper.positionFromPosition(position, altitude);
            var radius = 1.0;
            AbstractShape shape = null;
            AbstractAirspace airspace = null;

            if (component == BComponent.HEIGHT) {
                if (dimension == BDimension._1d) {
                    shape = new Cylinder(pos, height, radius);
                } else if (dimension == BDimension._3d) {
                    airspace = new PartialCappedCylinder(pos, radius, Angle.fromDegrees(0.0), Angle.fromDegrees(180.0));
                }
            } else {
                if (dimension == BDimension._2d) {
                    var p0 = WWHelper.movePolar(position, 0, radius);
                    var p1 = WWHelper.movePolar(position, 120, radius);
                    var p2 = WWHelper.movePolar(position, 240, radius);

                    airspace = new Polygon(List.of(p0, p1, p2));
                } else if (dimension == BDimension._3d) {
                    var p0 = WWHelper.movePolar(position, 0, radius);
                    var p1 = WWHelper.movePolar(position, 180, radius);
                    var p2 = WWHelper.movePolar(position, 270, radius);

                    airspace = new Polygon(List.of(p0, p1, p2));
                }
            }

            var material = ButterflyHelper.getAlarmMaterial(o.alarmLevel());

            if (shape != null) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(false);
                attrs.setInteriorMaterial(material);
                attrs.setEnableLighting(true);
                shape.setAttributes(attrs);
                addRenderable(shape, true);
                sPlotLimiter.incPlotCounter(GraphicRendererItem.TRACE_ALARM_LEVEL);
            } else if (airspace != null) {
                airspace.setAltitudes(altitude - height / 2, altitude + height / 2);
                var attrs = new BasicAirspaceAttributes();
                attrs.setInteriorMaterial(material);
                airspace.setAttributes(attrs);
                addRenderable(airspace, true);
                sPlotLimiter.incPlotCounter(GraphicRendererItem.TRACE_ALARM_LEVEL);
            }

        }
    }

    public record AlarmLevelChange(LocalDateTime localDateTime, Integer alarmLevel) {

    }
}
