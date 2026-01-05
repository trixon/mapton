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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Pyramid;
import java.util.ArrayList;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MRunnable;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.PinPaddle;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.graphics.GraphicRenderer;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TopoLayerBundle extends TopoBaseLayerBundle implements MRunnable {

    private static final double Z_BASE_OFFSET = 5.0;

    private final double SYMBOL_HEIGHT = 4.0;
    private final double SYMBOL_RADIUS = 1.5;
    private final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();
    private final ArrayList<AVListImpl> mEmptyDummyList = new ArrayList<>();
    private final GraphicRenderer mGraphicRenderer;
    private final TopoOptionsView mOptionsView;
    private final TopoOptions mOptions = TopoOptions.getInstance();

    public static double getZOffset() {
        return Z_BASE_OFFSET - TopoManager.getInstance().getMinimumZscaled();
    }

    public TopoLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new TopoOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mPassiveLayer, mOptionsView.getGraphicCheckModel());
        initListeners();
        mAttributeManager.setColorBy(mOptions.getColorBy());

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView.getUI();
    }

    @Override
    public void populate() throws Exception {
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    @Override
    public void run() {
    }

    @Override
    public void runOnce() {
        mOptionsView.runOnce();
    }

    private void init() {
        initCommons(Bundle.CTL_ControlPointAction(), SDict.TOPOGRAPHY.toString(), "TopoTopComponent");

        mLabelLayer.setMaxActiveAltitude(2000);
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            mAttributeManager.setColorBy(mOptions.getColorBy());
            SwingHelper.runLaterDelayed(50, () -> {
                resetPaintDelayedResetRunner();
            });
        });

        mOptionsView.registerLayerBundle(this);
        mManager.registerLayerBundle(this, mOptionsView);
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            mGraphicRenderer.reset();

            if (!mLayer.isEnabled()) {
                return;
            }

            var pointBy = mOptions.getPointBy();
            switch (pointBy) {
                case AUTO -> {
                    mPinLayer.setEnabled(true);
                    mSymbolLayer.setEnabled(true);
                    var pinSymbolCutOff = 400.0;
                    mSymbolLayer.setMaxActiveAltitude(pinSymbolCutOff);
                    mPinLayer.setMinActiveAltitude(pinSymbolCutOff);
                }
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
                case SYMBOL -> {
                    mPinLayer.setEnabled(false);
                    mSymbolLayer.setEnabled(true);
                    mSymbolLayer.setMinActiveAltitude(Double.MIN_VALUE);
                    mSymbolLayer.setMaxActiveAltitude(Double.MAX_VALUE);
                }
                default ->
                    throw new AssertionError();
            }

            synchronized (mManager.getTimeFilteredItems()) {
                mManager.getTimeFilteredItems().stream()
                        .sorted((o1, o2) -> Double.compare(o1.ext().getAlarmLevel(), o2.ext().getAlarmLevel()))
                        .forEachOrdered(p -> {
//                for (var p : mManager.getTimeFilteredItems()) {
                            if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                                var position = BCoordinatrix.toPositionWW2d(p);
                                var labelPlacemark = plotLabel(p, mOptions.getLabelBy(), position);
                                var mapObjects = new ArrayList<AVListImpl>();

                                mapObjects.add(labelPlacemark);
                                mapObjects.add(plotPin(p, position, labelPlacemark));
                                mapObjects.addAll(plotSymbol(p, position, labelPlacemark));

                                mGraphicRenderer.plot(p, mManager.getSelectedItem(), position, mapObjects, mOptions);
                                var leftClickRunnable = (Runnable) () -> {
                                    mManager.setSelectedItemAfterReset(p);
                                };

                                var leftDoubleClickRunnable = (Runnable) () -> {
                                    Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                                    mGraphicRenderer.addToAllowList(p);
                                    resetPaintDelayedResetRunner();
                                };

                                mapObjects.stream().filter(r -> r != null).forEach(r -> {
                                    r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                                    r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                                });
                            }
//                }
                        });

                mGraphicRenderer.postPlot();
            }

            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BTopoControlPoint p, TopoLabelBy labelBy, Position position) {
        if (labelBy == TopoLabelBy.NONE) {
            return null;
        } else {
            var placemark = createPlacemark(position, "", mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);
            Runnable task = () -> {
                var label = labelBy.getLabel(p);
                p.setValue(BKey.PIN_NAME, label);
                placemark.setLabelText(label);
            };
            Thread.ofVirtual().start(task);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BTopoControlPoint p, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(p);
        switch (p.getDimension()) {
            case _1d ->
                attrs = PinPaddle.N_CIRCLE.applyToCopy(attrs);
            case _2d ->
                attrs = PinPaddle.N_SQUARE.applyToCopy(attrs);
            case _3d ->
                attrs = PinPaddle.N_BLANK.applyToCopy(attrs);
            default ->
                throw new AssertionError();
        }

        if (p.getMeasurementMode() == BMeasurementMode.AUTOMATIC) {
            attrs.setScale(attrs.getScale() * 0.9);
        } else {
            attrs.setScale(attrs.getScale() * 1.1);
        }

        p.setValue(BKey.PIN_URL, attrs.getImageAddress());
        p.setValue(BKey.PIN_COLOR, attrs.getImageColor());

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

    private ArrayList<AVListImpl> plotSymbol(BTopoControlPoint p, Position position, PointPlacemark labelPlacemark) {
        var mapObjects = new ArrayList<AVListImpl>();
        var symbolRadius = SYMBOL_RADIUS;
        if (p.getMeasurementMode() == BMeasurementMode.AUTOMATIC) {
            symbolRadius *= 0.9;
        } else {
            symbolRadius *= 1.1;
        }

        var center = WWHelper.positionFromPosition(position, symbolRadius / 2);

        AbstractShape abstractShape = null;
        if (null != p.getDimension()) {
            var symbolHeight = SYMBOL_HEIGHT;
            if (p.getMeasurementMode() == BMeasurementMode.AUTOMATIC) {
                symbolHeight *= 0.9;
            } else {
                symbolHeight *= 1.1;
            }
            switch (p.getDimension()) {
                case _1d -> {
                    abstractShape = new Ellipsoid(position, symbolRadius, symbolHeight / 2, symbolRadius);
                }
                case _2d -> {
                    abstractShape = new Cylinder(position, 0.5, symbolRadius);
                }
                case _3d -> {
                    abstractShape = new Pyramid(center, symbolHeight * 1.3, symbolRadius * 2);
                }
                default -> {
                }
            }
        }

        var attrs = mAttributeManager.getSymbolAttributes(p);

        abstractShape.setAttributes(attrs);
        mapObjects.add(abstractShape);
        mSymbolLayer.addRenderable(abstractShape);

        if (labelPlacemark != null) {
            abstractShape.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            abstractShape.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return mapObjects;
    }
}
