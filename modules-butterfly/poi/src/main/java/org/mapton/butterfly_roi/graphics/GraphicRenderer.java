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
package org.mapton.butterfly_roi.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.BRoi;
import org.mapton.butterfly_roi.RoiAttributeManager;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends BaseGraphicRenderer<GraphicItem, BRoi> {

    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();

    private final RoiAttributeManager mAttributeManager = RoiAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer, sPlotLimiter);

        mCheckModel = checkModel;
    }

    @Override
    public void plot(BRoi roi, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicItem.SURFACE)) {
            plotArea(roi);
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    private void plotArea(BRoi roi) {
        var attrs = mAttributeManager.getSurfaceAttributes(roi);
        var attrsHighlight = mAttributeManager.getSurfaceHighlightAttributes(roi);
        Renderable renderable = null;
        try {
            var geometry = roi.getGeometry();
            switch (geometry) {
                case LineString lineString -> {
                    var surfaceObject = new SurfacePolyline(attrs, WWHelper.positionsFromGeometry(lineString, 0));
                    surfaceObject.setHighlightAttributes(attrsHighlight);
                    renderable = surfaceObject;
                }
                case Polygon polygon -> {
                    var surfaceObject = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
                    surfaceObject.setHighlightAttributes(attrsHighlight);
                    renderable = surfaceObject;
                }
                case MultiPolygon multiPolygon -> {
                    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                        var polygon = (Polygon) multiPolygon.getGeometryN(i);
                        var surfaceObject = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
                        surfaceObject.setHighlightAttributes(attrsHighlight);
                        renderable = surfaceObject;
                    }
                }
                default -> {
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        if (renderable != null) {
            addRenderable(renderable, true, GraphicItem.SURFACE, mMapObjects);

        }
    }

}
