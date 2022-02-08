/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.worldwind.file_renderer;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.file_opener.GeoCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoLine;
import se.trixon.almond.util.io.GeoPoint;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class GeoRenderer extends CoordinateFileRendererWW {

    private BasicShapeAttributes mLineBasicShapeAttributes;

    public GeoRenderer() {
        addSupportedFileOpeners(GeoCoordinateFileOpener.class);
        initAttributes();
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        mCooTrans = coordinateFile.getCooTrans();
        new Thread(() -> {
            try {
                var geo = new Geo();
                geo.read(coordinateFile.getFile());
                var layer = new RenderableLayer();
                layer.setPickEnabled(false);
                renderPoints(layer, geo.getPoints());
                renderLines(layer, geo.getLines());
                addLayer(coordinateFile, layer);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }, getClass().getName() + " Load").start();
    }

    @Override
    protected void render() {
        for (var coordinateFile : mCoordinateFileManager.getSublistBySupportedOpeners(getSupportedFileOpeners())) {
            render(coordinateFile);
        }
    }

    private void initAttributes() {
        mLineBasicShapeAttributes = new BasicShapeAttributes();
        mLineBasicShapeAttributes.setOutlineMaterial(Material.RED);
        mLineBasicShapeAttributes.setOutlineWidth(1.0D);
    }

    private void renderLines(RenderableLayer layer, List<GeoLine> geoLines) {
        for (var geoLine : geoLines) {
            var positions = new ArrayList<Position>();
            for (var geoPoint : geoLine.getPoints()) {
                if (mCooTrans.isWithinProjectedBounds(geoPoint.getX(), geoPoint.getY())) {
                    var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
                    positions.add(Position.fromDegrees(p.getY(), p.getX(), MathHelper.convertDoubleToDouble(geoPoint.getZ())));
                }
            }
            if (positions.size() > 1) {
                var path = new Path(positions);
                path.setAttributes((ShapeAttributes) mLineBasicShapeAttributes);
                path.setAltitudeMode(0);
                layer.addRenderable((Renderable) path);
            }
        }
    }

    private void renderPoints(RenderableLayer layer, List<GeoPoint> geoPoints) {
        for (var geoPoint : geoPoints) {
            if (mCooTrans.isWithinProjectedBounds(geoPoint.getX(), geoPoint.getY())) {
                var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
                var pointPlacemark = new PointPlacemark(Position.fromDegrees(p.getY(), p.getX()));
                pointPlacemark.setLabelText(geoPoint.getPointId());
                pointPlacemark.setAltitudeMode(1);
                pointPlacemark.setEnableLabelPicking(true);
                layer.addRenderable((Renderable) pointPlacemark);
            }
        }
    }
}
