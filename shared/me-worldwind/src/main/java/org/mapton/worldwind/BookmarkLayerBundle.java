/*
 * Copyright 2019 Patrik Karlström.
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
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.ListChangeListener;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class BookmarkLayerBundle extends LayerBundle {

    private final MBookmarkManager mBookmarkManager = MBookmarkManager.getInstance();
    private final RenderableLayer mLayer = new RenderableLayer();

    public BookmarkLayerBundle() {
        init();
        initListeners();

        SwingHelper.runLaterDelayed(2000, () -> {
            updatePlacemarks();
        });
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        setPopulated(true);
    }

    private void init() {
        mLayer.setName(Dict.BOOKMARKS.toString());
        setCategorySystem(mLayer);
        setName(Dict.BOOKMARKS.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
    }

    private void initListeners() {
        mBookmarkManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            updatePlacemarks();
        });
    }

    private void updatePlacemarks() {
        mLayer.removeAllRenderables();

        for (MBookmark bookmark : mBookmarkManager.getItems()) {
            if (bookmark.isDisplayMarker()) {
                PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(bookmark.getLatitude(), bookmark.getLongitude()));
                placemark.setLabelText(bookmark.getName());
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                placemark.setEnableLabelPicking(true);

                PointPlacemarkAttributes attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(bookmark.getColor())));
                placemark.setAttributes(attrs);
                placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                    Map<String, Object> propertyMap = new LinkedHashMap<>();
                    propertyMap.put(Dict.NAME.toString(), bookmark.getName());
                    propertyMap.put(Dict.DESCRIPTION.toString(), bookmark.getDescription());
                    propertyMap.put(Dict.CATEGORY.toString(), bookmark.getCategory());
                    propertyMap.put(Dict.COLOR.toString(), javafx.scene.paint.Color.web(bookmark.getColor()));

                    Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
                });

                mLayer.addRenderable(placemark);
            }
        }

        LayerBundleManager.getInstance().redraw();
    }
}
