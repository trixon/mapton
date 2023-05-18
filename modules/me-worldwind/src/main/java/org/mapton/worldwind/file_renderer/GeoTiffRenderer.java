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
package org.mapton.worldwind.file_renderer;

import gov.nasa.worldwind.layers.SurfaceImageLayer;
import java.io.IOException;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.file_opener.GeoTiffCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class GeoTiffRenderer extends CoordinateFileRendererWW {

    public GeoTiffRenderer() {
        addSupportedFileOpeners(GeoTiffCoordinateFileOpener.class);
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        try {
            var sourceFile = coordinateFile.getFile();
            var layer = new SurfaceImageLayer();
            layer.setPickEnabled(false);
            layer.addImage(sourceFile.getPath());
            layer.addPropertyChangeListener("Enabled", pce -> {
                var layers = LayerBundleManager.getInstance().getWwd().getModel().getLayers();
                layers.remove(layer);
                layers.add(layer);
            });
            addLayer(coordinateFile, layer);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void render() {
        for (var coordinateFile : mCoordinateFileManager.getSublistBySupportedOpeners(getSupportedFileOpeners())) {
            render(coordinateFile);
        }
    }
}
