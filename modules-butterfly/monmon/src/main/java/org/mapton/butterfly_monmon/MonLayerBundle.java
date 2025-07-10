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
package org.mapton.butterfly_monmon;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MonLayerBundle extends BfLayerBundle {

    private final MonAttributeManager mAttributeManager = MonAttributeManager.getInstance();
    private final GraphicRenderer mGraphicRenderer;
    private final MonManager mManager = MonManager.getInstance();
    private final MonOptionsView mOptionsView;

    public MonLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new MonOptionsView(this);
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
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        initCommons(Bundle.CTL_MonmonAction(), "", "MonmonTopComponent");

        mLabelLayer.setEnabled(true);
        mLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(10000);
        mLabelLayer.setMaxActiveAltitude(10000);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BMonmon> c) -> {
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

            var sortedStations = mManager.getAllItems().stream()
                    .filter(m -> m.isParent())
                    .map(m -> m.getName())
                    .sorted((o1, o2) -> o1.compareTo(o2)).toList();

            var pointToZ = new HashMap<String, Double>();
            var stationNames = mManager.getFilteredItems().stream().map(m -> m.getStationName()).collect(Collectors.toSet());
            for (var stationName : stationNames) {
                var min = mManager.getFilteredItems().stream()
                        .filter(m -> m.getStationName().equalsIgnoreCase(stationName) || m.getName().equalsIgnoreCase(stationName))
                        .mapToDouble(m -> m.getControlPoint().getZeroZ())
                        .min().orElse(0);

                pointToZ.put(stationName, min);
            }

            synchronized (mManager.getTimeFilteredItems()) {
                for (var mon : mManager.getTimeFilteredItems()) {
                    var stationIndex = sortedStations.indexOf(mon.getStationName());
                    var mapObjects = new ArrayList<AVListImpl>();

                    if (ObjectUtils.allNotNull(mon.getLat(), mon.getLon())) {
                        var position = Position.fromDegrees(mon.getLat(), mon.getLon());
                        var labelPlacemark = plotLabel(mon, mOptionsView.getLabelBy(), position);

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(mon, position, labelPlacemark, stationIndex));
                        mGraphicRenderer.plot(mon, position, stationIndex, pointToZ, mapObjects);
                    }

                    var leftClickRunnable = (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(mon);
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

    private PointPlacemark plotLabel(BMonmon p, MonLabelBy labelBy, Position position) {
        if (labelBy == MonLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BMonmon area, Position position, PointPlacemark labelPlacemark, int stationIndex) {
        var attrs = mAttributeManager.getPinAttributes(stationIndex);
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
