/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.files_nb;

import gov.nasa.worldwind.layers.RenderableLayer;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import org.apache.commons.io.FilenameUtils;
import org.mapton.addon.files_nb.api.Document;
import org.mapton.addon.files_nb.api.DocumentManager;
import org.mapton.addon.files_nb.renderers.GeoRenderer;
import org.mapton.addon.files_nb.renderers.KmlRenderer;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class FilesLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final DocumentManager mManager = DocumentManager.getInstance();

    public FilesLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Dict.FILES.toString());
        setCategoryAddOns(mLayer);
        setName(Dict.FILES.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
        attachTopComponentToLayer("FilesTopComponent", mLayer);
    }

    private void initListeners() {
        DelayedResetRunner delayedResetRunner = new DelayedResetRunner(100, () -> {
            repaint();
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends Document> c) -> {
            delayedResetRunner.reset();
        });

        mManager.updatedProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            delayedResetRunner.reset();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            mLayer.removeAllRenderables();

            for (Document document : mManager.getItems()) {
                if (document.isVisible()) {
                    File file = document.getFile();
                    if (file.isDirectory()) {
                        //TODO
                    } else if (file.isFile()) {
                        switch (FilenameUtils.getExtension(file.getName())) {
                            case "geo":
                                GeoRenderer.render(file, mLayer);
                                break;

                            case "kml":
                            case "kmz":
                                KmlRenderer.render(file, mLayer);
                                break;

                            default:
                                throw new AssertionError();
                        }
                    }
                }
            }
        });
    }
}