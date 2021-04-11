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
package org.mapton.frww_shp;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.Logging;
import java.util.HashMap;
import org.mapton.api.MCoordinateFile;
import org.mapton.core.api.MaptonNb;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.worldwind.RandomShapeAttributes;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class ShpRenderer extends CoordinateFileRendererWW {

    private final RandomShapeAttributes mRandomShapeAttributes = new RandomShapeAttributes();
    private final HashMap<MCoordinateFile, Layer> mCoordinateFileToLayer = new HashMap<>();

    public ShpRenderer() {
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    public void render(final MCoordinateFile coordinateFile) {
        if (mCoordinateFileToLayer.containsKey(coordinateFile)) {
            mCoordinateFileToLayer.get(coordinateFile).setEnabled(coordinateFile.isVisible());
        } else {
            if (!coordinateFile.isVisible()) {
                return;
            }
            mRandomShapeAttributes.nextAttributes();

            var shapefileLayerFactory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(AVKey.SHAPEFILE_LAYER_FACTORY);
            shapefileLayerFactory.setNormalPointAttributes(mRandomShapeAttributes.asPointAttributes());
            shapefileLayerFactory.setNormalShapeAttributes(mRandomShapeAttributes.asShapeAttributes());
            String progressMessage = String.format("%s %s", Dict.OPEN.toString(), coordinateFile.getFile().getName());
            MaptonNb.progressStart(progressMessage);
            shapefileLayerFactory.createFromShapefileSource(coordinateFile.getFile(), new ShapefileLayerFactory.CompletionCallback() {
                @Override
                public void completion(Object result) {
                    SwingHelper.runLater(() -> {
                        var layer = (Layer) result;
                        getLayerBundle().getLayers().add(layer);
                        getLayerBundle().addAllChildLayers(layer);
                        mCoordinateFileToLayer.put(coordinateFile, layer);
                        layer.setEnabled(coordinateFile.isVisible());

                        MaptonNb.progressStop(progressMessage);
                    });
                }

                @Override
                public void exception(Exception e) {
                    Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                }
            });
        }
    }

    @Override
    protected void render(RenderableLayer layer) {
    }
}
