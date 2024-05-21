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
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import static org.mapton.butterfly_topo.GraphicRendererBase.sPlotLimiter;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererSpeed extends GraphicRendererBase {

    public GraphicRendererSpeed() {
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicRendererItem.SPEED_1D)) {
            plotSpeed(p, position);
        }

//        if (sCheckModel.isChecked(GraphicRendererItem.TRACE_ALARM_LEVEL)) {
//            plotAlarmLevelTrace(p, position, BComponent.HEIGHT);
//            plotAlarmLevelTrace(p, position, BComponent.PLANE);
//        }
    }

    private void plotSpeed(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.SPEED_1D, position)) {
            return;
        }

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            plotAlarmLevel1d(p, position);
        }
    }

    private void plotAlarmLevel1d(BTopoControlPoint p, Position position) {
        var height = 0.4;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        BTopoControlPointObservation o = p.ext().getObservationFilteredLast();

        var dZ = o.ext().getDeltaZ();
        if (dZ == null) {
            return;
        }

        dZ = p.ext().getSpeed()[0];
        var radius = Math.min(maxRadius, Math.abs(dZ) * 250 + 0.05);
        var maximus = radius == maxRadius;
        var rise = Math.signum(dZ) > 0;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevelHeight(o);
        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, rise, maximus));
        attrs.setInteriorMaterial(TopoHelper.getSpeedMaterial(p));

//        if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorOpacity(0.25);
//            attrs.setOutlineOpacity(0.20);
//        }
        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true);
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
