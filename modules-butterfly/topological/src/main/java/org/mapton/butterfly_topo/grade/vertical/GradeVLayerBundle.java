/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade.vertical;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.DoubleStream;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoBaseLayerBundle;
import org.mapton.butterfly_topo.grade.GradeAttributeManager;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.butterfly_topo.grade.vertical.graphics.GraphicRenderer;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GradeVLayerBundle extends TopoBaseLayerBundle {

    private final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();
    private final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    private final GraphicRenderer mGraphicRenderer;
    private final GradeVManager mManager = GradeVManager.getInstance();
    private final GradeVOptionsView mOptionsView;

    public GradeVLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new GradeVOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mPassiveLayer, mOptionsView.getComponentCheckModel());
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
        initCommons(Bundle.CTL_GradeVAction(), SDict.TOPOGRAPHY.toString(), "GradeVTopComponent");

        mLayer.setMaxActiveAltitude(16000);
        mSurfaceLayer.setMaxActiveAltitude(16000);
        mPinLayer.setMaxActiveAltitude(10000);
        mLabelLayer.setMaxActiveAltitude(2000);
        mGroundConnectorLayer.setMaxActiveAltitude(1000);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoGrade> c) -> {
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

        mOptionsView.plotPointProperty().addListener((p, o, n) -> {
            repaint();
        });

        mOptionsView.plotSelectedProperty().addListener((p, o, n) -> {
            repaint();
        });

        mOptionsView.getDistanceSliderPane().selectedProperty().addListener((p, o, n) -> {
            repaint();
        });
        mOptionsView.getDistanceSliderPane().valueProperty().addListener((p, o, n) -> {
            repaint();
        });

        mManager.selectedItemProperty().addListener((ObservableValue<? extends BTopoGrade> observable, BTopoGrade oldValue, BTopoGrade newValue) -> {
            if (mOptionsView.isPlotSelected()) {
                repaint();
            }
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
                    mPinLayer.setMinActiveAltitude(Double.MIN_VALUE);
                    mPinLayer.setMaxActiveAltitude(Double.MAX_VALUE);
                }
                default ->
                    throw new AssertionError();
            }

            synchronized (mManager.getTimeFilteredItems()) {
                mManager.getTimeFilteredItems().stream()
                        .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                        .forEachOrdered(p -> {
                            var elevation = DoubleStream.of(p.getP1().getZeroZ(), p.getP2().getZeroZ()).average().orElse(0.0);
                            var position = Position.fromDegrees(p.getLat(), p.getLon(), elevation);
                            var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                            var mapObjects = new ArrayList<AVListImpl>();

                            mapObjects.add(labelPlacemark);
                            mapObjects.add(plotPin(p, position, labelPlacemark, BComponent.PLANE));
                            if (mOptionsView.isPlotSelected()) {
                                if (p.equals(mManager.getSelectedItem())) {
                                    mGraphicRenderer.plot(p, position, mapObjects);
                                } else if (mManager.getSelectedItem() != null && mOptionsView.getDistanceSliderPane().selectedProperty().get()) {
                                    var llP = BCoordinatrix.toLatLon(p);
                                    var llS = BCoordinatrix.toLatLon(mManager.getSelectedItem());
                                    if (llP.distance(llS) <= mOptionsView.getDistanceSliderPane().valueProperty().doubleValue()) {
                                        mGraphicRenderer.plot(p, position, mapObjects);
                                    }
                                }
                            } else {
                                mGraphicRenderer.plot(p, position, mapObjects);
                            }

                            var leftClickRunnable = (Runnable) () -> {
                                mManager.setSelectedItemAfterReset(p);
                            };

                            var leftDoubleClickRunnable = (Runnable) () -> {
                                Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                                mGraphicRenderer.addToAllowList(p.getName());
                                repaint();
                            };

                            mapObjects.stream().filter(r -> r != null).forEach(r -> {
                                r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                                r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                            });
                        });
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BTopoGrade p, GradeVLabelBy labelBy, Position position) {
        if (labelBy == null || labelBy == GradeVLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BTopoGrade p, Position position, PointPlacemark labelPlacemark, BComponent component) {
        var attrs = mAttributeManager.getPinAttributes(p, BComponent.PLANE);

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
