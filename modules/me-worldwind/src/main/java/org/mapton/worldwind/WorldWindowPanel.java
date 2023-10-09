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

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.GeographicProjection;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.globes.projections.ProjectionEquirectangular;
import gov.nasa.worldwind.globes.projections.ProjectionMercator;
import gov.nasa.worldwind.globes.projections.ProjectionModifiedSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionPolarEquidistant;
import gov.nasa.worldwind.globes.projections.ProjectionSinusoidal;
import gov.nasa.worldwind.globes.projections.ProjectionTransverseMercator;
import gov.nasa.worldwind.globes.projections.ProjectionUPS;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.LocalElevationModel;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import static org.mapton.api.MKey.*;
import org.mapton.api.MOptions;
import org.mapton.api.MWmsSource;
import org.mapton.api.Mapton;
import org.mapton.core.api.MaptonNb;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.MapStyle;
import org.mapton.worldwind.api.WWHelper;
import org.mapton.worldwind.api.WmsLayerLoader;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.swing.FileHelper;

/**
 *
 * @author Patrik Karlström
 */
public class WorldWindowPanel extends WorldWindowGLJPanel {

    private CrosshairLayer mCrosshairLayer;
    private final ObservableList<Layer> mCustomLayers = FXCollections.observableArrayList();
    private FlatGlobe mFlatGlobe;
    private IndicatorLayerBundle mIndicatorLayer;
    private Object mLastHighlightObject;
    private String mLastHighlightText;
    private CompoundElevationModel mNormalElevationModel;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private Globe mRoundGlobe;
    private final WmsLayerLoader mWmsLayerLoader = new WmsLayerLoader();
    private final HashSet<String> mWmsLoadedLayers = new HashSet<>();
    private final ElevationModel mZeroElevationModel = new ZeroElevationModel();

    public WorldWindowPanel(Runnable postCreateRunnable) {
        MaptonNb.progressStart(MDict.MAP_ENGINE.toString());
        init();

        new Thread(() -> {
            initFinalize();
            initListeners();
            postCreateRunnable.run();
        }, getClass().getCanonicalName()).start();
    }

    public void addCustomLayer(Layer layer) {
        if (!getLayers().contains(layer)) {
            Mapton.logLoading("Custom Layer", layer.getName());
            mCustomLayers.add(layer);
            insertLayerBefore(layer, CompassLayer.class);

            moveSurfaceImageLayersToTop();
        }
    }

    public WorldWindow getWwd() {
        return wwd;
    }

    public void removeCustomLayer(Layer layer) {
        if (getLayers().contains(layer)) {
            Mapton.logLoading("Custom Layer", layer.getName());
            mCustomLayers.remove(layer);
            getLayers().remove(layer);
        }
    }

    ObservableList<Layer> getCustomLayers() {
        return mCustomLayers;
    }

    Callable<BufferedImage> getImageRenderer() {
        return () -> {
            //TODO Save unwanted layer state and hide them
            var image = GraphicsHelper.componentToImage(this, null);
            //TODO Restore

            return image;
        };
    }

    LayerList getLayers() {
        return getModel().getLayers();
    }

