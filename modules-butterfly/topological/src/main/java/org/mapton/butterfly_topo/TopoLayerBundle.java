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
import java.util.HashMap;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Direction;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TopoLayerBundle extends LayerBundle {

    private static final double Z_OFFSET = 10.0;
    private static final double SCALE_FACTOR = 500.0;

    private final double SYMBOL_HEIGHT = 4.0;
    private final double SYMBOL_RADIUS = 1.5;
    private final ArrayList<AVListImpl> mEmptyDummyList = new ArrayList<>();
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final TopoManager mManager = TopoManager.getInstance();
    private final TopoOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();
    private final RenderableLayer mSymbolLayer = new RenderableLayer();
    private final TopoConfig mTopoConfig = new TopoConfig();
    private final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();
    private final HashMap<BTopoControlPoint, Position[]> mPointToPositionMap = new HashMap<>();

    public TopoLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new TopoOptionsView(this);
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

        mOptionsView.labelByProperty().addListener((p, o, n) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            mPointToPositionMap.clear();

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
                    mapObjects.addAll(plotBearing(p, position));
                    mapObjects.addAll(plotIndicators(p, position));
                    mapObjects.addAll(plotTrace(p, position));
                    mapObjects.addAll(plotVector(p, position));

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

    private ArrayList<AVListImpl> plotBearing(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();
        int size = p.ext().getObservationsFiltered().size();
        if (!mOptionsView.getPlotCheckModel().isChecked(Dict.BEARING.toString())
                || p.getDimension() == BDimension._1d
                || p.ext().getNumOfObservationsTimeFiltered() == 0) {
            return mapObjects;
        }

        int maxNumberOfItemsToPlot = Math.min(10, p.ext().getNumOfObservationsTimeFiltered());

        boolean first = true;
        for (int i = size - 1; i >= size - maxNumberOfItemsToPlot + 1; i--) {
            var o = p.ext().getObservationsFiltered().get(i);

            try {
                var bearing = o.ext().getBearing();
                if (bearing == null || bearing.isNaN()) {
                    continue;
                }

                var length = 10.0;
                var p2 = WWHelper.movePolar(position, bearing, length);
                var z = first ? 0.2 : 0.1;
                position = WWHelper.positionFromPosition(position, z);
                p2 = WWHelper.positionFromPosition(p2, z);
                var path = new Path(position, p2);
                var sa = new BasicShapeAttributes();
                sa.setOutlineMaterial(Material.BLUE);
                path.setAttributes(sa);

                if (first) {
                    first = false;
                    sa.setOutlineWidth(2.0);
                } else {
                    sa.setOutlineWidth(4.0);
                    sa.setOutlineOpacity(0.05);
                }

                mLayer.addRenderable(path);
                mapObjects.add(path);
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        return mapObjects;
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

    private ArrayList<AVListImpl> plotTrace(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();
        var checkModel = mOptionsView.getPlotCheckModel();

        if (checkModel.isChecked(SDict.TRACE_1D.toString()) && p.getDimension() == BDimension._1d) {
            plotTrace1d(p, position, mapObjects);
        } else if (checkModel.isChecked(SDict.TRACE_2D.toString()) && p.getDimension() == BDimension._2d) {
            plotTrace2d(p, position, mapObjects);
        } else if (checkModel.isChecked(SDict.TRACE_3D.toString()) && p.getDimension() == BDimension._3d) {
            plotTrace3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plotTrace1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plotTrace2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private Position[] plot3dOffsetPole(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        return mPointToPositionMap.computeIfAbsent(p, k -> {
            var ZERO_SIZE = 0.5;
            var END_SIZE = 0.5;
            var startPosition = WWHelper.positionFromPosition(position, Z_OFFSET);
            var ellipsoid = new Ellipsoid(startPosition, ZERO_SIZE, ZERO_SIZE, ZERO_SIZE);
            mapObjects.add(ellipsoid);
            mLayer.addRenderable(ellipsoid);

            var groundPath = new Path(position, startPosition);
            mapObjects.add(groundPath);
            mLayer.addRenderable(groundPath);
            var endPosition = startPosition;
            var o = p.ext().getObservationsFiltered().getLast();
            if (o.ext().getDeltaZ() != null) {
                var x = p.getZeroX() + MathHelper.convertDoubleToDouble(o.ext().getDeltaX()) * SCALE_FACTOR;
                var y = p.getZeroY() + MathHelper.convertDoubleToDouble(o.ext().getDeltaY()) * SCALE_FACTOR;
                var z = p.getZeroZ() + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * SCALE_FACTOR + Z_OFFSET;

                var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                endPosition = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);
            }

            var endEllipsoid = new Ellipsoid(endPosition, END_SIZE, END_SIZE, END_SIZE);
            mapObjects.add(endEllipsoid);
            mLayer.addRenderable(endEllipsoid);

            return new Position[]{startPosition, endPosition};
        });
    }

    private void plotTrace3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var positions = plot3dOffsetPole(p, position, mapObjects);
        if (ObjectUtils.anyNull(p.getZeroX(), p.getZeroY(), p.getZeroZ())) {
            return;
        }

        var collectedNodes = p.ext().getObservationsFiltered().stream()
                .map(o -> {
                    var x = p.getZeroX() + MathHelper.convertDoubleToDouble(o.ext().getDeltaX()) * SCALE_FACTOR;
                    var y = p.getZeroY() + MathHelper.convertDoubleToDouble(o.ext().getDeltaY()) * SCALE_FACTOR;
                    var z = p.getZeroZ() + MathHelper.convertDoubleToDouble(o.ext().getDeltaZ()) * SCALE_FACTOR + Z_OFFSET;

                    var wgs84 = MOptions.getInstance().getMapCooTrans().toWgs84(y, x);
                    var p0 = Position.fromDegrees(wgs84.getY(), wgs84.getX(), z);

                    return p0;
                }).toList();

        var nodes = new ArrayList<Position>(collectedNodes);
        nodes.add(0, positions[0]);
        var path = new Path(nodes);
        path.setShowPositions(true);
        mapObjects.add(path);
        mLayer.addRenderable(path);
    }

    private ArrayList<AVListImpl> plotVector(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();
        var checkModel = mOptionsView.getPlotCheckModel();

        if (checkModel.isChecked(SDict.VECTOR_1D.toString()) && p.getDimension() == BDimension._1d) {
            plotVector1d(p, position, mapObjects);
        } else if (checkModel.isChecked(SDict.VECTOR_2D.toString()) && p.getDimension() == BDimension._2d) {
            plotVector2d(p, position, mapObjects);
        } else if (checkModel.isChecked(SDict.VECTOR_3D.toString()) && p.getDimension() == BDimension._3d) {
            plotVector3d(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plotVector1d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plotVector2d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
    }

    private void plotVector3d(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        var positions = plot3dOffsetPole(p, position, mapObjects);
        var startPosition = positions[0];
        var endPosition = positions[1];

        var path = new Path(startPosition, endPosition);
        mapObjects.add(path);
        mLayer.addRenderable(path);

        //plot dZ
        var endDeltaZ = Position.fromDegrees(startPosition.latitude.degrees, startPosition.longitude.degrees, endPosition.getAltitude());
        var pathDeltaZ = new Path(startPosition, endDeltaZ);
        var sa = new BasicShapeAttributes();
        sa.setDrawOutline(true);
        sa.setOutlineWidth(10);
        pathDeltaZ.setAttributes(sa);
        mapObjects.add(pathDeltaZ);
        mLayer.addRenderable(pathDeltaZ);

        //plot dR
        var pathDeltaR = new Path(endDeltaZ, endPosition);
        var sar = new BasicShapeAttributes();
        sar.setDrawOutline(true);
        sar.setOutlineWidth(10);
        pathDeltaR.setAttributes(sar);
        mapObjects.add(pathDeltaR);
        mLayer.addRenderable(pathDeltaR);
    }
}
