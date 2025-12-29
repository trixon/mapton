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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.mapton.api.MDict;
import org.mapton.worldwind.api.AnnotationManager;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class AnnotationsLayerBundle extends LayerBundle {

    private final AnnotationLayer mAnnotationLayer = new AnnotationLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final AnnotationManager mManager = AnnotationManager.getInstance();
    private final AnnotationsOptions mOptions = AnnotationsOptions.getInstance();
    private AnnotationsOptionsView mOptionsView;

    public AnnotationsLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = new AnnotationsOptionsView();
        }

        return mOptionsView.getBorderPane();
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mAnnotationLayer);
        repaint(0);
    }

    private void init() {
        mLayer.setName(MDict.ANNOTATIONS.toString());
        setName(MDict.ANNOTATIONS.toString());
        setCategorySystem(mLayer);
        mLayer.setPickEnabled(false);
        setParentLayer(mLayer);
        addAllChildLayers(mAnnotationLayer);
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            resetPaintDelayedResetRunner();
        });

        mManager.getAllItems().addListener((ListChangeListener.Change<? extends GlobeAnnotation> c) -> {
            resetPaintDelayedResetRunner();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            synchronized (mManager.getAllItems()) {
                for (var annotation : mManager.getAllItems()) {
                    mAnnotationLayer.addAnnotation(annotation);
                }
            }

            setDragEnabled(false);
        });
    }
}
