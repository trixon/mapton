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
package org.mapton.butterfly_topo.grade.distance.graphic;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.RigidShape;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.butterfly_topo.TopoLayerBundle;
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
        initScales();

        var pos1 = BCoordinatrix.toPositionWW3d(p.getP1());
        var pos2 = BCoordinatrix.toPositionWW3d(p.getP2());

        if (sCheckModel.isChecked(GraphicItem.POINTS)) {
            plotRefPoints(p, pos1, pos2);
        }

        if (sCheckModel.isChecked(GraphicItem.NAME)) {
            plotName(p, position, pos1, pos2);
        }

        if (sCheckModel.isChecked(GraphicItem.VECTOR_3D)) {
            var p1 = plot3dVector(p.getP1(), true);
            var p2 = plot3dVector(p.getP2(), true);
            if (sCheckModel.isChecked(GraphicItem.VECTOR_3D_CONNECTOR)) {
                plotDistancePath(p, p1, p2);
            }
        }

        if (sCheckModel.isChecked(GraphicItem.TRACE_1D)) {
            plot1d(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VERTICAL_INDICATOR)) {
            plotVerticalIndicator(p, position);
        }
    }

    @Override
    public void reset() {
        resetPlotLimiter();
        sPointToPositionMap.clear();
        mPlottedConnectors.clear();
        mPlottedNames.clear();
        mPlottedPoints.clear();
    }

    private void plot1d(BTopoGrade p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.TRACE_1D, position)) {
            return;
        }

        var reversedList = new ArrayList<>(p.getCommonObservations().values()).reversed();
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = FastMath.max(0.01, timeSpan / 24000.0);

            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 2.0;
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), o);

            var dZ = gradeDiff.getPartialDiffDistance() / 1000.0;
            var radius = Math.min(maxRadius, Math.abs(dZ / 5.0) * mScale1dH + 0.05);
            var maximus = radius == maxRadius;

            RigidShape shape;
            if (dZ > 0) {
                shape = new Box(pos, radius, height, radius);
            } else {
                shape = new Cylinder(pos, height, radius);
            }

            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentTrace1dAttributes(-1, false, maximus));
            attrs.setInteriorMaterial(TopoHelper.getGradeDistanceMaterial(p));
            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                attrs.setInteriorOpacity(0.25);
                attrs.setOutlineOpacity(0.20);
            }

            shape.setAttributes(attrs);
            addRenderable(shape, true, GraphicItem.TRACE_1D, sMapObjects);
        }
    }

    private Position plot3dVector(BTopoControlPoint p, boolean plotBalls) {
        if (!isValidFor3dPlot(p)) {
            return null;
        }
        var position = Position.fromDegrees(p.getLat(), p.getLon());
        var positions = plot3dOffsetPole(p, position, plotBalls, 1.0, plotBalls);
        var startPosition = positions[0];
        var endPosition = positions[1];

        var path = new Path(startPosition, endPosition);
        path.setAttributes(mTopoAttributeManager.getComponentVector3dAttributes(p));
        addRenderable(path, true, null, sMapObjects);

        plotLabel(p, positions[0]);

        return endPosition;
    }

    private void plotDistancePath(BTopoGrade p, Position p1, Position p2) {
        var midPath = new Path(p1, p2);
        midPath.setAttributes(mAttributeManager.getComponentDistancePathAttributes(p));
        addRenderable(midPath, true, null, sMapObjects);
    }

    private void plotName(Position position, BTopoControlPoint point) {
        if (!mPlottedNames.contains(point)) {
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText(point.getName());

            addRenderable(placemark, true, GraphicItem.NAME, sMapObjects);
            mPlottedNames.add(point);
        }
    }

    private void plotName(BTopoGrade p, Position position, Position pos1, Position pos2) {
        if (getPlotLimiter().isLimitReached(GraphicItem.NAME, p.getName())) {
            return;
        }

        plotName(pos1, p.getP1());
        plotName(pos2, p.getP2());
    }

    private void plotRefPoint(BTopoControlPoint p, double baseSize, Position pos) {
        if (!mPlottedPoints.contains(p)) {
            var end_size = 0.5 * baseSize;
            var ellipsoid = new Ellipsoid(pos, end_size, end_size, end_size);
            ellipsoid.setAttributes(mTopoAttributeManager.getComponentVectorCurrentAttributes(p));
            addRenderable(ellipsoid, true, org.mapton.butterfly_topo.grade.vertical.graphics.GraphicItem.PIN, null);
            var leftClickRunnable = (Runnable) () -> {
                mTopoManager.setSelectedItemAfterReset(p);
            };

            ellipsoid.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        }
    }

    private void plotRefPoints(BTopoGrade p, Position pos1, Position pos2) {
        var baseSize = 0.5;
        var p1 = WWHelper.positionFromPosition(pos1, pos1.elevation + TopoLayerBundle.getZOffset());
        var p2 = WWHelper.positionFromPosition(pos2, pos2.elevation + TopoLayerBundle.getZOffset());

        plotRefPoint(p.getP1(), baseSize, p1);
        plotRefPoint(p.getP2(), baseSize, p2);

        var midPath = new Path(p1, p2);
        midPath.setAttributes(mAttributeManager.getComponentDistancePathAttributes(p));
        addRenderable(midPath, true, null, sMapObjects);

        var path1 = new Path(p1, WWHelper.positionFromPosition(p1, 0));
        var path2 = new Path(p2, WWHelper.positionFromPosition(p2, 0));
        for (var path : List.of(path1, path2)) {
            path.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(path, false, null, sMapObjects);
        }
    }

    private void plotVerticalIndicator(BTopoGrade p, Position position) {
        if (getPlotLimiter().isLimitReached(GraphicItem.VERTICAL_INDICATOR, p.getName())) {
            return;
        }

        var gradeDiff = p.ext().getDiff();
        var z = gradeDiff.getPartialDiffDistance();
        var radius = 1.0;
        var height = Math.abs(z) * 2;
        position = WWHelper.positionFromPosition(position, height / 2.0);

        RigidShape shape;
        if (z > 0) {
            shape = new Box(position, radius, height, radius);
        } else {
            shape = new Cylinder(position, height, radius);
        }

        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentTrace1dAttributes(-1, false, false));
        attrs.setInteriorMaterial(TopoHelper.getGradeDistanceMaterial(p));
        shape.setAttributes(attrs);
        addRenderable(shape, true, GraphicItem.TRACE_1D, sMapObjects);
    }

}
