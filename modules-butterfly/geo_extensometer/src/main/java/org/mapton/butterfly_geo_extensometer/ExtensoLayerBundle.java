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
package org.mapton.butterfly_geo_extensometer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.butterfly_geo.api.GeotechnicalHelper;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class ExtensoLayerBundle extends BfLayerBundle {

    private final ExtensoAttributeManager mAttributeManager = ExtensoAttributeManager.getInstance();
    private final GraphicRenderer mGraphicRenderer;
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final ExtensoManager mManager = ExtensoManager.getInstance();
    private final ExtensoOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();

    public ExtensoLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new ExtensoOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mOptionsView.getComponentCheckModel());
        initListeners();

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mLabelLayer, mPinLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_ExtensometerAction());
        setCategory(mLayer, GeotechnicalHelper.CAT_GEO);
        setName(Bundle.CTL_ExtensometerAction());
        attachTopComponentToLayer("ExtensoTopComponent", mLayer);
        mLabelLayer.setEnabled(true);
        mLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(10000);
        mLabelLayer.setMaxActiveAltitude(10000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mPinLayer);
        mLayer.setPickEnabled(true);

        mLayer.setEnabled(false);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BGeoExtensometer> c) -> {
            repaint();
        });

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = mLayer.isEnabled();
            mManager.updateTemporal(enabled);

            if (enabled) {
                repaint();
            }
        });

        mOptionsView.labelByProperty().addListener((p, o, n) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            if (!mLayer.isEnabled()) {
                return;
            }

            var pointBy = mOptionsView.getPointBy();
            switch (pointBy) {
                case NONE -> {
                    mPinLayer.setEnabled(false);
                }
                case PIN -> {
                    mPinLayer.setEnabled(true);
                }
                default ->
                    throw new AssertionError();
            }

            for (var extenso : new ArrayList<>(mManager.getTimeFilteredItems())) {
                var mapObjects = new ArrayList<AVListImpl>();

                if (ObjectUtils.allNotNull(extenso.getLat(), extenso.getLon())) {
                    var position = Position.fromDegrees(extenso.getLat(), extenso.getLon());
                    var labelPlacemark = plotLabel(extenso, mOptionsView.getLabelBy(), position);

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(extenso, position, labelPlacemark));
                    mGraphicRenderer.plot(extenso, position, mapObjects);
                }

                var leftClickRunnable = (Runnable) () -> {
                    mManager.setSelectedItemAfterReset(extenso);
                };

                var leftDoubleClickRunnable = (Runnable) () -> {
                    Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                };

                mapObjects.stream().filter(r -> r != null).forEach(r -> {
                    r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                    r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                });
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BGeoExtensometer extenso, ExtensoLabelBy labelBy, Position position) {
        if (labelBy == ExtensoLabelBy.NONE) {
            return null;
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(labelBy.getLabel(extenso));
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    private PointPlacemark plotPin(BGeoExtensometer extenso, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(0);
        var placemark = new PointPlacemark(position);

        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

        mPinLayer.addRenderable(placemark);
        if (labelPlacemark != null) {
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return placemark;
    }
}