    private void customElevationModelLoad(File dir) {
        System.out.println("customElevationModelLoad " + dir.getAbsolutePath());
        var localElevationModel = new LocalElevationModel();

        for (var file : dir.listFiles()) {
            if (!file.isFile()) {
                continue;
            }

            try {
                System.out.println("load elevation model " + file.getAbsolutePath());
//                CompoundElevationModel cem = (CompoundElevationModel) getWwd().getModel().getGlobe().getElevationModel();
                localElevationModel.addElevations(file);
//                cem.addElevationModel(em);
                mNormalElevationModel.addElevationModel(localElevationModel);
            } catch (BufferUnderflowException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void customElevationModelRefresh() {
        var thread = new Thread(() -> {
            var dir = new File(FileHelper.getDefaultDirectory(), "test/dem");
            if (dir.isDirectory()) {
                customElevationModelLoad(dir);
            }
        }, getClass().getCanonicalName());

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private GeographicProjection getProjection() {
        switch (mOptions.getInt(KEY_MAP_PROJECTION)) {
            case 1:
                return new ProjectionMercator();
            case 2:
                return new ProjectionPolarEquidistant(AVKey.NORTH);
            case 3:
                return new ProjectionPolarEquidistant(AVKey.SOUTH);
            case 4:
                return new ProjectionSinusoidal();
            case 5:
                return new ProjectionModifiedSinusoidal();
            case 6:
                return new ProjectionTransverseMercator(getView().getCurrentEyePosition().getLongitude());
            case 7:
                return new ProjectionUPS(AVKey.NORTH);
            case 8:
                return new ProjectionUPS(AVKey.SOUTH);
            default:
                return new ProjectionEquirectangular();
        }
    }

    private void init() {
        var model = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        setModel(model);

        mRoundGlobe = model.getGlobe();
        mFlatGlobe = new EarthFlat();
        mFlatGlobe.setElevationModel(new ZeroElevationModel());

        var viewControlsLayer = new ViewControlsLayer();
        insertLayerBefore(viewControlsLayer, CompassLayer.class);
        addSelectListener(new ViewControlsSelectListener(this, viewControlsLayer));

        var maskLayer = new MaskLayer();
        insertLayerBefore(maskLayer, WorldMapLayer.class);

        mCrosshairLayer = new CrosshairLayer();
        mCrosshairLayer.setIconFilePath("org/mapton/worldwind/crosshair.png");
        mCrosshairLayer.setIconScale(0.16);
        mCrosshairLayer.setEnabled(MOptions.getInstance().isDisplayCrosshair());
        insertLayerBefore(mCrosshairLayer, WorldMapLayer.class);

        mNormalElevationModel = (CompoundElevationModel) wwd.getModel().getGlobe().getElevationModel();
        wwd.getModel().getGlobe().setElevationModel(mZeroElevationModel);
        wwd.getSceneController().setDeepPickEnabled(true);

        mIndicatorLayer = new IndicatorLayerBundle();
        mIndicatorLayer.populate();
    }

    private void initFinalize() {
        updateScreenLayers();
        updateMode();
        updateProjection();
        updateElevation();
        MaptonNb.progressStop(MDict.MAP_ENGINE.toString());
        Mapton.getExecutionFlow().setReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED);
        updateStyle();

        initLayerBundles();

        customElevationModelRefresh();
    }

    private void initLayerBundles() {
        for (var layerBundle : Lookup.getDefault().lookupAll(LayerBundle.class)) {
            if (!layerBundle.isPopulated()) {
                try {
                    layerBundle.populate();
                    layerBundle.setPopulated(true);
                    layerBundle.getLayers().forEach((layer) -> {
                        layer.setValue("layerBundle", layerBundle);
                        addCustomLayer(layer);
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

                layerBundle.getLayers().addListener((ListChangeListener.Change<? extends Layer> c) -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            c.getAddedSubList().forEach((layer) -> {
                                addCustomLayer(layer);
                            });
                        }
                        if (c.wasRemoved()) {
                            c.getRemoved().forEach((layer) -> {
                                removeCustomLayer(layer);
                            });
                        }
                    }
                });
            }
        }
        addCustomLayer(mIndicatorLayer.getLayers().get(0));
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            switch (pce.getKey()) {
                case KEY_MAP_OPACITY, KEY_MAP_STYLE ->
                    updateStyle();
                case KEY_MAP_ELEVATION ->
                    updateElevation();
                case KEY_MAP_GLOBE ->
                    updateMode();
                case KEY_MAP_PROJECTION ->
                    updateProjection();
                case KEY_DISPLAY_ATMOSPHERE, KEY_DISPLAY_COMPASS, KEY_DISPLAY_CONTROLS, KEY_DISPLAY_PLACE_NAMES, KEY_DISPLAY_SCALE_BAR, KEY_DISPLAY_STARS, KEY_DISPLAY_WORLD_MAP ->
                    updateScreenLayers();
                default -> {
                }
            }
        });

        Lookup.getDefault().lookupResult(LayerBundle.class).addLookupListener(lookupEvent -> {
            initLayerBundles();
        });

        var highlightClickAdapter = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mLastHighlightObject != null) {
                    if (mLastHighlightObject instanceof AVList avList) {
                        if (mouseEvent.getClickCount() == 1) {
                            Runnable r = (Runnable) avList.getValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK);
                            if (r != null) {
                                r.run();
                            }
                        } else if (mouseEvent.getClickCount() == 2) {
                            Runnable r = (Runnable) avList.getValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK);
                            if (r != null) {
                                r.run();
                            }
                        }
                    }
                }
            }
        };

        var rolloverSelectListener = new SelectListener() {

            @Override
            public void selected(SelectEvent event) {
                if (event.isRollover()) {
                    highlight(event.getTopObject());
                }
            }

            private void highlight(Object o) {
                if (mLastHighlightObject == o) {
                    return;
                }

                if (mLastHighlightObject != null) {
                    if (mLastHighlightObject instanceof PointPlacemark pointPlacemark) {
                        pointPlacemark.setAlwaysOnTop(false);
                        pointPlacemark.setHighlighted(false);
                        if (pointPlacemark.hasKey(WWHelper.KEY_HOOVER_TEXT)) {
                            pointPlacemark.setLabelText(mLastHighlightText);
                        }
                    } else if (mLastHighlightObject instanceof WWIcon wwIcon) {
                        wwIcon.setAlwaysOnTop(false);
                        wwIcon.setHighlighted(false);
                    } else if (mLastHighlightObject instanceof Highlightable highlightable) {
                        highlightable.setHighlighted(false);
                    }

                    if (mLastHighlightObject instanceof AVList avList) {
                        Runnable r = (Runnable) avList.getValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF);
                        if (r != null) {
                            r.run();
                        }
                    }

                    mLastHighlightObject = null;
                }

                mLastHighlightObject = o;
                if (mLastHighlightObject instanceof PointPlacemark pointPlacemark) {
                    pointPlacemark.setAlwaysOnTop(true);
                    pointPlacemark.setHighlighted(true);
                    if (pointPlacemark.hasKey(WWHelper.KEY_HOOVER_TEXT)) {
                        mLastHighlightText = pointPlacemark.getLabelText();
                        pointPlacemark.setLabelText(pointPlacemark.getStringValue(WWHelper.KEY_HOOVER_TEXT));
                    }
                } else if (mLastHighlightObject instanceof WWIcon wwIcon) {
                    wwIcon.setAlwaysOnTop(true);
                    wwIcon.setHighlighted(true);
                } else if (mLastHighlightObject instanceof Highlightable highlightable) {
                    highlightable.setHighlighted(true);
                }

                if (mLastHighlightObject instanceof AVList avList) {
                    Runnable r = (Runnable) avList.getValue(WWHelper.KEY_RUNNABLE_HOOVER_ON);
                    if (r != null) {
                        r.run();
                    }
                }
            }
        };

        getWwd().addSelectListener(rolloverSelectListener);
        getWwd().getInputHandler().addMouseListener(highlightClickAdapter);

        MOptions.getInstance().displayCrosshairProperty().addListener((p, o, n) -> {
            mCrosshairLayer.setEnabled(n);
        });
    }

    private void insertLayerBefore(Layer aLayer, Class type) {
        int position = 0;
        var layers = getLayers();
        for (var layer : getLayers()) {
            if (type.isInstance(layer)) {
                position = layers.indexOf(layer);
                break;
            }
        }

        layers.add(position, aLayer);
    }

    private boolean isFlatGlobe() {
        return getModel().getGlobe() instanceof FlatGlobe;
    }

    private void moveSurfaceImageLayersToTop() {
        for (var layer : getLayers()) {
            if (layer instanceof SurfaceImageLayer) {
                getLayers().remove(layer);
                getLayers().add(layer);
            }
        }
    }

    private synchronized void orderLayers(String[] styleLayers) {
        if (styleLayers == null) {
            return;
        }

        var allLayers = getLayers();
        var documentLayers = Arrays.asList(styleLayers);
        Collections.reverse(documentLayers);

        for (var layerName : documentLayers) {
            var layer = allLayers.getLayerByName(layerName);
            if (layer != null) {
                allLayers.remove(layer);
                allLayers.add(layer);
            } else {
                Mapton.getLog().e(Dict.DOCUMENT.toString(), "Layer not found: " + layerName);
            }
        }

        moveSurfaceImageLayersToTop();
    }

    private void updateElevation() {
        wwd.getModel().getGlobe().setElevationModel(mOptions.is(KEY_MAP_ELEVATION, DEFAULT_MAP_ELEVATION) ? mNormalElevationModel : mZeroElevationModel);
        wwd.redraw();
    }

    private void updateMode() {
        boolean flat = !mOptions.is(KEY_MAP_GLOBE);
        if (isFlatGlobe() == flat) {
            //return;
        }

        if (flat) {
            getModel().setGlobe(mFlatGlobe);
            getView().stopMovement();
        } else {
            getModel().setGlobe(mRoundGlobe);
            getView().stopMovement();
            updateProjection();
        }

        redraw();
    }

    private void updateProjection() {
        if (!isFlatGlobe()) {
            return;
        }

        mFlatGlobe.setProjection(this.getProjection());
        redraw();
    }

    private void updateScreenLayers() {
        getLayers().getLayerByName("Compass").setEnabled(mOptions.is(KEY_DISPLAY_COMPASS, DEFAULT_DISPLAY_COMPASS));
        getLayers().getLayerByName("World Map").setEnabled(mOptions.is(KEY_DISPLAY_WORLD_MAP, DEFAULT_DISPLAY_WORLD_MAP));
        getLayers().getLayerByName("Scale bar").setEnabled(mOptions.is(KEY_DISPLAY_SCALE_BAR, DEFAULT_DISPLAY_SCALE_BAR));
        getLayers().getLayerByName("View Controls").setEnabled(mOptions.is(KEY_DISPLAY_CONTROLS, DEFAULT_DISPLAY_CONTROLS));
        getLayers().getLayerByName("Atmosphere").setEnabled(mOptions.is(KEY_DISPLAY_ATMOSPHERE, DEFAULT_DISPLAY_ATMOSPHERE));
        getLayers().getLayerByName("Stars").setEnabled(mOptions.is(KEY_DISPLAY_STARS, DEFAULT_DISPLAY_STARS));
        getLayers().getLayerByName("Place Names").setEnabled(mOptions.is(KEY_DISPLAY_PLACE_NAMES, DEFAULT_DISPLAY_PLACE_NAMES));

        redraw();
    }

    private synchronized void updateStyle() {
        var blocklist = Set.of(
                "Compass",
                "World Map",
                "Scale bar",
                "View Controls",
                "Stars",
                "Atmosphere",
                "Place Names",
                "Measure Tool",
                "Mask",
                "Crosshairs"
        );

        var styleId = mOptions.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE);
        var mapStyle = MapStyle.getStyle(styleId);

        try {
            Mapton.getLog().i(Dict.DOCUMENT.toString(), "%s: (%s)".formatted(mapStyle.getName(), String.join(", ", mapStyle.getLayers())));
        } catch (NullPointerException e) {
        }

        ((Stream<String>) Arrays.stream(mapStyle.getLayers()))
                .filter(id -> !mWmsLoadedLayers.contains(id))
                .forEachOrdered(id -> {
                    ArrayList<MWmsSource> wmsSources = Mapton.getGlobalState().get(DATA_SOURCES_WMS_SOURCES);

                    if (wmsSources != null) {
                        Layer layer = null;

                        for (var wmsSource : wmsSources) {
                            for (var entry : wmsSource.getLayers().entrySet()) {
                                var key = entry.getKey();
                                var val = entry.getValue();

                                if (val.equalsIgnoreCase(id)) {
                                    layer = mWmsLayerLoader.load(id, wmsSource.getUrl(), key);
                                    break;
                                }
                            }
                        }

                        if (layer != null) {
                            layer.setOpacity(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
                            getLayers().addIfAbsent(layer);
                            mWmsLoadedLayers.add(id);
                        }
                    }
                });

        getLayers().forEach(layer -> {
            try {
                var name = layer.getName();
                if (!blocklist.contains(name) && !mCustomLayers.contains(layer)) {
                    layer.setEnabled(Arrays.asList(mapStyle.getLayers()).contains(name));
                }
            } catch (NullPointerException e) {
                //nvm
            }
        });

        orderLayers(mapStyle.getLayers());

        redraw();
    }
}
