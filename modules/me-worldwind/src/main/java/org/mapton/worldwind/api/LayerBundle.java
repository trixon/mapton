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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.layers.Layer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class LayerBundle {

    public static final int DEFAULT_REPAINT_DELAY = 5000;
    private static Set<Runnable> sActivePaintersSet = Collections.synchronizedSet(new HashSet());
    private HashSet<Layer> mChildLayers = new HashSet<>();
    private boolean mInitialized = false;
    private final ObservableList<Layer> mLayers = FXCollections.observableArrayList();
    private final StringProperty mName = new SimpleStringProperty();
    private Runnable mPainter;
    private Layer mParentLayer;
    private boolean mPopulated = false;

    public LayerBundle() {
    }

    public void addAllChildLayers(Layer... childLayers) {
        mChildLayers.addAll(Arrays.asList(childLayers));
        for (var childLayer : childLayers) {
            setVisibleInLayerManager(childLayer, false);
            childLayer.setEnabled(mParentLayer.isEnabled());
        }

        mParentLayer.addPropertyChangeListener(pce -> {
            if (pce.getPropertyName().equals("Enabled")) {
                for (var childLayer : mChildLayers) {
                    childLayer.setEnabled(mParentLayer.isEnabled());
                }
            }
        });
    }

    public void attachTopComponentToLayer(String topComponentID, Layer layer) {
        layer.setValue(WWHelper.KEY_FAST_OPEN, topComponentID);
    }

    /**
     *
     * @param parentLayer
     * @param childLayers
     * @deprecated Use setAllChildLayers or addAllChildLayers instead
     */
    @Deprecated
    public void connectChildLayers(Layer parentLayer, Layer... childLayers) {
        mParentLayer = parentLayer;
        setAllChildLayers(childLayers);
    }

    public ObservableList<Layer> getLayers() {
        return mLayers;
    }

    public final String getName() {
        return mName.get();
    }

    public Node getOptionsView() {
        return null;
    }

    public Layer getParentLayer() {
        return mParentLayer;
    }

    public boolean isPopulated() {
        return mPopulated;
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public abstract void populate() throws Exception;

    /**
     * Runs the repaintRunnable in its own thread after a delay.
     *
     * @param delay -1=Run without delay, >=0 initialize and run
     */
    public synchronized void repaint(long delay) {
        if (mPainter == null || sActivePaintersSet.contains(mPainter)) {
            return;
        }

        if (delay != -1) {
            mInitialized = true;
        }

        if (mInitialized) {
            sActivePaintersSet.add(mPainter);
            new Thread(() -> {
                if (delay > -1) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        //nvm
                    }
                }
                mPainter.run();
                LayerBundleManager.getInstance().redraw();
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
                sActivePaintersSet.remove(mPainter);
            }, getClass().getName() + "->repaint").start();
        }
    }

    /**
     * Repaint now, if initialized
     */
    public void repaint() {
        repaint(-1);
    }

    public void setAllChildLayers(Layer... childLayers) {
        mChildLayers = new HashSet<>(Arrays.asList(childLayers));
        addAllChildLayers(childLayers);
    }

    public void setCategory(Layer layer, String category) {
        layer.setValue(WWHelper.KEY_LAYER_CATEGORY, category);
    }

    public void setCategoryAddOns(Layer layer) {
        setCategory(layer, String.format("- %s -", Dict.ADD_ONS.toString()));
    }

    public void setCategorySystem(Layer layer) {
        setCategory(layer, String.format("- %s -", Dict.SYSTEM.toString()));
    }

    public final void setName(String value) {
        mName.set(value);
    }

    public void setPainter(Runnable painter) {
        mPainter = painter;
    }

    public void setParentLayer(Layer parentLayer) {
        mParentLayer = parentLayer;
    }

    public void setPopulated(boolean populated) {
        mPopulated = populated;
    }

    public void setVisibleInLayerManager(Layer layer, boolean visibility) {
        layer.setValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER, !visibility);
    }
}
