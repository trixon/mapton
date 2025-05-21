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
package org.mapton.butterfly_acoustic.blast;

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
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends BaseGraphicRenderer<GraphicRendererItem, BAcousticBlast> {

    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();

    private final BlastAttributeManager mAttributeManager = BlastAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer, sPlotLimiter);

        mCheckModel = checkModel;
    }

    public void plot(BAcousticBlast blast, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicRendererItem.BALLS)) {
            var timeSpan = ChronoUnit.MINUTES.between(blast.getDateLatest(), LocalDateTime.now());
            var altitude = timeSpan / 24000.0;
            var startPosition = WWHelper.positionFromPosition(position, 0.0);
            var endPosition = WWHelper.positionFromPosition(position, altitude);
            var radius = 1.2;
            var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
            endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(endEllipsoid, true, GraphicRendererItem.BALLS, mMapObjects);

            var groundPath = new Path(startPosition, endPosition);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(groundPath, true, GraphicRendererItem.BALLS, mMapObjects);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.BALLS_Z) && blast.getZeroZ() != null) {
            var altitude = blast.getZeroZ();
            var startPosition = WWHelper.positionFromPosition(position, 0.0);
            var endPosition = WWHelper.positionFromPosition(position, altitude);
            var radius = 1.2;
            var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
            endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(endEllipsoid, true, GraphicRendererItem.BALLS_Z, mMapObjects);

            var groundPath = new Path(startPosition, endPosition);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(groundPath, true, GraphicRendererItem.BALLS_Z, mMapObjects);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.RADIUS_40)) {
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

                addRenderable(circle, false, GraphicRendererItem.RADIUS_40, null);
            });

        }

        if (mCheckModel.isChecked(GraphicRendererItem.RECENT)) {
            var age = blast.ext().getMeasurementAge(ChronoUnit.DAYS);
            var maxAge = 30.0;

            if (age < maxAge) {
                var circle = new SurfaceCircle(position, 40.0);
                var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
                var reducer = age / maxAge;//  1/30   15/30 30/30
                var maxOpacity = 0.2;
                var opacity = maxOpacity - reducer * maxOpacity;
                attrs.setInteriorOpacity(opacity);
                circle.setAttributes(attrs);

                addRenderable(circle, false, GraphicRendererItem.RECENT, null);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

}
