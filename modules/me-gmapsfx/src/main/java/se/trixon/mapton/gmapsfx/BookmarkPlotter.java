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
package se.trixon.mapton.gmapsfx;

import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import javafx.collections.ListChangeListener;
import se.trixon.mapton.api.MBookmark;
import se.trixon.mapton.api.MBookmarkManager;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPlotter {

    private final MBookmarkManager mBookmarkManager = MBookmarkManager.getInstance();
    private final GMapsFXMapEngine mEngine;

    public BookmarkPlotter(GMapsFXMapEngine engine) {
        mEngine = engine;
        mBookmarkManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            updatePlacemarks();
        });

        updatePlacemarks();
    }

    private void updatePlacemarks() {
        mEngine.getMap().clearMarkers();

        for (MBookmark bookmark : mBookmarkManager.getItems()) {
            if (bookmark.isDisplayMarker()) {
                LatLong latLong = new LatLong(bookmark.getLatitude(), bookmark.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLong);
                markerOptions.title(bookmark.getName());
                Marker marker = new Marker(markerOptions);
                mEngine.getMap().addMarker(marker);
            }
        }
    }
}
