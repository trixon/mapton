/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.poi_ww;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import org.mapton.api.MDict;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class PoiLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final MPoiManager mPoiManager = MPoiManager.getInstance();

    public PoiLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(MDict.POI.toString());
        setCategorySystem(mLayer);
        setName(MDict.POI.toString());
        attachTopComponentToLayer("PoiTopComponent", mLayer);
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
    }

    private void initListeners() {
        mPoiManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends MPoi> c) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            for (var poi : new ArrayList<>(mPoiManager.getTimeFilteredItems())) {
                if (poi.isDisplayMarker()) {
                    var placemark = new PointPlacemark(Position.fromDegrees(poi.getLatitude(), poi.getLongitude()));
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);
                    var attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());

                    var style = poi.getStyle();
                    if (style == null) {
                        placemark.setLabelText(poi.getName());
                        attrs.setImageAddress("images/pushpins/plain-white.png");
                        try {
                            attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(poi.getColor())));
                        } catch (Exception e) {
                            // nvm?
                        }
                    } else {
                        var label = style.getLabelText() != null ? style.getLabelText() : poi.getName();
                        placemark.setLabelText(style.isLabelVisible() ? label : null);
                        attrs.setLabelScale(style.getLabelScale());
                        attrs.setImageOffset(WWHelper.offsetFromImageLocation(style.getImageLocation()));
                        if (style.getImageUrl() == null) {
                            attrs.setImageAddress("images/pushpins/plain-white.png");
                        } else {
                            attrs.setImageAddress(style.getImageUrl());
                            attrs.setScale(style.getImageScale());
                            try {
                                attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(style.getImageColor())));
                            } catch (Exception e) {
                                // nvm?
                            }
                        }
                        attrs.setScale(style.getImageScale());
                    }

                    placemark.setAttributes(attrs);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                        mPoiManager.setSelectedItem(poi);
                    });
                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    });

                    mLayer.addRenderable(placemark);
                }
            }

            setDragEnabled(false);
        });
    }
}
