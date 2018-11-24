/*
 * Copyright 2018 Patrik Karlström.
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
import java.util.prefs.PreferenceChangeEvent;
import javafx.collections.ListChangeListener;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MEngine;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class BookmarkLayerBundle extends LayerBundle {

    private final MBookmarkManager mBookmarkManager = MBookmarkManager.getInstance();
    private final RenderableLayer mBookmarksLayer = new RenderableLayer();
    private final WorldWindMapEngine mEngine;
    private final MOptions mOptions = MOptions.getInstance();

    public BookmarkLayerBundle() {
        mEngine = (WorldWindMapEngine) MEngine.byName("WorldWind");
        mBookmarksLayer.setName(String.format("~ %s ~", Dict.BOOKMARKS.toString()));
        mBookmarksLayer.setEnabled(true);
        mBookmarkManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            updatePlacemarks();
        });

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_HOME_LAT:
                    updatePlacemarks();
                    break;
            }
        });

        updatePlacemarks();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mBookmarksLayer);
        setPopulated(true);
    }

    private void updatePlacemarks() {
        mBookmarksLayer.removeAllRenderables();

        for (MBookmark bookmark : mBookmarkManager.getItems()) {
            if (bookmark.isDisplayMarker()) {
                PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(bookmark.getLatitude(), bookmark.getLongitude()));
                placemark.setLabelText(bookmark.getName());
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                placemark.setEnableLabelPicking(true);

                mBookmarksLayer.addRenderable(placemark);
            }
        }

        MLatLon home = mOptions.getMapHome();
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(home.getLatitude(), home.getLongitude()));
        placemark.setLabelText(Dict.HOME.toString());
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setEnableLabelPicking(true);

        mBookmarksLayer.addRenderable(placemark);

        LayerBundleManager.getInstance().redraw();
    }
}
