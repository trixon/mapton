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
package org.mapton.butterfly_tmo.rorelse;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import org.mapton.butterfly_tmo.api.RorelseManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class RorelseLayerBundle extends BfLayerBundle {

    private final RorelseAttributeManager mAttributeManager = RorelseAttributeManager.getInstance();
    private final ComponentRenderer mComponentRenderer;
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final RorelseManager mManager = RorelseManager.getInstance();
    private final RorelseOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();
    private final RenderableLayer mGroundConnectorLayer = new RenderableLayer();
    private final RenderableLayer mSymbolLayer = new RenderableLayer();
    private final RenderableLayer mSurfaceLayer = new RenderableLayer();

    public RorelseLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new RorelseOptionsView(this);
        mComponentRenderer = new ComponentRenderer(mLayer, mGroundConnectorLayer, mSurfaceLayer);
        initListeners();

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mLabelLayer, mSymbolLayer, mPinLayer, mGroundConnectorLayer, mSurfaceLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_RorelseAction());
        setCategory(mLayer, "TMO");
        setName(Bundle.CTL_RorelseAction());
        attachTopComponentToLayer("RorelseTopComponent", mLayer);
//        mLayer.setMaxActiveAltitude(6000);
//        mSurfaceLayer.setMaxActiveAltitude(6000);
//        mPinLayer.setMaxActiveAltitude(300);
//        mLabelLayer.setMaxActiveAltitude(200);
//        mGroundConnectorLayer.setMaxActiveAltitude(1000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mSymbolLayer, mPinLayer, mGroundConnectorLayer, mSurfaceLayer);

        mLayer.setPickEnabled(true);
        mSurfaceLayer.setPickEnabled(false);

        mLayer.setEnabled(false);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BRorelse> c) -> {
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
            mComponentRenderer.reset();
            if (!mLayer.isEnabled()) {
                return;
            }
            var pointBy = mOptionsView.getPointBy();
//            switch (pointBy) {
//                case NONE -> {
//                    mPinLayer.setEnabled(false);
//                    mSymbolLayer.setEnabled(false);
//                }
//                case PIN -> {
//                    mSymbolLayer.setEnabled(false);
//                    mPinLayer.setEnabled(true);
//                }
//                default ->
//                    throw new AssertionError();
//            }

            for (var p : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                    var position = Position.fromDegrees(p.getLat(), p.getLon());

                    var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                    var mapObjects = new ArrayList<AVListImpl>();

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(p, position, labelPlacemark));

//                    mComponentRenderer.plot(p, position, mapObjects);
                    var leftClickRunnable = (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(p);
                    };

                    var leftDoubleClickRunnable = (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    };

                    mapObjects.stream().filter(r -> r != null).forEach(r -> {
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                    });
                }
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BRorelse p, RorelseLabelBy labelBy, Position position) {
        if (labelBy == RorelseLabelBy.NONE) {
            return null;
        }

        String label;
        try {
//            label = mOptionsView.getLabelBy().getLabel(p);
        } catch (Exception e) {
            label = "ERROR %s <<<<<<<<".formatted(p.getName());
        }
        label = p.getBenämning();
        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(label);
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    private PointPlacemark plotPin(BRorelse r, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(Color.ORANGE);
        if (r.ext().getObservationsAllRaw() != null && r.ext().getObservationsAllRaw().isEmpty()) {
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageColor(Color.CYAN);
        }

        if (!StringUtils.equalsIgnoreCase(r.getStatus(), "Aktiv")) {
            attrs = new PointPlacemarkAttributes(attrs);
            //attrs.setImageColor(Color.RED);
        }
        attrs.setScale(Mapton.SCALE_PIN_IMAGE);
        attrs.setLabelScale(Mapton.SCALE_PIN_LABEL);

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
