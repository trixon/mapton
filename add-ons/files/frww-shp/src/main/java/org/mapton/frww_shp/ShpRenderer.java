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
package org.mapton.frww_shp;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.file_opener.ShpCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.worldwind.RandomShapeAttributes;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class ShpRenderer extends CoordinateFileRendererWW {

    private final RandomShapeAttributes mRandomShapeAttributes = new RandomShapeAttributes();

    public ShpRenderer() {
        addSupportedFileOpeners(ShpCoordinateFileOpener.class);
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        mRandomShapeAttributes.nextAttributes();

        var shapefileLayerFactory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(AVKey.SHAPEFILE_LAYER_FACTORY);
        shapefileLayerFactory.setNormalPointAttributes(mRandomShapeAttributes.asPointAttributes());
        shapefileLayerFactory.setNormalShapeAttributes(mRandomShapeAttributes.asShapeAttributes());

        shapefileLayerFactory.createFromShapefileSource(coordinateFile.getFile(), new ShapefileLayerFactory.CompletionCallback() {
            @Override
            public void completion(Object result) {
                SwingHelper.runLater(() -> {
                    addLayer(coordinateFile, (Layer) result);
                });
            }

            @Override
            public void exception(Exception e) {
                Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            }
        });
    }

    @Override
    protected void render() {
        for (var coordinateFile : mCoordinateFileManager.getSublistBySupportedOpeners(getSupportedFileOpeners())) {            
            render(coordinateFile);
        }
    }
}
