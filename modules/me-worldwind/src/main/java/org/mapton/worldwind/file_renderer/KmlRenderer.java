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

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.file_opener.KmlCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CoordinateFileRendererWW.class)
public class KmlRenderer extends CoordinateFileRendererWW {

    public KmlRenderer() {
        addSupportedFileOpeners(KmlCoordinateFileOpener.class);
    }

    @Override
    public void init(LayerBundle layerBundle) {
        setLayerBundle(layerBundle);
    }

    @Override
    protected void load(MCoordinateFile coordinateFile) {
        var layer = new RenderableLayer();
        try {
            var kmlRoot = KMLRoot.createAndParse(coordinateFile.getFile());
            layer.addRenderable(new KMLController(kmlRoot));
            addLayer(coordinateFile, layer);
        } catch (IOException | XMLStreamException ex) {
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
