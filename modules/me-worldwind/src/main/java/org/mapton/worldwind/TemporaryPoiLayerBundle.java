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
package org.mapton.worldwind;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MKey;
import org.mapton.api.MTemporaryPoiManager;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class TemporaryPoiLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final MTemporaryPoiManager mManager = MTemporaryPoiManager.getInstance();

    public TemporaryPoiLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(0);
    }

    private void init() {
        mLayer.setName(Dict.TEMPORARY_CONTENT.toString());
        setCategorySystem(mLayer);
        setName(Dict.TEMPORARY_CONTENT.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(false);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        mManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            try {
                Thread.sleep(Duration.ofSeconds(1));
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            for (var poi : new ArrayList<>(mManager.getItems())) {
                if (poi.isDisplayMarker() && ObjectUtils.allNotNull(poi.getLatitude(), poi.getLongitude())) {
                    PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(poi.getLatitude(), poi.getLongitude()));
                    placemark.setLabelText(poi.getName());
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemark.setEnableLabelPicking(true);

                    PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());
                    attrs.setImageAddress("images/pushpins/plain-white.png");
                    attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(poi.getColor())));
                    attrs.setScale(Mapton.getScalePinImage());
                    attrs.setLabelScale(Mapton.getScalePinLabel());
                    placemark.setAttributes(attrs);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                    placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                        Map<String, Object> propertyMap = new LinkedHashMap<>();
                        propertyMap.put(Dict.NAME.toString(), poi.getName());
                        propertyMap.put(Dict.DESCRIPTION.toString(), poi.getDescription());
                        propertyMap.put(Dict.CATEGORY.toString(), poi.getCategory());
                        propertyMap.put(Dict.COLOR.toString(), javafx.scene.paint.Color.web(poi.getColor()));

                        Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
                    });

                    mLayer.addRenderable(placemark);
                }
            }

            setDragEnabled(false);
        });
    }
}
