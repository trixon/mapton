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
package org.mapton.butterfly_activities;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class ActLayerBundle extends BfLayerBundle {

    private final ActAttributeManager mAttributeManager = ActAttributeManager.getInstance();
    private final RenderableLayer mLabelLayer = new RenderableLayer();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final ActManager mManager = ActManager.getInstance();
    private final ActOptionsView mOptionsView;
    private final RenderableLayer mPinLayer = new RenderableLayer();

    public ActLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new ActOptionsView(this);

        initListeners();

        FxHelper.runLaterDelayed(1000, () -> mManager.updateTemporal(mLayer.isEnabled()));
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
        mLayer.setName(Bundle.CTL_ActAction());
        setCategory(mLayer, "");
        setName(Bundle.CTL_ActAction());
        attachTopComponentToLayer("ActTopComponent", mLayer);
        mLabelLayer.setEnabled(true);
        mLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(10000);
        mLabelLayer.setMaxActiveAltitude(10000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mPinLayer);
        mLayer.setPickEnabled(true);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BAreaActivity> c) -> {
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

            for (var area : new ArrayList<>(mManager.getTimeFilteredItems())) {
                var mapObjects = new ArrayList<AVListImpl>();
                if (ObjectUtils.allNotNull(area.getLat(), area.getLon())) {
                    var position = Position.fromDegrees(area.getLat(), area.getLon());
                    var labelPlacemark = plotLabel(area, mOptionsView.getLabelBy(), position);

                    mapObjects.add(labelPlacemark);
                    mapObjects.add(plotPin(area, position, labelPlacemark));
                }

                mapObjects.add(plotArea(area));

                var leftClickRunnable = (Runnable) () -> {
                    mManager.setSelectedItemAfterReset(area);
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

    private AVListImpl plotArea(BAreaActivity area) {
        var attrs = mAttributeManager.getSurfaceAttributes(area);
        var attrsHighlight = mAttributeManager.getSurfaceHighlightAttributes(area);
        Renderable renderable = null;
        try {
            var geometry = area.getGeometry();
            if (geometry instanceof LineString lineString) {
                var surfaceObject = new SurfacePolyline(attrs, WWHelper.positionsFromGeometry(lineString, 0));
                surfaceObject.setHighlightAttributes(attrsHighlight);
                renderable = surfaceObject;
                mLayer.addRenderable(renderable);
            } else if (geometry instanceof Polygon polygon) {
                var surfaceObject = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
                surfaceObject.setHighlightAttributes(attrsHighlight);
                renderable = surfaceObject;
                mLayer.addRenderable(renderable);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return (AVListImpl) renderable;
    }

    private PointPlacemark plotLabel(BAreaActivity p, ActLabelBy labelBy, Position position) {
        if (labelBy == ActLabelBy.NONE) {
            return null;
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(labelBy.getLabel(p));
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    private PointPlacemark plotPin(BAreaActivity area, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(area);

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
