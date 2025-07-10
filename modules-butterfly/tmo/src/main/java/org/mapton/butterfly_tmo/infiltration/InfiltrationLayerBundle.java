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
package org.mapton.butterfly_tmo.infiltration;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.awt.Color;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.tmo.BInfiltration;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class InfiltrationLayerBundle extends BfLayerBundle {

    private final InfiltrationAttributeManager mAttributeManager = InfiltrationAttributeManager.getInstance();
    private final ComponentRenderer mComponentRenderer;
    private final InfiltrationManager mManager = InfiltrationManager.getInstance();
    private final InfiltrationOptionsView mOptionsView;

    public InfiltrationLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new InfiltrationOptionsView(this);
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
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        initCommons(Bundle.CTL_InfiltrationAction(), "TMO", "InfiltrationTopComponent");
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BInfiltration> c) -> {
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

            synchronized (mManager.getTimeFilteredItems()) {
                for (var p : mManager.getTimeFilteredItems()) {
                    if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                        var position = Position.fromDegrees(p.getLat(), p.getLon());

                        var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                        var mapObjects = new ArrayList<AVListImpl>();

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(position, labelPlacemark));

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
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BInfiltration p, InfiltrationLabelBy labelBy, Position position) {
        if (labelBy == InfiltrationLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(Color.ORANGE);

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
