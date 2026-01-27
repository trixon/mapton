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
package org.mapton.butterfly_geo_predrilling.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.RigidShape;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.geo.BGeoPreDrillPoint;
import org.mapton.butterfly_geo_predrilling.PreDrillAttributeManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends BaseGraphicRenderer<GraphicItem, BGeoPreDrillPoint> {

    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();

    private final PreDrillAttributeManager mAttributeManager = PreDrillAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer, sPlotLimiter);

        mCheckModel = checkModel;
    }

    @Override
    public void plot(BGeoPreDrillPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicItem.ALTUTID)) {
            var dateLatest = p.getDateLatest() != null ? p.getDateLatest() : LocalDate.parse("2025-01-01").atStartOfDay();
            var timeSpan = ChronoUnit.MINUTES.between(dateLatest, LocalDateTime.now());
            var altitude = timeSpan / 24000.0;
            var startPosition = WWHelper.positionFromPosition(position, 0.0);
            var endPosition = WWHelper.positionFromPosition(position, altitude);
            var radius = 0.6;
            var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            if (p.getDateLatest() == null) {
                attrs.setInteriorMaterial(Material.RED);
            }
            endEllipsoid.setAttributes(attrs);
            addRenderable(endEllipsoid, true, GraphicItem.ALTUTID, mMapObjects);

            var groundPath = new Path(startPosition, endPosition);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(groundPath, true, GraphicItem.ALTUTID, mMapObjects);
        }

//        if (mCheckModel.isChecked(GraphicItem.BALLS_Z) && drillPoint.getZeroZ() != null) {
//            var altitude = drillPoint.getZeroZ();
//            var startPosition = WWHelper.positionFromPosition(position, 0.0);
//            var endPosition = WWHelper.positionFromPosition(position, altitude);
//            var radius = 1.2;
//            var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
//            endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
//            addRenderable(endEllipsoid, true, GraphicItem.BALLS_Z, mMapObjects);
//
//            var groundPath = new Path(startPosition, endPosition);
//            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
//            addRenderable(groundPath, true, GraphicItem.BALLS_Z, mMapObjects);
//        }
        if (mCheckModel.isChecked(GraphicItem.SHAPE)) {
            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            RigidShape shape;

            var centerPosition = WWHelper.positionFromPosition(position, p.getDepth() / 2);
            var height = p.getDepth();
            var radius = p.getDiameter() / 2;

            if (p.hasDiameter()) {
                shape = new Cylinder(centerPosition, height, radius);
            } else {
                shape = new Box(centerPosition, radius, height, radius);
            }

            if (!p.hasZ()) {
                attrs.setInteriorOpacity(0.5);
            }

            if (p.getDateLatest() != null) {
                attrs.setInteriorMaterial(Material.RED);
            }

            shape.setAttributes(attrs);

            addRenderable(shape, true, GraphicItem.SHAPE, mMapObjects);

        }

//        if (mCheckModel.isChecked(GraphicItem.RECENT)) {
//            var age = drillPoint.ext().getMeasurementAge(ChronoUnit.DAYS);
//            var maxAge = 30.0;
//
//            if (age < maxAge) {
//                var circle = new SurfaceCircle(position, 40.0);
//                var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
//                var reducer = age / maxAge;//  1/30   15/30 30/30
//                var maxOpacity = 0.2;
//                var opacity = maxOpacity - reducer * maxOpacity;
//                attrs.setInteriorOpacity(opacity);
//                circle.setAttributes(attrs);
//
//                addRenderable(circle, false, GraphicItem.RECENT, null);
//            }
//        }
    }

    @Override
    public void reset() {
        super.reset();
    }

}
