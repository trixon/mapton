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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.drag.Draggable;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.apache.commons.lang3.Strings;
import org.mapton.api.MKey;
import org.mapton.api.MTemporalManager;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.DelayedResetRunner;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class LayerBundle {

    public static final int DEFAULT_REPAINT_DELAY = 5000;
    private static final Set<Runnable> sActivePaintersSet = Collections.synchronizedSet(new HashSet<>());
    private String mCategory;
    private HashSet<Layer> mChildLayers = new HashSet<>();
    private final AnnotationAttributes mClickAreaAttributes = new AnnotationAttributes();
    private final int mClickAreaSize = SwingHelper.getUIScaled(32);
    private boolean mInitialized = false;
    private final ObservableList<Layer> mLayers = FXCollections.observableArrayList();
    private final StringProperty mName = new SimpleStringProperty();
    private final HashMap<Object, ArrayList<Renderable>> mObjectToRenderables = new HashMap<>();
    private DelayedResetRunner mPaintDelayedResetRunner = new DelayedResetRunner(100, () -> repaint());
    private Runnable mPainter;
    private Layer mParentLayer;
    private boolean mPopulated = false;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private MTemporalRange mTemporalRange;
    private ConcurrentHashMap<String, MTemporalRange> mTemporalRanges;

    public LayerBundle() {
        mClickAreaAttributes.setLeader(AVKey.SHAPE_NONE);
        mClickAreaAttributes.setDrawOffset(new Point(0, (int) (-mClickAreaSize * .5)));
        mClickAreaAttributes.setSize(new Dimension(mClickAreaSize, mClickAreaSize));
        mClickAreaAttributes.setBorderWidth(0);
        mClickAreaAttributes.setCornerRadius(0);
        mClickAreaAttributes.setOpacity(0.001);
        mClickAreaAttributes.setBackgroundColor(Color.WHITE);
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

    public RoundAnnotation addClickArea(Position position, RenderableLayer layer, ArrayList<AVListImpl> mapObjects) {
        var annotation = new RoundAnnotation(position, mClickAreaSize, mClickAreaAttributes);
        annotation.setMaxActiveAltitude(1000);

        layer.addRenderable(annotation);
        mapObjects.add(annotation);

        return annotation;
    }

    public void attachTopComponentToLayer(String topComponentID, Layer layer) {
        layer.setValue(WWHelper.KEY_FAST_OPEN, topComponentID);
    }

    public void connectToOtherBundle(Class<? extends LayerBundle> cls, String checkBoxKey) {
        var otherLayerBundle = Lookup.getDefault().lookupAll(cls).stream().findFirst().orElse(null);
        if (otherLayerBundle != null) {
            otherLayerBundle.mParentLayer.addPropertyChangeListener("Enabled", pce -> {
                var otherLayerEnabled = otherLayerBundle.mParentLayer.isEnabled();
                var tabChecked = true;
                if (checkBoxKey != null) {
                    tabChecked = Mapton.getGlobalState().<Boolean>getOrDefault(checkBoxKey, false);
                }
                mParentLayer.setEnabled(otherLayerEnabled && tabChecked);
            });
        } else {
            System.out.println("MASTER BUNDLE NOT FOUND");
        }
    }

    public String getCategory() {
        return mCategory;
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

    public String getPath() {
        var sb = new StringBuilder(getCategory());
        if (!Strings.CI.endsWith(getCategory(), "/")) {
            sb.append("/");
        }
        sb.append(getName());

        return sb.toString();
    }

    public ArrayList<Renderable> getRenderablesForObject(Object o) {
        return mObjectToRenderables.computeIfAbsent(o, k -> {
            return new ArrayList<>();
        });
    }

    public MTemporalManager getTemporalManager() {
        return mTemporalManager;
    }

    public MTemporalRange getTemporalRange() {
        return mTemporalRange;
    }

    public ConcurrentHashMap<String, MTemporalRange> getTemporalRanges() {
        return mTemporalRanges;
    }

    public boolean isPopulated() {
        return mPopulated;
    }

    public boolean isVisible() {
        return mParentLayer != null && mParentLayer.isEnabled();
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
        if (mParentLayer instanceof IconLayer iconLayer) {
            iconLayer.removeAllIcons();
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof IconLayer))
                .map(layer -> (IconLayer) layer)
                .forEachOrdered(iconLayer -> {
                    iconLayer.removeAllIcons();
                });

        mLayers.stream()
                .filter(layer -> (layer instanceof IconLayer))
                .map(layer -> (IconLayer) layer)
                .forEachOrdered(iconLayer -> {
                    iconLayer.removeAllIcons();
                });
    }

    public void removeAllRenderables() {
        if (mParentLayer instanceof RenderableLayer renderableLayer) {
            renderableLayer.removeAllRenderables();
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .map(layer -> (RenderableLayer) layer)
                .forEachOrdered(layer -> layer.removeAllRenderables());

        mChildLayers.stream()
                .filter(layer -> (layer instanceof AnnotationLayer))
                .map(layer -> (AnnotationLayer) layer)
                .forEachOrdered(layer -> layer.removeAllAnnotations());

        mLayers.stream()
                .filter(layer -> (layer instanceof RenderableLayer))
                .map(layer -> (RenderableLayer) layer)
                .forEachOrdered(layer -> layer.removeAllRenderables());
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
            Thread.ofVirtual().name(getClass().getName() + "->repaint").start(() -> {
                mPainter.run();
                LayerBundleManager.getInstance().redraw();
                sActivePaintersSet.remove(mPainter);
            });
        }
    }

    /**
     * Repaint now, if initialized
     */
    public void repaint() {
        repaint(-1);
    }

    public void resetPaintDelayedResetRunner() {
        mPaintDelayedResetRunner.reset();
    }

    public void setAllChildLayers(Layer... childLayers) {
        mChildLayers = new HashSet<>(Arrays.asList(childLayers));
        addAllChildLayers(childLayers);
    }

    public void setCategory(Layer layer, String category) {
        mCategory = category;
        layer.setValue(WWHelper.KEY_LAYER_CATEGORY, category);
    }

    public void setCategoryAddOns(Layer layer) {
        setCategory(layer, "%s".formatted(Dict.ADD_ONS.toString()));
    }

    public void setCategorySystem(Layer layer) {
        setCategory(layer, "* %s".formatted(Dict.SYSTEM.toString()));
    }

    public void setDragEnabled(boolean enabled, AbstractLayer... abstractLayers) {
        for (var abstractLayer : abstractLayers) {
            if (abstractLayer instanceof RenderableLayer layer) {
                for (var renderable : layer.getRenderables()) {
                    if (renderable instanceof Draggable draggable) {
                        if (draggable instanceof AbstractAirspace abstractAirspace) {
                            setDragEnabled(abstractAirspace, enabled);
                        } else {
                            draggable.setDragEnabled(enabled);
                        }
                    }
                }
            } else if (abstractLayer instanceof IconLayer layer) {
                for (var icon : layer.getIcons()) {
                    if (icon instanceof Draggable draggable) {
                        draggable.setDragEnabled(enabled);
                    }
                }
            } else if (abstractLayer instanceof AnnotationLayer layer) {
                for (var annotation : layer.getAnnotations()) {
                    if (annotation instanceof Draggable draggable) {
                        draggable.setDragEnabled(enabled);
                    }
                }
            } else {
                System.err.println("Unhandled layer type in: " + getClass().getName());
                System.err.println(abstractLayer.getClass().getName());
            }
        }
    }

    public void setDragEnabled(boolean enabled) {
        if (mParentLayer instanceof AbstractLayer layer) {
            setDragEnabled(enabled, layer);
        }

        mChildLayers.stream()
                .filter(layer -> (layer instanceof AbstractLayer))
                .map(layer -> (AbstractLayer) layer)
                .forEach(layer -> setDragEnabled(enabled, layer));

        mLayers.stream()
                .filter(layer -> (layer instanceof AbstractLayer))
                .map(layer -> (AbstractLayer) layer)
                .forEach(layer -> setDragEnabled(enabled, layer));
    }

    public final void setName(String value) {
        mName.set(value);
    }

    public void setPaintDelayedResetRunner(DelayedResetRunner paintDelayedResetRunner) {
        mPaintDelayedResetRunner = paintDelayedResetRunner;
    }

    public void setPainter(Runnable painter) {
        mPainter = painter;
    }

    public void setParentLayer(Layer parentLayer) {
        mParentLayer = parentLayer;
        mParentLayer.addPropertyChangeListener("Enabled", pce -> {
            Mapton.getGlobalState().put(getClass().getCanonicalName() + "_Enabled", mParentLayer.isEnabled());
        });
        mParentLayer.setEnabled(WWHelper.isStoredAsVisible(parentLayer, false));
    }

    public void setPopulated(boolean populated) {
        mPopulated = populated;
    }

    public void setSelectEventWatcher(AVListImpl avListImpl, Object object) {
        avListImpl.setValue(MKey.WW_DRAG_LAYER_BUNDLE, this);
        avListImpl.setValue(MKey.WW_DRAG_OBJECT, object);
    }

    public void setTemporalRange(MTemporalRange temporalRange) {
        this.mTemporalRange = temporalRange;
    }

    public void setTemporalRanges(ConcurrentHashMap<String, MTemporalRange> temporalRanges) {
        this.mTemporalRanges = temporalRanges;
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
