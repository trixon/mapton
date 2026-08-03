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
package org.mapton.butterfly_misc_xyz.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.SurfaceCircle;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);

        sCheckModel = checkModel;
    }

    @Override
    public void plot(BXyzPoint xyz, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicItem.ALTUTID)) {
            plotAltutid(xyz, position);
        }

        if (sCheckModel.isChecked(GraphicItem.BALLS_Z) && xyz.getZeroZ() != null) {
            plotBallsZ(xyz, position);
        }

        if (sCheckModel.isChecked(GraphicItem.TRACE)) {
            plotTrace(xyz, position);
        }
        if (sCheckModel.isChecked(GraphicItem.RADIUS_40)) {
            plotRadius(xyz, position);
        }

        if (sCheckModel.isChecked(GraphicItem.RECENT)) {
//            plotRecent(xyz, position);
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    private void plotAltutid(BXyzPoint xyz, Position position) {
        var timeSpan = ChronoUnit.MINUTES.between(xyz.getDateLatest(), LocalDateTime.now());
        var altitude = timeSpan / 24000.0;
        var startPosition = WWHelper.positionFromPosition(position, 0.0);
        var endPosition = WWHelper.positionFromPosition(position, altitude);
        var radius = 1.2;
        var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
        endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
        addRenderable(endEllipsoid, true, GraphicItem.ALTUTID, mMapObjects);

        var groundPath = new Path(startPosition, endPosition);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, GraphicItem.ALTUTID, mMapObjects);
    }

    private void plotBallsZ(BXyzPoint xyz, Position position) {
        var altitude = xyz.getZeroZ();
        var startPosition = WWHelper.positionFromPosition(position, 0.0);
        var endPosition = WWHelper.positionFromPosition(position, altitude);
        var radius = 1.2;
        var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
        endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
        addRenderable(endEllipsoid, true, GraphicItem.BALLS_Z, mMapObjects);

        var groundPath = new Path(startPosition, endPosition);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, GraphicItem.BALLS_Z, mMapObjects);
    }

    private void plotRadius(BXyzPoint xyz, Position position) {
        var map = Map.of(40.0, Material.RED, 50.0, Material.ORANGE);
        List.of(40.0, 50.0, 100.0).forEach(r -> {
            var circle = new SurfaceCircle(position, r);
            var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
            attrs.setDrawInterior(false);
            attrs.setDrawOutline(true);
            attrs.setOutlineMaterial(map.getOrDefault(r, Material.GREEN));
            attrs.setOutlineWidth(1.0);
            attrs.setOutlineOpacity(0.25);
            circle.setAttributes(attrs);

            addRenderable(circle, false, GraphicItem.RADIUS_40, null);
        });

    }

//    private void plotRecent(BXyzPoint xyz, Position position) {
//        var age = xyz.ext().getMeasurementAge(ChronoUnit.DAYS);
//        var maxAge = 30.0;
//
//        if (age < maxAge) {
//            var circle = new SurfaceCircle(position, 40.0);
//            var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
//            var reducer = age / maxAge;//  1/30   15/30 30/30
//            var maxOpacity = 0.2;
//            var opacity = maxOpacity - reducer * maxOpacity;
//            attrs.setInteriorOpacity(opacity);
//            circle.setAttributes(attrs);
//
//            addRenderable(circle, false, GraphicItem.RECENT, null);
//        }
//    }
    private void plotTrace(BXyzPoint xyz, Position position) {
        var circle = new SurfaceCircle(position, 5.0);
        var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
        attrs.setDrawInterior(true);
        attrs.setDrawOutline(false);
        attrs.setInteriorMaterial(Material.ORANGE);
        circle.setAttributes(attrs);

        addRenderable(circle, false, GraphicItem.TRACE, null);
    }

}
