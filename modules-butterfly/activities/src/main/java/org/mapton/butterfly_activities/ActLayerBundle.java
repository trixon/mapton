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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import java.awt.Color;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.mapton.butterfly_api.api.BfLayerBundle;
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

    private final RenderableLayer mLayer = new RenderableLayer();
    private final ActManager mManager = ActManager.getInstance();

    public ActLayerBundle() {
        init();
        initRepaint();
        initListeners();

        FxHelper.runLaterDelayed(1000, () -> mManager.updateTemporal(mLayer.isEnabled()));
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(Bundle.CTL_ActAction());
        setCategory(mLayer, "");
        setName(Bundle.CTL_ActAction());
        attachTopComponentToLayer("ActTopComponent", mLayer);
        setParentLayer(mLayer);
//        mLayer.setEnabled(true);
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
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            var attrs = new BasicShapeAttributes();
            attrs.setInteriorMaterial(Material.RED);
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(3.0);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(true);
            attrs.setInteriorOpacity(0.1);

            for (var area : new ArrayList<>(mManager.getTimeFilteredItems())) {
                if (ObjectUtils.allNotNull(area.getLat(), area.getLon())) {
                    var placemark = new PointPlacemark(Position.fromDegrees(area.getLat(), area.getLon()));
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);
                    var attrsPP = new PointPlacemarkAttributes(placemark.getDefaultAttributes());

                    placemark.setLabelText(area.getName());
                    attrsPP.setImageAddress("images/pushpins/plain-white.png");
                    attrsPP.setImageColor(Color.RED);

                    placemark.setAttributes(attrsPP);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrsPP, 1.5));

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(area);
                    });

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    });

                    mLayer.addRenderable(placemark);
                }

                try {
                    var geometry = area.getGeometry();
                    if (geometry instanceof LineString lineString) {
                        var renderable = new SurfacePolyline(attrs, WWHelper.positionsFromGeometry(lineString, 0));
                        mLayer.addRenderable(renderable);
                    } else if (geometry instanceof Polygon polygon) {
                        var renderable = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
                        mLayer.addRenderable(renderable);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

            }

            setDragEnabled(false);
        });
    }
}
