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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.mapton.butterfly_topo.api.TopoManager;
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
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final MonManager mManager = MonManager.getInstance();
    private final MonOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();

    public MonLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new MonOptionsView(this);

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
        mLayer.setName(Bundle.CTL_MonmonAction());
        setCategory(mLayer, "");
        setName(Bundle.CTL_MonmonAction());
        attachTopComponentToLayer("MonmonTopComponent", mLayer);
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
            var sortedStations = mManager.getTimeFilteredItems().stream()
                    .filter(m -> m.isParent())
                    .map(m -> m.getName())
                    .sorted((o1, o2) -> o1.compareTo(o2)).toList();

            for (var mon : new ArrayList<>(mManager.getTimeFilteredItems())) {
                var stationIndex = sortedStations.indexOf(mon.getStationName());
                var mapObjects = new ArrayList<AVListImpl>();

                if (ObjectUtils.allNotNull(mon.getLat(), mon.getLon())) {
                    var position = Position.fromDegrees(mon.getLat(), mon.getLon());
                    var labelPlacemark = plotLabel(mon, mOptionsView.getLabelBy(), position);

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(mon, position, labelPlacemark, stationIndex));
                }

                mapObjects.add(plotGroundConnector(mon));
                if (mon.isChild()) {
                    mapObjects.add(plotStationConnector(mon, stationIndex));
                    mapObjects.addAll(plotStatus(mon));
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

            setDragEnabled(false);
        });
    }

    private AVListImpl plotGroundConnector(BMonmon mon) {
        var p0 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()));
        var p1 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()), mon.getControlPoint().getZeroZ());
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getGroundConnectorAttributes());
        mLayer.addRenderable(path);

        return path;
    }

    private PointPlacemark plotLabel(BMonmon p, MonLabelBy labelBy, Position position) {
        if (labelBy == MonLabelBy.NONE) {
            return null;
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
//        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
//        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(labelBy.getLabel(p));
        mLabelLayer.addRenderable(placemark);

        return placemark;
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

    private AVListImpl plotStationConnector(BMonmon mon, int stationIndex) {
        var stationName = mon.getStationName();
        var p = mon.getControlPoint();
        var s = TopoManager.getInstance().getAllItemsMap().get(stationName);
        var p0 = WWHelper.positionFromLatLon(new MLatLon(s.getLat(), s.getLon()), s.getZeroZ());
        var p1 = WWHelper.positionFromLatLon(new MLatLon(mon.getLat(), mon.getLon()), p.getZeroZ());
        var path = new Path(p0, p1);

        path.setAttributes(mAttributeManager.getStationConnectorAttribute(stationIndex));
        mLayer.addRenderable(path);

        return path;
    }

    private ArrayList<AVListImpl> plotStatus(BMonmon mon) {
        var mapObjects = new ArrayList<AVListImpl>();

        var size = 1.0;
        var z7 = mon.getControlPoint().getZeroZ();
        var z1 = z7 - size * 2;
        var z14 = z7 + size * 2;
        var latLon = new MLatLon(mon.getLat(), mon.getLon());

        var p7 = WWHelper.positionFromLatLon(latLon, z7);
        var p1 = WWHelper.positionFromLatLon(latLon, z1);
        var p14 = WWHelper.positionFromLatLon(latLon, z14);

        var ellipsoid7 = new Ellipsoid(p7, size, size, size);
        var ellipsoid1 = new Ellipsoid(p1, size, size, size);
        var ellipsoid14 = new Ellipsoid(p14, size, size, size);

        ellipsoid7.setAttributes(mAttributeManager.getStatusAttributes(mon.getQuota(7)));
        ellipsoid1.setAttributes(mAttributeManager.getStatusAttributes(mon.getQuota(1)));
        ellipsoid14.setAttributes(mAttributeManager.getStatusAttributes(mon.getQuota(14)));

        mLayer.addRenderable(ellipsoid7);
        mLayer.addRenderable(ellipsoid1);
        mLayer.addRenderable(ellipsoid14);

        mapObjects.add(ellipsoid1);
        mapObjects.add(ellipsoid14);
        mapObjects.add(ellipsoid7);

        return mapObjects;
    }

}
