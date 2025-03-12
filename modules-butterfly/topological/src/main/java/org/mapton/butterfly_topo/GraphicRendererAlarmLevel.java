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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwind.render.Wedge;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_ALTITUDE;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_SIZE;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_SIZE_ALARM;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_SIZE_ALARM_HEIGHT;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import static org.mapton.butterfly_topo.GraphicRendererBase.sMapObjects;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererAlarmLevel extends GraphicRendererBase {

    public GraphicRendererAlarmLevel(RenderableLayer layer, RenderableLayer passiveLayer) {
        super(layer, passiveLayer);
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_LEVEL)) {
            plotAlarmLevel(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
            if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION_TRACE_1)) {
                plotAlarmConsumptionTrace1(p, position);
            }
            if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION_TRACE_2)) {
                plotAlarmConsumptionTrace2(p, position);
            }
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TRACE_ALARM_LEVEL)) {
            plotAlarmLevelTrace(p, position, BComponent.HEIGHT);
            plotAlarmLevelTrace(p, position, BComponent.PLANE);
        }
    }

    private ArrayList<BTopoControlPointObservation> getHistoryObservation(BTopoControlPoint p) {
        var weeks = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        var observations = new ArrayList<BTopoControlPointObservation>();
        var lastDate = p.ext().getObservationFilteredLastDate();
        for (int week : weeks) {
            for (var o : p.ext().getObservationsTimeFiltered().reversed()) {
                if (DateHelper.isAfterOrEqual(lastDate.minusWeeks(week), o.getDate().toLocalDate())) {
                    observations.add(o);
                    break;
                }
            }
        }

        return observations;
    }

    private void plotAlarmConsumption(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION, position)) {
            return;
        }
        var alarmPercent = p.ext().getAlarmPercent();
        if (alarmPercent != null && alarmPercent < 10000) {
            plotPercentageRod(position, alarmPercent);

            var o = p.ext().getObservationFilteredLast();
            plotAlarmConsumptionObservation(p, position, o, o != p.ext().getObservationFilteredLast());
        }
    }

    private void plotAlarmConsumptionObservation(BTopoControlPoint p, Position position, BTopoControlPointObservation o, boolean historic) {
        var historicOpacity = 0.05;

        if (p.getDimension() != BDimension._1d) {
            Integer percentP = p.ext().getAlarmPercent(BComponent.PLANE);
            if (percentP == null) {
                percentP = 0;
            }
            var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelPlane(o));
            var attrs = new BasicAirspaceAttributes();
            attrs.setInteriorMaterial(material);
            if (historic) {
                attrs.setInteriorOpacity(historicOpacity);
            }
            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentP / 100.0);
            var bearing = o.ext().getBearing();
            RigidShape planeShape;
            if (bearing == null || Double.isNaN(bearing)) {
                planeShape = new Pyramid(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE);
            } else {
                var wedgeHeight = PERCENTAGE_SIZE * 0.5;
                var wedgeRadius = PERCENTAGE_SIZE * 3;
                var angle = 5.0;
//                var pos2 = WWHelper.movePolar(pos, bearing, wedgeRadius, pos.getElevation());
                planeShape = new Wedge(pos, Angle.fromDegrees(angle), wedgeHeight, wedgeRadius);
                var az = Angle.normalizedDegrees(bearing - angle / 2);
                planeShape.setHeading(Angle.fromDegrees(az));
            }
            planeShape.setAttributes(attrs);
            addRenderable(planeShape, true, null, sMapObjects);

            var alarm = p.ext().getAlarm(BComponent.PLANE);
            var alarmShape = new Box(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
            plotPercentageAlarmIndicator(position, alarm, alarmShape, true);
        }

        if (p.getDimension() != BDimension._2d) {
            Integer percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
            if (percentH == null) {
                percentH = 0;
            }

            int alarmLevel = p.ext().getAlarmLevelHeight(o);
            var dZ = o.ext().getDeltaZ();
            var rise = false;
            if (dZ != null) {
                rise = Math.signum(o.ext().getDeltaZ()) > 0;
            }
            var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);

            var scale = 0.6;
            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
            var ellipsoid = new Ellipsoid(pos, PERCENTAGE_SIZE * scale, PERCENTAGE_SIZE * scale, PERCENTAGE_SIZE * scale);
            ellipsoid.setAttributes(attrs);
            addRenderable(ellipsoid, true, null, sMapObjects);
            double heightOffset = rise ? +PERCENTAGE_SIZE * scale : -PERCENTAGE_SIZE * scale;
            pos = WWHelper.positionFromPosition(position, (PERCENTAGE_ALTITUDE * percentH / 100.0) + heightOffset);
            var pyramid = new Pyramid(pos, PERCENTAGE_SIZE * 0.75, PERCENTAGE_SIZE * 0.75);
            var a2 = new BasicAirspaceAttributes(attrs);
            a2.setInteriorMaterial(Material.WHITE);
            pyramid.setAttributes(a2);
            if (!rise) {
                pyramid.setTilt(Angle.POS180);
            }
            if (historic) {
                attrs = new BasicAirspaceAttributes(attrs);
                attrs.setInteriorOpacity(historicOpacity);
            }
            addRenderable(pyramid, false, null, null);

            var alarm = p.ext().getAlarm(BComponent.HEIGHT);
            var alarmShape = new Cylinder(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
            plotPercentageAlarmIndicator(position, alarm, alarmShape, rise);
        }
    }

    private void plotAlarmConsumptionTrace1(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION_TRACE_1, position) || p.getDimension() == BDimension._2d) {
            return;
        }

        var observations = getHistoryObservation(p);
        double cylinderHeight = 0.05;
        for (int i = 0; i < observations.size(); i++) {
            var o = observations.get(i);
            Integer percent = p.ext().getAlarmPercent(BComponent.HEIGHT, o.ext().getDeltaZ());
            if (percent == null) {
                percent = 0;
            }

            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentmAlarmLevelTrace1dAttributes());
            var opacity = Math.max(0.1, 1.0 - i * 0.15);
            attrs.setInteriorOpacity(opacity);

            var radius = 0.5 * i + 1.2;
            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percent / 100.0);
            var cylinder = new Cylinder(pos, cylinderHeight, radius);
            cylinder.setAttributes(attrs);
            addRenderable(cylinder, true, null, sMapObjects);
        }
    }

    private void plotAlarmConsumptionTrace2(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION_TRACE_2, position) || p.getDimension() == BDimension._1d) {
            return;
        }

        var observations = getHistoryObservation(p);
        for (int i = 0; i < observations.size(); i++) {
            var o = observations.get(i);
            Integer percent = p.ext().getAlarmPercent(BComponent.PLANE, o.ext().getDelta2d());
            if (percent == null) {
                percent = 0;
            }

            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentmAlarmLevelTrace2dAttributes());
            var opacity = Math.max(0.1, 1.0 - i * 0.18);
            attrs.setInteriorOpacity(opacity);

            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percent / 100.0);
            var bearing = o.ext().getBearing();
            RigidShape planeShape;
            if (bearing == null || Double.isNaN(bearing)) {
                planeShape = new Pyramid(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE);
            } else {
                var wedgeHeight = PERCENTAGE_SIZE * 0.5;
                var wedgeRadius = 2 * i + 1.5;
                var angle = 5.0;
                planeShape = new Wedge(pos, Angle.fromDegrees(angle), wedgeHeight, wedgeRadius);
                var az = Angle.normalizedDegrees(bearing - angle / 2);
                planeShape.setHeading(Angle.fromDegrees(az));
            }
            planeShape.setAttributes(attrs);
            addRenderable(planeShape, true, null, sMapObjects);
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

        addRenderable(partCyl, true, null, sMapObjects);
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

        addRenderable(partCyl, true, null, sMapObjects);
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
                addRenderable(shape, true, GraphicRendererItem.TRACE_ALARM_LEVEL, sMapObjects);
            } else if (airspace != null) {
                airspace.setAltitudes(altitude - height / 2, altitude + height / 2);
                var attrs = new BasicAirspaceAttributes();
                attrs.setInteriorMaterial(material);
                airspace.setAttributes(attrs);
                addRenderable(airspace, true, GraphicRendererItem.TRACE_ALARM_LEVEL, sMapObjects);
            }
        }
    }

    public record AlarmLevelChange(LocalDateTime localDateTime, Integer alarmLevel) {

    }
}
