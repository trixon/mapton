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
import javafx.collections.ListChangeListener;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.Mapton;
import org.mapton.core.api.BookmarkEditor;
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
public class BookmarkLayerBundle extends LayerBundle {

    private final BookmarkEditor mBookmarkEditor = new BookmarkEditor();
    private final RenderableLayer mLayer = new RenderableLayer();
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();

    public BookmarkLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(2000);
    }

    private void init() {
        mLayer.setName(Dict.BOOKMARKS.toString());
        setCategorySystem(mLayer);
        setName(Dict.BOOKMARKS.toString());
        mLayer.setPickEnabled(true);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        mManager.getFilteredItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            repaint();
        });

        mManager.lastSavedProperty().addListener((p, o, n) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            synchronized (mManager.getFilteredItems()) {
                for (var bookmark : mManager.getFilteredItems()) {
                    if (bookmark.isDisplayMarker()) {
                        var placemark = new PointPlacemark(Position.fromDegrees(bookmark.getLatitude(), bookmark.getLongitude()));
                        placemark.setLabelText(bookmark.getName());
                        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                        placemark.setEnableLabelPicking(true);

                        var attrs = new PointPlacemarkAttributes(placemark.getDefaultAttributes());
                        attrs.setImageAddress("images/pushpins/plain-white.png");
                        attrs.setImageColor(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(bookmark.getColor())));
                        attrs.setScale(Mapton.getScalePinImage());
                        attrs.setLabelScale(Mapton.getScalePinLabel());
                        placemark.setAttributes(attrs);
                        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

                        placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, (Runnable) () -> {
                            mManager.setSelectedItem(bookmark);
                        });

                        placemark.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                            mBookmarkEditor.editBookmark(bookmark);
                        });

                        mLayer.addRenderable(placemark);
                    }
                }
            }

            setDragEnabled(false);
        });
    }
}
