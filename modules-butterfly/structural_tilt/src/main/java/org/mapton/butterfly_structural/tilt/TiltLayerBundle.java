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
package org.mapton.butterfly_structural.tilt;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_core.api.PinPaddle;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TiltLayerBundle extends BfLayerBundle {

    private final double SYMBOL_HEIGHT = 8.0;
    private final double SYMBOL_RADIUS = 0.5;

    private final TiltAttributeManager mAttributeManager = TiltAttributeManager.getInstance();
    private final GraphicRenderer mGraphicRenderer;
    private final RenderableLayer mGroundConnectorLayer = new RenderableLayer();
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final TiltManager mManager = TiltManager.getInstance();
    private final TiltOptionsView mOptionsView;
    private final RenderableLayer mPassiveLayer = new RenderableLayer();
    private final RenderableLayer mPinLayer = new RenderableLayer();
    private final RenderableLayer mSurfaceLayer = new RenderableLayer();
    private final RenderableLayer mSymbolLayer = new RenderableLayer();

    public TiltLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new TiltOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mPassiveLayer, mOptionsView.getGraphicCheckModel());
        initListeners();

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mPassiveLayer, mLabelLayer, mSymbolLayer, mPinLayer, mGroundConnectorLayer, mSurfaceLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_TiltAction());
        setCategory(mLayer, SDict.STRUCTURAL.toString());
        setName(Bundle.CTL_TiltAction());
        attachTopComponentToLayer("TiltTopComponent", mLayer);
        mLayer.setMaxActiveAltitude(6000);
        mSurfaceLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(20000);
        mLabelLayer.setMaxActiveAltitude(2000);
        mGroundConnectorLayer.setMaxActiveAltitude(1000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mPassiveLayer, mSymbolLayer, mPinLayer, mGroundConnectorLayer, mSurfaceLayer);

        mLayer.setPickEnabled(true);
        mPassiveLayer.setPickEnabled(false);
        mSurfaceLayer.setPickEnabled(false);

        mLayer.setEnabled(false);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BStructuralTiltPoint> c) -> {
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
            mGraphicRenderer.reset();

            if (!mLayer.isEnabled()) {
                return;
            }

            var pointBy = mOptionsView.getPointBy();
            switch (pointBy) {
                case NONE -> {
                    mPinLayer.setEnabled(false);
                    mSymbolLayer.setEnabled(false);
                }
                case PIN -> {
                    mSymbolLayer.setEnabled(false);
                    mPinLayer.setEnabled(true);
                }
                case SYMBOL -> {
                    mSymbolLayer.setEnabled(true);
                    mPinLayer.setEnabled(false);
                }
                default ->
                    throw new AssertionError();
            }

            for (var p : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                    var position = Position.fromDegrees(p.getLat(), p.getLon());
                    var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                    var mapObjects = new ArrayList<AVListImpl>();

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(p, position, labelPlacemark));
                    mapObjects.addAll(plotSymbol(p, position, labelPlacemark));

                    mGraphicRenderer.plot(p, position, mapObjects);

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

    private PointPlacemark plotLabel(BStructuralTiltPoint p, TiltLabelBy labelBy, Position position) {
        if (labelBy == TiltLabelBy.NONE) {
            return null;
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(labelBy.getLabel(p));
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    private PointPlacemark plotPin(BStructuralTiltPoint p, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(TiltHelper.getAlarmLevel(p));
        attrs = PinPaddle.W_STAR.applyToCopy(attrs);

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

    private ArrayList<AVListImpl> plotSymbol(BStructuralTiltPoint p, Position position, PointPlacemark labelPlacemark) {
        var mapObjects = new ArrayList<AVListImpl>();
        var cylinder = new Cylinder(position, SYMBOL_HEIGHT, SYMBOL_RADIUS);
        var attrs = mAttributeManager.getAlarmInteriorAttributes(TiltHelper.getAlarmLevel(p));

        cylinder.setAttributes(attrs);
        mapObjects.add(cylinder);
        mSymbolLayer.addRenderable(cylinder);

        if (labelPlacemark != null) {
            cylinder.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            cylinder.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return mapObjects;
    }

}
