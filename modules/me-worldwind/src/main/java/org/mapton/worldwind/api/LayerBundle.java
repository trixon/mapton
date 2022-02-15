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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.drag.Draggable;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.mapton.api.MKey;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class LayerBundle {

    public static final int DEFAULT_REPAINT_DELAY = 5000;
    private static final Set<Runnable> sActivePaintersSet = Collections.synchronizedSet(new HashSet());
    private HashSet<Layer> mChildLayers = new HashSet<>();
    private boolean mInitialized = false;
    private final ObservableList<Layer> mLayers = FXCollections.observableArrayList();
    private final StringProperty mName = new SimpleStringProperty();
    private final HashMap<Object, ArrayList<Renderable>> mObjectToRenderables = new HashMap<>();
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
                    childLayer.setEnabled(mParentLayer.isEnabled() && getChildVisibility(childLayer));
                }
            }
        });
    }

    public void attachTopComponentToLayer(String topComponentID, Layer layer) {
        layer.setValue(WWHelper.KEY_FAST_OPEN, topComponentID);
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

    public ArrayList<Renderable> getRenderablesForObject(Object o) {
        return mObjectToRenderables.computeIfAbsent(o, k -> {
            return new ArrayList<>();
        });
    }

    public boolean isPopulated() {
        return mPopulated;
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public void onSelectEvent(Object o, SelectEvent selectEvent) {
    }

    public abstract void populate() throws Exception;

    public void removeAllIcons(IconLayer... layers) {
        for (var layer : layers) {
            layer.removeAllIcons();
        }
    }

    public void removeAllIcons() {
        if (mParentLayer instanceof IconLayer) {
            ((IconLayer) mParentLayer).removeAllIcons();
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof IconLayer))
                .forEachOrdered(layer -> {
                    ((IconLayer) layer).removeAllIcons();
                });

        mLayers.stream()
                .filter(layer -> (layer instanceof IconLayer))
                .forEachOrdered(layer -> {
                    ((IconLayer) layer).removeAllIcons();
                });
    }

    public void removeAllRenderables() {
        if (mParentLayer instanceof RenderableLayer) {
            ((RenderableLayer) mParentLayer).removeAllRenderables();
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .forEachOrdered(layer -> {
                    ((RenderableLayer) layer).removeAllRenderables();
                });

        mLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .forEachOrdered(layer -> {
                    ((RenderableLayer) layer).removeAllRenderables();
                });
    }

    public void removeAllRenderables(RenderableLayer... layers) {
        for (var layer : layers) {
            layer.removeAllRenderables();
        }
    }

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
                        Thread.currentThread().interrupt();
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

    public void setDragEnabled(boolean enabled, RenderableLayer... layers) {
        for (var layer : layers) {
            for (var renderable : layer.getRenderables()) {
                if (renderable instanceof Draggable) {
                    if (renderable instanceof AbstractAirspace) {
                        setDragEnabled((AbstractAirspace) renderable, enabled);
                    } else {
                        ((Draggable) renderable).setDragEnabled(enabled);
                    }
                }
            }
        }
    }

    public void setDragEnabled(boolean enabled) {
        if (mParentLayer instanceof RenderableLayer) {
            setDragEnabled(enabled, (RenderableLayer) mParentLayer);
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .forEachOrdered(layer -> {
                    setDragEnabled(enabled, (RenderableLayer) layer);
                });

        mLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .forEachOrdered(layer -> {
                    setDragEnabled(enabled, (RenderableLayer) layer);
                });
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

    public void setSelectEventWatcher(AVListImpl avListImpl, Object object) {
        avListImpl.setValue(MKey.WW_DRAG_LAYER_BUNDLE, this);
        avListImpl.setValue(MKey.WW_DRAG_OBJECT, object);
    }

    public void setVisibleInLayerManager(Layer layer, boolean visibility) {
        layer.setValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER, !visibility);
    }

    private boolean getChildVisibility(Layer layer) {
        boolean visible = true;
        var visibility = layer.getValue(MKey.LAYER_SUB_VISIBILITY);
        if (visibility != null) {
            visible = (boolean) visibility;
        }

        return visible;
    }

    private void setDragEnabled(AbstractAirspace abstractAirspace, boolean enabled) {
        //TODO Remove this method once resolved: https://github.com/NASAWorldWind/WorldWindJava/issues/240
        try {
            var dragEnabledField = AbstractAirspace.class.getDeclaredField("dragEnabled");
            boolean accessible = dragEnabledField.canAccess(abstractAirspace);
            dragEnabledField.setAccessible(true);
            dragEnabledField.set(abstractAirspace, enabled);
            dragEnabledField.setAccessible(accessible);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
