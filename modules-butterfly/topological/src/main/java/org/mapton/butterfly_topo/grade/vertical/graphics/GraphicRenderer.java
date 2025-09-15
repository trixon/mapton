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
package org.mapton.butterfly_topo.grade.vertical.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwind.render.Wedge;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoAttributeManager;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    public static final double MAX = 5.0;
    public static final double MAX_PER_MILLE = 3.0;
    public static final double MIN = 2.0;
    public static final double MID = MAX - (MAX - MIN) / 2;
    public static final double SPAN = MAX - MID;
    private final HashSet<BTopoControlPoint> mPlottedConnectors = new HashSet();
    private final HashSet<BTopoControlPoint> mPlottedNames = new HashSet();
    private final HashSet<BTopoControlPoint> mPlottedPoints = new HashSet();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BTopoGrade p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;

        var pos1 = BCoordinatrix.toPositionWW3d(p.getP1());
        var pos2 = BCoordinatrix.toPositionWW3d(p.getP2());

        if (sCheckModel.isChecked(GraphicItem.PIN)) {
            plotPin(p, position);
        }
        if (sCheckModel.isChecked(GraphicItem.VALUE)) {
            plotValue(p, position);
        }
        if (sCheckModel.isChecked(GraphicItem.BEARING)) {
            plotBearing(p, position);
        }
        if (sCheckModel.isChecked(GraphicItem.POINTS)) {
            plotRefPoints(p, pos1, pos2);
        }
        if (sCheckModel.isChecked(GraphicItem.INDICATOR)) {
            plotIndicator(p, position, pos1, pos2);
        }
        if (sCheckModel.isChecked(GraphicItem.TRACE)) {
            plotTrace(p, position);
        }
    }

    @Override
    public void reset() {
        resetPlotLimiter();
        sPointToPositionMap.clear();
        mPlottedConnectors.clear();
        mPlottedNames.clear();
        mPlottedPoints.clear();
        clearLabeledPoints(BTopoGrade.class);
    }

    private void plotBearing(BTopoGrade p, Position position) {
        int size = p.getCommonObservations().size();
        if (!sCheckModel.isChecked(GraphicItem.BEARING)
                || size == 0) {
            return;
        }

        int maxNumberOfItemsToPlot = Math.min(10, size);
        var keys = new ArrayList<>(p.getCommonObservations().keySet());
        boolean first = true;

        for (int i = size - 1; i >= size - maxNumberOfItemsToPlot + 1; i--) {
            var o = p.getCommonObservations().get(keys.get(i));
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), o);

            try {
                var bearing = gradeDiff.getBearing();
                if (bearing == null || bearing.isNaN()) {
                    first = false;
                    continue;
                }

                var length = 10.0;
                var p2 = WWHelper.movePolar(position, bearing, length);
                var z = position.elevation - (first ? 0.2 : 0.1);
                position = WWHelper.positionFromPosition(position, z);
                p2 = WWHelper.positionFromPosition(p2, z);
                var path = new Path(position, p2);
                path.setAttributes(TopoAttributeManager.getInstance().getBearingAttribute(first));
                first = false;

                addRenderable(path, true, null, null);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    private void plotIndicator(BTopoGrade p, Position position, Position pos1, Position pos2) {
        var topPath = new Path(position, WWHelper.positionFromPosition(position, pos2.elevation));
        topPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(topPath, true, null, sMapObjects);

        var alarm = p.ext().getAlarmP1(BComponent.PLANE);
        var scale = p.getDistanceHeight() * 0.5 * 1000;
        var scaledDistance = p.ext().getDiff().getRQuota() * scale;
        var bearing = p.ext().getDiff().getBearing();
        var centerHiPosition = WWHelper.positionFromPosition(position, pos2.elevation);
        var centerLoPosition = WWHelper.positionFromPosition(position, pos1.elevation);
        var endHiPosition = WWHelper.movePolar(centerHiPosition, bearing, scaledDistance, centerHiPosition.elevation);
        var endLoPosition = WWHelper.movePolar(centerLoPosition, bearing - 180, scaledDistance, centerLoPosition.elevation);
        var gradePath = new Path(endHiPosition, endLoPosition);
        var attrs0 = TopoAttributeManager.getInstance().getComponentVectorAttributes(TopoHelper.getAlarmLevel(p));
        gradePath.setAttributes(attrs0);
        addRenderable(gradePath, true, null, sMapObjects);

        if (alarm != null) {
            List.of(pos1, pos2).forEach(poz -> {
                var min = 0.0;
                for (int i = 0; i < alarm.ext().getRatioRanges().size(); i++) {
                    var bearingAlarmIndicator = bearing;

                    if (poz == pos1) {
                        bearingAlarmIndicator = bearingAlarmIndicator - 180;
                    }
                    var range = alarm.ext().getRatioRanges().get(i);
                    var max = range.getMaximum();
                    var scaledMin = min * scale;
                    var scaledMax = max * scale;
                    min = max;
                    var pos0 = WWHelper.positionFromPosition(position, poz.elevation);
                    var pA = WWHelper.movePolar(pos0, bearingAlarmIndicator, scaledMin, poz.elevation);
                    var pB = WWHelper.movePolar(pos0, bearingAlarmIndicator, scaledMax, poz.elevation);
                    var alarmSegmentPath = new Path(pA, pB);
                    var attrs = TopoAttributeManager.getInstance().getComponentVectorAttributes(i);
                    alarmSegmentPath.setAttributes(attrs);
                    addRenderable(alarmSegmentPath, false, null, sMapObjects);
                }
            });
        }
    }

    private void plotPin(BTopoGrade p, Position position) {
        var baseSize = 0.5;
        var midEllipsoid = new Ellipsoid(position, baseSize, baseSize, baseSize);
        midEllipsoid.setAttributes(mAttributeManager.getGradeVectorCurrentAttributes(p));
        addRenderable(midEllipsoid, true, GraphicItem.PIN, sMapObjects);

        var groundPath = new Path(WWHelper.positionFromPosition(position, 0), position);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, null, sMapObjects);

        plotLabel(p, position);
    }

    private void plotRefPoint(BTopoControlPoint p, double baseSize, Position pos) {
        if (!mPlottedPoints.contains(p)) {
            var end_size = 0.5 * baseSize;
            var ellipsoid = new Ellipsoid(pos, end_size, end_size, end_size);
            ellipsoid.setAttributes(mTopoAttributeManager.getComponentVectorCurrentAttributes(p));
            addRenderable(ellipsoid, true, GraphicItem.PIN, null);
            var leftClickRunnable = (Runnable) () -> {
                mTopoManager.setSelectedItemAfterReset(p);
            };

            ellipsoid.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        }
    }

    private void plotRefPoints(BTopoGrade p, Position pos1, Position pos2) {
        var baseSize = 0.5;
        plotRefPoint(p.getP1(), baseSize, pos1);
        plotRefPoint(p.getP2(), baseSize, pos2);

        var midPath = new Path(pos1, pos2);
        midPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(midPath, true, null, sMapObjects);
    }

    private void plotTrace(BTopoGrade p, Position position) {
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;
        var dates = new ArrayList<>(p.getCommonObservations().keySet()).reversed();

        for (int i = 0; i < dates.size(); i++) {
            var date = dates.get(i);
            var o = p.getCommonObservations().get(dates.get(i));
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), o);
            var timeSpan = ChronoUnit.MINUTES.between(date.atStartOfDay(), prevDate);
            var height = timeSpan / 24000.0;
            height = Math.max(height, 0.01);
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = date.atStartOfDay();
            prevHeight = height;
            var pos = WWHelper.positionFromPosition(position, altitude);
            var radius = Math.max(gradeDiff.getRQuota() * 10000, 0.1);
            var attrs = TopoAttributeManager.getInstance().getComponentVectorCurrentAttributes(TopoHelper.getAlarmLevel(p, gradeDiff));
            var bearing = gradeDiff.getBearing();
            RigidShape rigidShape;

            if (bearing == null || Double.isNaN(bearing)) {
                rigidShape = new Ellipsoid(pos, radius, height * .5, radius);
            } else {
                var angle = 5.0;
                rigidShape = new Wedge(pos, Angle.fromDegrees(angle), height, radius);
                var az = Angle.normalizedDegrees(bearing - angle / 2);
                rigidShape.setHeading(Angle.fromDegrees(az));
            }

            rigidShape.setAttributes(attrs);
            addRenderable(rigidShape, true, null, sMapObjects);
        }
    }

    private void plotValue(BTopoGrade p, Position position) {
        int size = p.getCommonObservations().size();
        if (size == 0) {
            return;
        }

        int maxNumberOfItemsToPlot = Math.min(10, size);
        var keys = new ArrayList<>(p.getCommonObservations().keySet());
        boolean first = true;
        for (int i = size - 1; i >= size - maxNumberOfItemsToPlot + 1; i--) {
            var o = p.getCommonObservations().get(keys.get(i));
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), o);

            try {
                var bearing = gradeDiff.getBearing();
                if (bearing == null || bearing.isNaN()) {
                    var attrs = TopoAttributeManager.getInstance().getComponentVectorCurrentAttributes(TopoHelper.getAlarmLevel(p, gradeDiff));
                    var circle = new Cylinder(position, 0.02, 0.75);
                    circle.setAttributes(attrs);
                    addRenderable(circle, true, null, null);
                    first = false;
                    continue;
                }

                var length = gradeDiff.getRQuota() * 100000;
                var p2 = WWHelper.movePolar(position, bearing, length);
                var z = position.elevation - (first ? 0.2 : 0.1);
                position = WWHelper.positionFromPosition(position, z);
                p2 = WWHelper.positionFromPosition(p2, z);
                var path = new Path(position, p2);
                var attrs = TopoAttributeManager.getInstance().getComponentVectorAttributes(TopoHelper.getAlarmLevel(p, gradeDiff));
                if (!first) {
                    attrs = new BasicShapeAttributes(attrs);
                    attrs.setOutlineOpacity(0.1);
                }
                path.setAttributes(attrs);
                first = false;

                addRenderable(path, true, null, null);
            } catch (Exception e) {
                System.err.println(e);
            }

            if (!sCheckModel.isChecked(GraphicItem.VALUE_TRACE)) {
                break;
            }
        }
    }
}
