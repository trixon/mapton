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
package org.mapton.butterfly_geo_reinforcement.graphics;

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
import gov.nasa.worldwind.render.SurfaceCircle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.geo.BGeoReinforcementPoint;
import org.mapton.butterfly_geo_reinforcement.ReinforcementAttributeManager;
import org.mapton.butterfly_geo_reinforcement.ReinforcementManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends BaseGraphicRenderer<GraphicItem, BGeoReinforcementPoint> {

    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();

    private final ReinforcementAttributeManager mAttributeManager = ReinforcementAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;
    private final ReinforcementManager mManager = ReinforcementManager.getInstance();
    private double mHeightOffset = 25.0;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer, sPlotLimiter);

        mCheckModel = checkModel;
    }

    @Override
    public void plot(BGeoReinforcementPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        mHeightOffset = Math.abs(mOffsetManager.getMinZ());
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicItem.ALTUTID)) {
            plotAltutid(p, position);
        }

        if (mCheckModel.isChecked(GraphicItem.SHAPE)) {
            plotShape(p, position);
        }
        if (mCheckModel.isChecked(GraphicItem.SHAPE_HOVERING)) {
            plotShapeHovering(p, position);
        }

        if (mCheckModel.isChecked(GraphicItem.RADIUS)) {
            plotRadius(p, position);
        }

        if (mCheckModel.isChecked(GraphicItem.RECENT)) {
            plotRecent(p, position);
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    private void plotAltutid(BGeoReinforcementPoint p, Position position) {
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

    private void plotRadius(BGeoReinforcementPoint p, Position position) {
        var r1 = p.getDepth() * 1.5;
        var r2 = r1 * 1.5;
        var r3 = r1 * 2.0;
        var map = Map.of(r1, Material.RED, r2, Material.ORANGE);
        List.of(r1, r2, r3).forEach(r -> {
            var circle = new SurfaceCircle(position, r);
            var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
            attrs.setDrawInterior(false);
            attrs.setDrawOutline(true);
            attrs.setOutlineMaterial(map.getOrDefault(r, Material.GREEN));
            attrs.setOutlineWidth(1.0);
            attrs.setOutlineOpacity(0.25);
            circle.setAttributes(attrs);

            addRenderable(circle, false, GraphicItem.RADIUS, null);
        });
    }

    private void plotRecent(BGeoReinforcementPoint p, Position position) {
        var age = p.ext().getMeasurementAge(ChronoUnit.DAYS);
        var maxAge = 30.0;

        if (age < maxAge) {
            var circle = new SurfaceCircle(position, 4.0);
            var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
            var reducer = age / maxAge;//  1/30   15/30 30/30
            var maxOpacity = 0.2;
            var opacity = maxOpacity - reducer * maxOpacity;
            attrs.setInteriorOpacity(opacity);
            circle.setAttributes(attrs);

            addRenderable(circle, false, GraphicItem.RECENT, null);
        }
    }

    private void plotShape(BGeoReinforcementPoint p, Position position) {
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

        if (p.getDateLatest() != null) {
            var age = p.extOrNull().getMeasurementAge(ChronoUnit.DAYS);
            attrs.setInteriorOpacity(age > 14 ? 0.5 : 1.0);
            attrs.setInteriorMaterial(Material.GREEN);
        } else {
            attrs.setInteriorMaterial(Material.ORANGE);
        }

        shape.setAttributes(attrs);

        addRenderable(shape, true, GraphicItem.SHAPE, mMapObjects);
    }

    private void plotShapeHovering(BGeoReinforcementPoint p, Position position) {
        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentEllipsoidAttributes());
        RigidShape shape;
        var topAltitude = mHeightOffset;
        var centerAltitude = topAltitude - (p.getDepth() / 2);

        var centerPosition = WWHelper.positionFromPosition(position, centerAltitude);
        var height = p.getDepth();
        var radius = p.getDiameter() / 2;
        if (p.hasDiameter()) {
            shape = new Cylinder(centerPosition, height, radius);
        } else {
            shape = new Box(centerPosition, radius, height / 2, radius);
        }

        if (p.getDateLatest() != null) {
            var age = p.extOrNull().getMeasurementAge(ChronoUnit.DAYS);
            attrs.setInteriorOpacity(age > 14 ? 0.5 : 1.0);
            attrs.setInteriorMaterial(Material.GREEN);
        } else {
            attrs.setInteriorMaterial(Material.ORANGE);
        }

        shape.setAttributes(attrs);
        addRenderable(shape, true, GraphicItem.SHAPE, mMapObjects);

        var groundPath = new Path(WWHelper.positionFromPosition(position, 0), WWHelper.positionFromPosition(position, topAltitude - height));
        var attrs2 = new BasicShapeAttributes(mAttributeManager.getComponentGroundPathAttributes());
        groundPath.setAttributes(attrs2);
        addRenderable(groundPath, false, null, null);
    }

}
