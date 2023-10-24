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
import gov.nasa.worldwind.render.AbstractShape;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Pyramid;
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
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TopoLayerBundle extends LayerBundle {

    public static final double SCALE_FACTOR = 500.0;
    public static final double Z_OFFSET = 10.0;

    private final double SYMBOL_HEIGHT = 4.0;
    private final double SYMBOL_RADIUS = 1.5;
    private final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();
    private final ArrayList<AVListImpl> mEmptyDummyList = new ArrayList<>();
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final ComponentRenderer mComponentRenderer;
    private final TopoManager mManager = TopoManager.getInstance();
    private final TopoOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();
    private final RenderableLayer mSymbolLayer = new RenderableLayer();
    private final TopoConfig mTopoConfig = new TopoConfig();

    public TopoLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new TopoOptionsView(this);
        mComponentRenderer = new ComponentRenderer(mLayer, mOptionsView.getComponentCheckModel());
        initListeners();

        FxHelper.runLaterDelayed(1000, () -> mManager.updateTemporal(mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mLabelLayer, mSymbolLayer, mPinLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_TopoAction());
        setCategory(mLayer, "Butterfly");
        setName(Bundle.CTL_TopoAction());
        attachTopComponentToLayer("TopoTopComponent", mLayer);
        mLabelLayer.setEnabled(true);
        mLabelLayer.setMaxActiveAltitude(2000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mSymbolLayer, mPinLayer);

        mLayer.setPickEnabled(true);
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

        mOptionsView.colorByProperty().addListener((p, o, n) -> {
            mAttributeManager.setColorBy(n);
            repaint();
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

            for (var p : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
                    var position = Position.fromDegrees(p.getLat(), p.getLon());
                    var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
                    var mapObjects = new ArrayList<AVListImpl>();

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(p, position, labelPlacemark));
                    mapObjects.addAll(plotSymbol(p, position, labelPlacemark));
                    mapObjects.addAll(plotIndicators(p, position));

                    mComponentRenderer.plot(p, position, mapObjects);

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
            var circle = new SurfaceCircle(mAttributeManager.getIndicatorNeedAttributes()[idx], p2, radius);
            plotConnector(position, p2);
            mLayer.addRenderable(circle);
            mapObjects.add(circle);
        }

        return mapObjects;
    }

    private PointPlacemark plotLabel(BTopoControlPoint p, TopoLabelBy labelBy, Position position) {
        if (labelBy == TopoLabelBy.NONE) {
            return null;
        }

        String label;
        try {
            label = mOptionsView.getLabelBy().getLabel(p);
        } catch (Exception e) {
            label = "ERROR %s <<<<<<<<".formatted(p.getName());
        }

        var offsetPosition = WWHelper.movePolar(position, 45, SYMBOL_RADIUS * 1.2);
        var placemark = new PointPlacemark(offsetPosition);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(label);
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    private PointPlacemark plotPin(BTopoControlPoint p, Position position, PointPlacemark labelPlacemark) {
        var attrs = new PointPlacemarkAttributes(mAttributeManager.getPinAttributes());
        attrs.setImageColor(mTopoConfig.getColor(p));
        attrs.setImageColor(mAttributeManager.getAlarmColorHeight(p));//FIXME

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
        var center = WWHelper.positionFromPosition(position, SYMBOL_RADIUS / 2);

        AbstractShape abstractShape = null;
        if (null != p.getDimension()) {
            switch (p.getDimension()) {
                case _1d -> {
                    abstractShape = new Ellipsoid(position, SYMBOL_RADIUS, SYMBOL_HEIGHT / 2, SYMBOL_RADIUS);
                }
                case _2d -> {
                    abstractShape = new Cylinder(position, 0.5, SYMBOL_RADIUS);
                }
                case _3d -> {
                    abstractShape = new Pyramid(center, SYMBOL_HEIGHT * 1.3, SYMBOL_RADIUS * 2);
                }
                default -> {
                }
            }
        }

        var sa = new BasicShapeAttributes(mAttributeManager.getSymbolAttributes());
        sa.setInteriorMaterial(new Material(mTopoConfig.getColor(p)));
        abstractShape.setAttributes(sa);
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
