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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.ShapefileLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.Logging;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.file_opener.ShpCoordinateFileOpener;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.worldwind.RandomShapeAttributes;
import org.openide.util.Exceptions;
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
        mRandomShapeAttributes.asShapeAttributes().setDrawOutline(false);
        mRandomShapeAttributes.asAirspaceAttributes().setDrawOutline(false);

        var shapefileLayerFactory = (ShapefileLayerFactory) WorldWind.createConfigurationComponent(AVKey.SHAPEFILE_LAYER_FACTORY);
        shapefileLayerFactory.setNormalPointAttributes(mRandomShapeAttributes.asPointAttributes());
        shapefileLayerFactory.setNormalShapeAttributes(mRandomShapeAttributes.asShapeAttributes());

        var externalFile = coordinateFile.getFile();
        try {
            var tempDir = Files.createTempDirectory("mapton-shape").toFile();
            var internalFile = new File(tempDir, externalFile.getName());
            var sourceDir = externalFile.getParentFile();
            var sourceBase = FilenameUtils.getBaseName(externalFile.getName());
            var sidecarFiles = sourceDir.list(new PrefixFileFilter(sourceBase + "."));

            if (sidecarFiles != null) {
                for (var sourceName : sidecarFiles) {
                    var extension = FilenameUtils.getExtension(sourceName);
                    var srcFile = new File(sourceDir, sourceName);
                    var destFile = new File(tempDir, sourceBase + "." + extension);
                    try {
                        FileUtils.copyFile(srcFile, destFile);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }

            shapefileLayerFactory.createFromShapefileSource(internalFile, new ShapefileLayerFactory.CompletionCallback() {
                @Override
                public void completion(Object result) {
                    var keepAlive = tempDir.getAbsolutePath();

                    SwingHelper.runLater(() -> {
                        addLayer(coordinateFile, (Layer) result);
                    });
                }

                @Override
                public void exception(Exception e) {
                    Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
                }
            });
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
