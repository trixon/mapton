/*
 * Copyright 2020 Patrik Karlström.
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
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
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
        mPoiManager.getFilteredItems().addListener((ListChangeListener.Change<? extends MPoi> c) -> {
            repaint();
        });

        Mapton.getGlobalState().addListener(gsce -> {
            Platform.runLater(() -> {
                sendObjectProperties(gsce.getValue());
            });
        }, MKey.POI_SELECTION);
    }

    private void initRepaint() {
        setPainter(() -> {
            mLayer.removeAllRenderables();

            for (MPoi poi : mPoiManager.getFilteredItems()) {
                if (poi.isDisplayMarker()) {
                    PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(poi.getLatitude(), poi.getLongitude()));
                    placemark.setLabelText(poi.getName());
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);

                    PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());
                    attrs.setImageAddress("images/pushpins/plain-white.png");
                    try {
                        attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(poi.getColor())));
                    } catch (Exception e) {
                        // nvm?
                    }
                    placemark.setAttributes(attrs);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                        Mapton.getGlobalState().put(MKey.POI_SELECTION_MAP, poi);
                    });

                    mLayer.addRenderable(placemark);
                }
            }
        });
    }

    private void sendObjectProperties(MPoi poi) {
        Map<String, Object> propertyMap = new LinkedHashMap<>();

        if (poi != null) {
            propertyMap.put(Dict.NAME.toString(), poi.getName());
            propertyMap.put(Dict.CATEGORY.toString(), poi.getCategory());
            propertyMap.put(Dict.SOURCE.toString(), poi.getProvider());
            propertyMap.put(Dict.DESCRIPTION.toString(), poi.getDescription());
            propertyMap.put(Dict.TAGS.toString(), poi.getTags());
            propertyMap.put(Dict.COLOR.toString(), javafx.scene.paint.Color.web(poi.getColor()));
            propertyMap.put("URL", poi.getUrl());
        }

        Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
    }
}
