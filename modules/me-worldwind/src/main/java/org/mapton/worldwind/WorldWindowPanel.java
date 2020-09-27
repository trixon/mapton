/*
 * Copyright 2019 Patrik Karlström.
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
import gov.nasa.worldwind.exception.WWRuntimeException;
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
import java.net.SocketTimeoutException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import static org.mapton.api.MKey.*;
import org.mapton.api.MNotificationIcons;
import org.mapton.api.MOptions;
import org.mapton.api.MWmsSource;
import org.mapton.api.Mapton;
import org.mapton.core.api.MaptonNb;
import static org.mapton.worldwind.ModuleOptions.*;
import static org.mapton.worldwind.WorldWindMapEngine.LOG_TAG;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.MapStyle;
import org.mapton.worldwind.api.WWHelper;
import org.mapton.worldwind.api.WmsService;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Priority;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GraphicsHelper;

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
    private ElevationModel mZeroElevationModel = new ZeroElevationModel();

    public WorldWindowPanel() {
        MaptonNb.progressStart(MDict.MAP_ENGINE.toString());
        init();

        new Thread(() -> {
            initFinalize();
            initListeners();

            MaptonNb.progressStop(MDict.MAP_ENGINE.toString());
        }).start();
    }

    public void addCustomLayer(Layer layer) {
        if (!getLayers().contains(layer)) {
            Mapton.logLoading("Custom Layer", layer.getName());
            mCustomLayers.add(layer);
            insertLayerBefore(layer, CompassLayer.class);
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
            BufferedImage image = GraphicsHelper.componentToImage(this, null);
            //TODO Restore

            return image;
        };
    }

    LayerList getLayers() {
        return getModel().getLayers();
    }

    private void customElevationModelLoad(File dir) {
        System.out.println("customElevationModelLoad " + dir.getAbsolutePath());
        LocalElevationModel localElevationModel = new LocalElevationModel();

        for (File file : dir.listFiles()) {
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
        Thread thread = new Thread(() -> {
            File dir = new File(FileUtils.getUserDirectory(), "test/dem");
            if (dir.isDirectory()) {
                customElevationModelLoad(dir);
            }
        });

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
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        setModel(m);

        mRoundGlobe = m.getGlobe();
        mFlatGlobe = new EarthFlat();
        mFlatGlobe.setElevationModel(new ZeroElevationModel());

        ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
        insertLayerBefore(viewControlsLayer, CompassLayer.class);
        addSelectListener(new ViewControlsSelectListener(this, viewControlsLayer));

        MaskLayer maskLayer = new MaskLayer();
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
        Mapton.getExecutionFlow().setReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED);
        initLayerBundles();
        initWmsService();

        updateStyle();
        customElevationModelRefresh();
    }

    private void initLayerBundles() {
//        SwingUtilities.invokeLater(() -> {
        for (LayerBundle layerBundle : Lookup.getDefault().lookupAll(LayerBundle.class)) {
            if (!layerBundle.isPopulated()) {
                try {
                    layerBundle.populate();
                    layerBundle.setPopulated(true);
                    layerBundle.getLayers().forEach((layer) -> {
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
//        });
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            switch (pce.getKey()) {
                case ModuleOptions.KEY_MAP_OPACITY:
                case ModuleOptions.KEY_MAP_STYLE:
                    updateStyle();
                    break;
                case ModuleOptions.KEY_MAP_ELEVATION:
                    updateElevation();
                    break;
                case ModuleOptions.KEY_MAP_GLOBE:
                    updateMode();
                    break;
                case ModuleOptions.KEY_MAP_PROJECTION:
                    updateProjection();
                    break;
                case ModuleOptions.KEY_DISPLAY_ATMOSPHERE:
                case ModuleOptions.KEY_DISPLAY_COMPASS:
                case ModuleOptions.KEY_DISPLAY_CONTROLS:
                case ModuleOptions.KEY_DISPLAY_PLACE_NAMES:
                case ModuleOptions.KEY_DISPLAY_SCALE_BAR:
                case ModuleOptions.KEY_DISPLAY_STARS:
                case ModuleOptions.KEY_DISPLAY_WORLD_MAP:
                    updateScreenLayers();
                    break;

                default:
                    break;
            }
        });

        Mapton.getGlobalState().addListener(gsce -> {
            initWmsService();
        }, DATA_SOURCES_WMS_SOURCES);

        Lookup.getDefault().lookupResult(LayerBundle.class).addLookupListener(lookupEvent -> {
            initLayerBundles();
        });

        Lookup.getDefault().lookupResult(WmsService.class).addLookupListener(lookupEvent -> {
            initWmsService();
        });

        MouseAdapter highlightClickAdapter = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mLastHighlightObject != null) {
                    if (mLastHighlightObject instanceof AVList) {
                        AVList avList = (AVList) mLastHighlightObject;

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

        SelectListener rolloverSelectListener = new SelectListener() {

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
                    if (mLastHighlightObject instanceof PointPlacemark) {
                        PointPlacemark pointPlacemark = (PointPlacemark) mLastHighlightObject;
                        pointPlacemark.setAlwaysOnTop(false);
                        pointPlacemark.setHighlighted(false);
                        if (pointPlacemark.hasKey(WWHelper.KEY_HOOVER_TEXT)) {
                            pointPlacemark.setLabelText(mLastHighlightText);
                        }
                    } else if (mLastHighlightObject instanceof WWIcon) {
                        ((WWIcon) mLastHighlightObject).setAlwaysOnTop(false);
                        ((WWIcon) mLastHighlightObject).setHighlighted(false);
                    } else if (mLastHighlightObject instanceof Highlightable) {
                        ((Highlightable) mLastHighlightObject).setHighlighted(false);
                    }

                    mLastHighlightObject = null;
                }

                mLastHighlightObject = o;
                if (mLastHighlightObject instanceof PointPlacemark) {
                    PointPlacemark pointPlacemark = (PointPlacemark) mLastHighlightObject;
                    pointPlacemark.setAlwaysOnTop(true);
                    pointPlacemark.setHighlighted(true);
                    if (pointPlacemark.hasKey(WWHelper.KEY_HOOVER_TEXT)) {
                        mLastHighlightText = pointPlacemark.getLabelText();
                        pointPlacemark.setLabelText(pointPlacemark.getStringValue(WWHelper.KEY_HOOVER_TEXT));
                    }
                } else if (mLastHighlightObject instanceof WWIcon) {
                    ((WWIcon) mLastHighlightObject).setAlwaysOnTop(true);
                    ((WWIcon) mLastHighlightObject).setHighlighted(true);
                } else if (mLastHighlightObject instanceof Highlightable) {
                    ((Highlightable) mLastHighlightObject).setHighlighted(true);
                }

                if (mLastHighlightObject instanceof AVList) {
                    Runnable r = (Runnable) ((AVList) mLastHighlightObject).getValue(WWHelper.KEY_RUNNABLE_HOOVER);
                    if (r != null) {
                        r.run();
                    }
                }
            }
        };

        getWwd().addSelectListener(rolloverSelectListener);
        getWwd().getInputHandler().addMouseListener(highlightClickAdapter);

        MOptions.getInstance().displayCrosshairProperty().addListener((observable, oldValue, newValue) -> {
            mCrosshairLayer.setEnabled(newValue);
        });
    }

    private void initWmsService() {
        ArrayList< WmsService> wmsServices = new ArrayList<>(Lookup.getDefault().lookupAll(WmsService.class));
        ArrayList<MWmsSource> wmsSources = Mapton.getGlobalState().get(DATA_SOURCES_WMS_SOURCES);

        if (wmsSources != null) {
            for (MWmsSource wmsSource : wmsSources) {
                wmsServices.add(WmsService.createFromWmsSource(wmsSource));
            }
        }

        for (WmsService wmsService : wmsServices) {
            if (!wmsService.isPopulated()) {
//                new Thread(() -> {
                try {
                    wmsService.populate();
                    for (Layer layer : wmsService.getLayers()) {
                        Mapton.logLoading("WMS Layer", layer.getName());
                        layer.setEnabled(false);
                        getLayers().addIfAbsent(layer);
                    }
                    updateStyle();
                } catch (SocketTimeoutException ex) {
                    //aaaNbMessage.warning("ERROR", "initWmsService");//TODO Remove this once spotted
                    Mapton.getLog().w(LOG_TAG, ex.getMessage());
                } catch (XMLStreamException ex) {
                    Mapton.getLog().w(LOG_TAG, ex.getMessage());
                } catch (WWRuntimeException ex) {
                    NotificationDisplayer.getDefault().notify(
                            Dict.Dialog.TITLE_IO_ERROR.toString(),
                            MNotificationIcons.getErrorIcon(),
                            String.format("WMS error: %s", wmsService.getName()),
                            null,
                            Priority.HIGH
                    );
                    Mapton.getLog().e(LOG_TAG, ex.getMessage());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
//                }).start();
            }
        }
    }

    private void insertLayerBefore(Layer layer, Class type) {
        int position = 0;
        LayerList layers = getLayers();
        for (Layer l : getLayers()) {
            if (type.isInstance(l)) {
                position = layers.indexOf(l);
                break;
            }
        }

        layers.add(position, layer);
    }

    private boolean isFlatGlobe() {
        return getModel().getGlobe() instanceof FlatGlobe;
    }

    private synchronized void orderLayers(String[] styleLayers) {
        if (styleLayers == null) {
            return;
        }

        LayerList allLayers = getLayers();
        List<String> documentLayers = Arrays.asList(styleLayers);
        Collections.reverse(documentLayers);

        for (String layerName : documentLayers) {
            Layer layer = allLayers.getLayerByName(layerName);
            if (layer != null) {
                allLayers.remove(layer);
                allLayers.add(layer);
            } else {
                Mapton.getLog().e(Dict.DOCUMENT.toString(), "Layer not found: " + layerName);
            }
        }
    }

    private void updateElevation() {
        wwd.getModel().getGlobe().setElevationModel(mOptions.is(KEY_MAP_ELEVATION) ? mNormalElevationModel : mZeroElevationModel);
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
        HashSet<String> blacklist = new HashSet<>();
        blacklist.add("Compass");
        blacklist.add("World Map");
        blacklist.add("Scale bar");
        blacklist.add("View Controls");
        blacklist.add("Stars");
        blacklist.add("Atmosphere");
        blacklist.add("Place Names");
        blacklist.add("Measure Tool");
        blacklist.add("Mask");
        blacklist.add("Crosshairs");

        String styleId = mOptions.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE);
        String[] styleLayers = MapStyle.getLayers(styleId);
        MapStyle mapStyle = MapStyle.getStyle(styleId);

        try {
            Mapton.getLog().i(Dict.DOCUMENT.toString(), String.format("%s: (%s)", mapStyle.getName(), String.join(", ", styleLayers)));
        } catch (NullPointerException e) {
        }
        getLayers().forEach((layer) -> {
            try {
                final String name = layer.getName();
                if (!blacklist.contains(name) && !mCustomLayers.contains(layer)) {
                    layer.setEnabled(Arrays.asList(styleLayers).contains(name));
                    layer.setOpacity(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
                }
            } catch (NullPointerException e) {
                //nvm
            }
        });

        orderLayers(styleLayers);

        redraw();
    }
}
