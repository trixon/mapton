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

import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import se.trixon.mapton.core.api.LatLon;
import se.trixon.mapton.core.api.MapController;

/**
 *
 * @author Patrik Karlström
 */
public class GMapsFXMapController extends MapController {

    private final GoogleMap mMap;

    public GMapsFXMapController(GoogleMap map) {
        mMap = map;
    }

    @Override
    public void panTo(LatLon latLon) {
        mMap.panTo(toLatLong(latLon));
    }

    @Override
    public void panTo(LatLon latLong, int zoom) {
        mMap.setZoom(zoom);
        panTo(latLong);
    }

    LatLong getMapCenter() {
        return toLatLong(mOptions.getMapCenter());
    }

    LatLon toLatLon(LatLong latLong) {
        return new LatLon(
                latLong.getLatitude(),
                latLong.getLongitude()
        );
    }

    LatLong toLatLong(LatLon latLon) {
        return new LatLong(
                latLon.getLatitude(),
                latLon.getLongitude()
        );
    }

}
