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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Direction;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TopoLayerBundle extends LayerBundle {

    private final ArrayList<AVListImpl> mEmptyDummyList = new ArrayList<>();
    private BasicShapeAttributes[] mIndicatorNeedAttributes;
    private final RenderableLayer mLayer = new RenderableLayer();
    private final TopoManager mManager = TopoManager.getInstance();
    private final TopoOptionsView mOptionsView;

    public TopoLayerBundle() {
        init();
        initAttributes();
        initRepaint();
        mOptionsView = new TopoOptionsView(this);
        initListeners();

//        mManager.updateTemporal(mLayer.isEnabled());
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_TopoAction());
        setCategory(mLayer, "Butterfly");
        setName(Bundle.CTL_TopoAction());
        attachTopComponentToLayer("TopoTopComponent", mLayer);
        setParentLayer(mLayer);
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
    }

    private void initAttributes() {
        //***
        var indicatorNeed = new BasicShapeAttributes();
        var indicatorNeed0 = new BasicShapeAttributes(indicatorNeed);
        var indicatorNeed1 = new BasicShapeAttributes(indicatorNeed);
        var indicatorNeed2 = new BasicShapeAttributes(indicatorNeed);
        indicatorNeed0.setInteriorMaterial(Material.GREEN);
        indicatorNeed1.setInteriorMaterial(Material.ORANGE);
        indicatorNeed2.setInteriorMaterial(Material.RED);

        mIndicatorNeedAttributes = new BasicShapeAttributes[]{
            indicatorNeed0,
            indicatorNeed1,
            indicatorNeed2
        };
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
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

            for (var p : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                    var position = Position.fromDegrees(p.getLat(), p.getLon());
                    var placemark = new PointPlacemark(position);
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);
                    var attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());

                    String label;
                    try {
                        label = mOptionsView.getLabelBy().getLabel(p);
                    } catch (Exception e) {
                        label = "ERROR %s <<<<<<<<".formatted(p.getName());
                    }
                    placemark.setLabelText(label);
                    attrs.setImageAddress("images/pushpins/plain-white.png");
                    attrs.setLabelScale(1.6);

                    placemark.setAttributes(attrs);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                    var mapObjects = new ArrayList<AVListImpl>();
                    mapObjects.add(placemark);

                    mLayer.addRenderable(placemark);
                    mapObjects.addAll(plotIndicators(p, position));

                    var leftClickRunnable = (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(p);
                    };

                    var leftDoubleClickRunnable = (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    };

                    mapObjects.stream().forEach(r -> {
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                    });
                }
            }

            setDragEnabled(false);
        });
    }

    private void plotConnector(Position p1, Position p2) {
        var path = new Path(p1, p2);
        var sa = new BasicShapeAttributes();
        sa.setOutlineMaterial(Material.DARK_GRAY);
        sa.setOutlineWidth(2.0);
        path.setAttributes(sa);
        mLayer.addRenderable(path);
    }

    private ArrayList<AVListImpl> plotIndicators(BTopoControlPoint p, Position position) {
        var checkModel = mOptionsView.getIndicatorCheckModel();
        if (checkModel.getCheckedItems().isEmpty() || p.getFrequency() == 0) {
            return mEmptyDummyList;
        }

        var mapObjects = new ArrayList<AVListImpl>();
        var indicatorDistance = 2.0;

        var untilNextDirection = Direction.WEST;
        var planeDirection = Direction.SOUTH;
        var heightDirection = Direction.NORTH;

        if (checkModel.isChecked(untilNextDirection)) {
            var days = p.ext().getMeasurementUntilNext(ChronoUnit.DAYS);
            var radius = 1.0;
            int idx = 0;

            if (days < -3) {
                idx = 2;
            } else if (days < -1) {
                idx = 1;
            }

            var p2 = WWHelper.movePolar(position, untilNextDirection.getAzimuth(), indicatorDistance);
            var circle = new SurfaceCircle(mIndicatorNeedAttributes[idx], p2, radius);
            plotConnector(position, p2);
            mLayer.addRenderable(circle);
            mapObjects.add(circle);
        }

        return mapObjects;
    }
}
