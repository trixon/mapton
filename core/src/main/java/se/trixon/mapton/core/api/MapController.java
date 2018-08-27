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
package se.trixon.mapton.core.api;

import se.trixon.almond.nbp.NbLog;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapController {

    protected final MaptonOptions mOptions = MaptonOptions.getInstance();

    private LatLon mLatLon;
    private double mLatitude;
    private double mLongitude;
    private int mZoom;

    public MapController() {
    }

//    public void fitBounds(GeocoderGeometry geometry) {
////        try {
////            getMap().fitBounds(geometry.getBounds());
////        } catch (netscape.javascript.JSException e) {
////            panTo(geometry.getLocation());
////        }
//    }
    public LatLon getLatLon() {
        return mLatLon;
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
        NbLog.i(getClass().getSimpleName(), "goHome not implemented");
    }

    public void panTo(LatLon latLong) {
        NbLog.i(getClass().getSimpleName(), "panTo not implemented");
    }

    public void panTo(LatLon latLong, int zoom) {
        NbLog.i(getClass().getSimpleName(), "panTo(Zoom) not implemented");
    }

    public void setLatLon(LatLon latLon) {
        mLatLon = latLon;
        mLatitude = latLon.getLatitude();
        mLongitude = latLon.getLongitude();
    }

    void setZoom(int zoom) {
        mZoom = zoom;
    }

}
