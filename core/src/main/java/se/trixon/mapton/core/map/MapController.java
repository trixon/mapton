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
package se.trixon.mapton.core.map;

import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.service.geocoding.GeocoderGeometry;
import se.trixon.mapton.core.api.Mapton;
import se.trixon.mapton.core.api.MaptonOptions;

/**
 *
 * @author Patrik Karlström
 */
public class MapController {

    private LatLong mLatLong;
    private double mLatitude;
    private double mLongitude;
    private final MaptonOptions mOptions = MaptonOptions.getInstance();
    private int mZoom;

    public static MapController getInstance() {
        return Holder.INSTANCE;
    }

    private MapController() {
    }

    public void fitBounds(GeocoderGeometry geometry) {
        try {
            getMap().fitBounds(geometry.getBounds());
        } catch (netscape.javascript.JSException e) {
            panTo(geometry.getLocation());
        }
    }

    public LatLong getLatLong() {
        return mLatLong;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLatitudeProj() {
        return mOptions.getMapCooTrans().getLatitude(mLatitude, mLongitude);
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLongitudeProj() {
        return mOptions.getMapCooTrans().getLongitude(mLatitude, mLongitude);
    }

    public int getZoom() {
        return mZoom;
    }

    public void goHome() {
        getMap().panTo(mOptions.getMapHome());
        getMap().setZoom(mOptions.getMapHomeZoom());
    }

    public void panTo(LatLong latLong) {
        getMap().panTo(latLong);
    }

    public void panTo(LatLong latLong, int zoom) {
        getMap().setZoom(zoom);
        panTo(latLong);
    }

    public void setLatLong(LatLong latLong) {
        mLatLong = latLong;
        mLatitude = latLong.getLatitude();
        mLongitude = latLong.getLongitude();
    }

    private GoogleMap getMap() {
        return Mapton.getInstance().getMap();
    }

    void setZoom(int zoom) {
        mZoom = zoom;
    }

    private static class Holder {

        private static final MapController INSTANCE = new MapController();
    }

}
