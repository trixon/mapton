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
package org.mapton.butterfly_remote.insar;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.util.ArrayList;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_core.api.PinPaddle;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_remote.api.RemoteHelper;
import org.mapton.butterfly_remote.insar.graphics.GraphicItem;
import org.mapton.butterfly_remote.insar.graphics.GraphicRenderer;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.mapton.worldwind.api.analytic.AnalyticGrid;
import org.mapton.worldwind.api.analytic.CellAggregate;
import org.mapton.worldwind.api.analytic.GridData;
import org.mapton.worldwind.api.analytic.GridValue;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class InsarLayerBundle extends BfLayerBundle {

    private final double SYMBOL_HEIGHT = .1;
    private final double SYMBOL_RADIUS = 1.5;

    private final InsarAttributeManager mAttributeManager = InsarAttributeManager.getInstance();
    private final GraphicRenderer mGraphicRenderer;
    private final InsarManager mManager = InsarManager.getInstance();
    private final InsarOptionsView mOptionsView;
    private final InsarOptions mOptions = InsarOptions.getInstance();

    public InsarLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new InsarOptionsView(this);
        mGraphicRenderer = new GraphicRenderer(mLayer, mPassiveLayer, mOptionsView.getGraphicsCheckModel());
        initListeners();

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

    private void init() {
        initCommons(Mapton.addWarning(Bundle.CTL_InsarAction(), 0), RemoteHelper.CAT_REMOTE, "InsarTopComponent");

        mLayer.setMaxActiveAltitude(6000);
        mSurfaceLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(20000);
        mLabelLayer.setMaxActiveAltitude(2000);
    }

    private void initListeners() {
        mOptions.registerLayerBundle(this);
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

            synchronized (mManager.getTimeFilteredItems()) {
                for (var p : mManager.getTimeFilteredItems()) {
                    if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                        var position = Position.fromDegrees(p.getLat(), p.getLon());
                        var labelPlacemark = plotLabel(p, mOptions.getLabelBy(), position);
                        var mapObjects = new ArrayList<AVListImpl>();

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(p, position, labelPlacemark));
                        mapObjects.addAll(plotSymbol(p, position, labelPlacemark));

                        mGraphicRenderer.plot(p, mManager.getSelectedItem(), position, mapObjects, mOptions);
                        addClickArea(position, mapObjects);

                        var leftClickRunnable = (Runnable) () -> {
                            mManager.setSelectedItemAfterReset(p);
                        };

                        var leftDoubleClickRunnable = (Runnable) () -> {
                            Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                            if (!p.ext().getObservationsTimeFiltered().isEmpty()) {
                                mGraphicRenderer.addToAllowList(p);
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

            if (mOptionsView.getGraphicsCheckModel().isChecked(GraphicItem.HEAT_MAP)) {
                var values = mManager.getTimeFilteredItems().stream()
                        .map(p -> new GridValue(p.getLat(), p.getLon(), p.ext().deltaZero().getDeltaZ() * 1000))
                        //                        .map(p -> new GridValue(p.getLat(), p.getLon(), p.getVelocity()))
                        .toList();

                int width = 600;
                int height = 600;

                var gridData = new GridData(width, height, values, CellAggregate.MIN);
                var analyticGrid = new AnalyticGrid(mLayer, 50, -10, +10);
                analyticGrid.setNullOpacity(0.0);
                analyticGrid.setZeroOpacity(0.3);
                analyticGrid.setZeroValueSearchRange(5);
                analyticGrid.setGridData(gridData);
                var surface = analyticGrid.getSurface();
                mSurfaceLayer.addRenderable(surface);
            }
            setDragEnabled(false);
        });
    }

    private PointPlacemark plotLabel(BRemoteInsarPoint p, InsarLabelBy labelBy, Position position) {
        if (labelBy == InsarLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BRemoteInsarPoint p, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(p);
        attrs = PinPaddle.W_CIRCLE.applyToCopy(attrs);

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

    private ArrayList<AVListImpl> plotSymbol(BRemoteInsarPoint p, Position position, PointPlacemark labelPlacemark) {
        var mapObjects = new ArrayList<AVListImpl>();
        var attrs = mAttributeManager.getSymbolAttributes(p);
        var value = switch (mOptions.getColorBy()) {
            case ACCELERATION ->
                p.getAcceleration();
            case ALARM, DISPLACEMENT ->
                p.ext().deltaZero().getDeltaZ();
            case VELOCITY ->
                p.getVelocity();
            case VELOCITY_3 ->
                p.getVelocity3m();
            case VELOCITY_6 ->
                p.getVelocity6m();
            default ->
                Double.MAX_VALUE;
        };

        var polygon = new Polygon();
        ArrayList<LatLon> nodes;
        if (Math.abs(value) <= 0.004) {
            nodes = WWHelper.createNodes(position, SYMBOL_RADIUS, 6);
        } else if (value > 0) {
            nodes = WWHelper.createNodes(position, SYMBOL_RADIUS, 3);
        } else {
            nodes = WWHelper.createNodes(position, SYMBOL_RADIUS, 3, 180);
        }
        polygon.setLocations(nodes);
        polygon.setAltitudes(0, SYMBOL_HEIGHT);
        polygon.setAttributes(attrs);
        mapObjects.add(polygon);
        mSymbolLayer.addRenderable(polygon);

        if (labelPlacemark != null) {
            polygon.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            polygon.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return mapObjects;
    }

}
