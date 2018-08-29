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
package se.trixon.mapton.jxmapviewer2;

import org.jxmapviewer.viewer.GeoPosition;
import se.trixon.mapton.core.api.LatLon;
import se.trixon.mapton.core.api.MapController;

/**
 *
 * @author Patrik Karlström
 */
public class JxMapViewerMapController extends MapController {

    private final MapKit mMapKit;

    public JxMapViewerMapController(MapKit mapKit) {
        mMapKit = mapKit;
    }

    @Override
    public void panTo(LatLon latLong) {
        panTo(latLong, mMapKit.getMainMap().getZoom());
    }

    @Override
    public void panTo(LatLon latLon, int zoom) {
        panAndZoomTo(toGeoPosition(latLon), zoom);
    }

    void panAndZoomTo(GeoPosition geoPosition, int zoom) {
        mMapKit.setGeoPosition(geoPosition);
        mMapKit.setAddressLocation(geoPosition);
        mMapKit.setZoom(zoom);
    }

    GeoPosition toGeoPosition(LatLon latLon) {
        return new GeoPosition(
                latLon.getLatitude(),
                latLon.getLongitude()
        );
    }

    LatLon toLatLon(GeoPosition geoPosition) {
        return new LatLon(
                geoPosition.getLatitude(),
                geoPosition.getLongitude()
        );
    }
}
