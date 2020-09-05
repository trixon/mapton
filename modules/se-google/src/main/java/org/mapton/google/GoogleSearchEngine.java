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
package org.mapton.google;

import com.dlsc.gmapsfx.GoogleMapView;
import com.dlsc.gmapsfx.javascript.object.LatLong;
import com.dlsc.gmapsfx.javascript.object.LatLongBounds;
import com.dlsc.gmapsfx.service.geocoding.GeocoderGeometry;
import com.dlsc.gmapsfx.service.geocoding.GeocoderStatus;
import com.dlsc.gmapsfx.service.geocoding.GeocodingResult;
import com.dlsc.gmapsfx.service.geocoding.GeocodingService;
import com.dlsc.gmapsfx.service.geocoding.GeocodingServiceCallback;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.mapton.api.MBookmark;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.google_maps.api.GoogleMaps;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class GoogleSearchEngine {

    boolean mDone = false;

    public GoogleSearchEngine() {
        GoogleMapView googleMapView = new GoogleMapView(Locale.getDefault().getLanguage(), GoogleMaps.getKey());
    }

    public String getName() {
        return "Google Maps";
    }

    public ArrayList<MBookmark> getResults(String searchString) {
        ArrayList<MBookmark> bookmarks = new ArrayList<>();

        mDone = false;

        GeocodingServiceCallback callback = (GeocodingResult[] results, GeocoderStatus status) -> {
            if (status != GeocoderStatus.OK) {
                mDone = true;
                return;
            }

            for (GeocodingResult result : results) {
                GeocoderGeometry geometry = result.getGeometry();
                LatLong latLon = geometry.getLocation();
                MBookmark bookmark = new MBookmark();
                bookmark.setName(result.getFormattedAddress());
                bookmark.setLatitude(latLon.getLatitude());
                bookmark.setLongitude(latLon.getLongitude());
                LatLongBounds bounds = geometry.getBounds();
                MLatLon southWest = new MLatLon(bounds.getSouthWest().getLatitude(), bounds.getSouthWest().getLongitude());
                MLatLon northEast = new MLatLon(bounds.getNorthEast().getLatitude(), bounds.getNorthEast().getLongitude());
                MLatLonBox latLonBox = new MLatLonBox(southWest, northEast);
                bookmark.setLatLonBox(latLonBox);
                bookmarks.add(bookmark);
            }

            mDone = true;
        };

        Platform.runLater(() -> {
            GeocodingService service = new GeocodingService();
            service.geocode(searchString, callback);
        });

        do {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        } while (!mDone);

        return bookmarks;
    }

    public boolean isInstantSearch() {
        return false;
    }
}
