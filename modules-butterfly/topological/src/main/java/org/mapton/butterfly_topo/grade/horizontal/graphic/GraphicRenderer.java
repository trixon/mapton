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
package org.mapton.butterfly_topo.grade.horizontal.graphic;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import java.util.HashSet;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MOptions;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
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

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BTopoGrade p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;
//        plotBearing(p, position);

        var pos1 = BCoordinatrix.toPositionWW2d(p.getP1());
        var pos2 = BCoordinatrix.toPositionWW2d(p.getP2());

        if (sCheckModel.isChecked(GraphicItem.HOR_INDICATOR)) {
            plotHorIndicator(p, position, pos1, pos2, mapObjects);
        }
        if (sCheckModel.isChecked(GraphicItem.VER_INDICATOR)) {
            plotVerIndicator(p, position, pos1, pos2, mapObjects);
        }
        if (sCheckModel.isChecked(GraphicItem.NAME)) {
            plotName(p, position, pos1, pos2);
        }
    }

    @Override
    public void reset() {
        resetPlotLimiter();
        sPointToPositionMap.clear();
        mPlottedConnectors.clear();
        mPlottedNames.clear();
    }

    private void plotHorIndicator(BTopoGrade p, Position position, Position pos1, Position pos2, ArrayList<AVListImpl> mapObjects) {
        if (getPlotLimiter().isLimitReached(GraphicItem.HOR_INDICATOR, p.getName())) {
            return;
        }

        plotIndicatorGroundPath(pos1, p.getP1());
        plotIndicatorGroundPath(pos2, p.getP2());

        var z = Math.abs(p.ext().getDiff().getZPerMille()) / MAX_PER_MILLE * SPAN;

        double z1;
        double z2;

        if (Math.signum(p.ext().getDiff().getZPerMille()) > 0) {
            z1 = MID - z;
            z2 = MID + z;
        } else {
            z1 = MID + z;
            z2 = MID - z;
        }

        var path = new Path(WWHelper.positionFromPosition(pos1, z1), WWHelper.positionFromPosition(pos2, z2));
        path.setAttributes(mAttributeManager.getGradeVectorAttributes(p));
        addRenderable(path, true, GraphicItem.HOR_INDICATOR, sMapObjects);

        mapObjects.add(path);
    }

    private void plotIndicatorGroundCylinder(Position position, double z, double r) {
        var cylinder = new Cylinder(WWHelper.positionFromPosition(position, z), 0.05, r);
        cylinder.setAttributes(mAttributeManager.getGroundCylinderAttributes());
        addRenderable(cylinder, false, null, null);
    }

    private void plotIndicatorGroundPath(Position position, BTopoControlPoint point) {
        if (!mPlottedConnectors.contains(point)) {
            var groundPath = new Path(position, WWHelper.positionFromPosition(position, MAX + .3));
            groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
            addRenderable(groundPath, false, null, null);

            mPlottedConnectors.add(point);
            plotIndicatorGroundCylinder(position, MAX, 0.1);
            plotIndicatorGroundCylinder(position, MID, 0.05);
            plotIndicatorGroundCylinder(position, MIN, 0.1);
        }
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

    private void plotVerIndicator(BTopoGrade p, Position position, Position pos1, Position pos2, ArrayList<AVListImpl> mapObjects) {
        if (getPlotLimiter().isLimitReached(GraphicItem.VER_INDICATOR, p.getName())) {
            return;
        }

        var cootrans = MOptions.getInstance().getMapCooTrans();
        var wgs = cootrans.toWgs84(p.ext().getMidPoint().getY(), p.ext().getMidPoint().getX());

        var begPos = Position.fromDegrees(wgs.getY(), wgs.getX(), 0.0);
        var height = Math.abs(p.ext().getDiff().getZPerMille() * 25);
        var endPos = WWHelper.positionFromPosition(begPos, height);

        var path = new Path(begPos, endPos);
        var attrs = mAttributeManager.getGradeVectorAttributes(p);
        attrs.setOutlineWidth(4.0);
        path.setAttributes(attrs);
        addRenderable(path, true, GraphicItem.HOR_INDICATOR, sMapObjects);

        mapObjects.add(path);
    }

}
