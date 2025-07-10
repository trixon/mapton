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
package org.mapton.butterfly_tmo.grundvatten;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.butterfly_tmo.api.GrundvattenManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GrundvattenLayerBundle extends BfLayerBundle {

    private final GrundvattenAttributeManager mAttributeManager = GrundvattenAttributeManager.getInstance();
    private final GraphicRenderer mGraphicRenderer;
    private final GrundvattenManager mManager = GrundvattenManager.getInstance();
    private final GrundvattenOptionsView mOptionsView;

    public GrundvattenLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new GrundvattenOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mOptionsView.getGraphicCheckModel());
        initListeners();

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        initCommons(Bundle.CTL_GrundvattenAction(), "TMO", "GrundvattenTopComponent");
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BGrundvatten> c) -> {
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

//        mOptionsView..addListener((p, o, n) -> {
//            repaint();
//        });
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
                default ->
                    throw new AssertionError();
            }

            synchronized (mManager.getTimeFilteredItems()) {
                for (var p : mManager.getTimeFilteredItems()) {
                    if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                        var position = Position.fromDegrees(p.getLat(), p.getLon());

                        var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                        var mapObjects = new ArrayList<AVListImpl>();

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(p, position, labelPlacemark));

                        mGraphicRenderer.plot(p, position, mapObjects);

                        var leftClickRunnable = (Runnable) () -> {
                            mManager.setSelectedItemAfterReset(p);
                        };

                        var leftDoubleClickRunnable = (Runnable) () -> {
                            Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                            if (!p.ext().getObservationsTimeFiltered().isEmpty()) {
                                mGraphicRenderer.addToAllowList(p.getName());
                                repaint();
                            }
                        };

                        mapObjects.stream().filter(r -> r != null).forEach(r -> {
                            r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                            r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                        });
                    }
                }
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BGrundvatten p, GrundvattenLabelBy labelBy, Position position) {
        if (labelBy == GrundvattenLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BGrundvatten grundvatten, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(grundvatten);

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
