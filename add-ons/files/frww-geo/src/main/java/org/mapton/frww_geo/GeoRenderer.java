/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.frww_geo;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.PointPlacemark;
import java.io.IOException;
import java.util.List;
import org.mapton.api.MCoordinateFile;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoPoint;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class GeoRenderer extends CoordinateFileRendererWW {

    private BasicShapeAttributes mLineBasicShapeAttributes;

    public GeoRenderer() {
        initAttributes();
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        mCooTrans = coordinateFile.getCooTrans();
        var layer = new RenderableLayer();
        Geo geo = new Geo();
        try {
            geo.read(coordinateFile.getFile());
            renderPoints(layer, geo.getPoints());
            System.out.println(geo.getLines().size());
            //Dynamic transform
            //Create Points
            //Create Lines
            System.out.println(geo.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        addLayer(coordinateFile, layer);
    }

    @Override
    protected void render() {
        for (var coordinateFile : mCoordinateFileManager.getSublistByExtensions("geo")) {
            render(coordinateFile);
        }
    }

    private void initAttributes() {
        mLineBasicShapeAttributes = new BasicShapeAttributes();
    }

    private void renderPoints(RenderableLayer layer, List<GeoPoint> geoPoints) {
        for (var geoPoint : geoPoints) {
            if (mCooTrans.isWithinProjectedBounds(geoPoint.getX(), geoPoint.getY())) {
                var p = mCooTrans.toWgs84(geoPoint.getX(), geoPoint.getY());
                var pointPlacemark = new PointPlacemark(Position.fromDegrees(p.getY(), p.getX()));

                pointPlacemark.setLabelText(geoPoint.getPointId());
                pointPlacemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                pointPlacemark.setEnableLabelPicking(true);

                layer.addRenderable(pointPlacemark);
            }
        }
    }
}
