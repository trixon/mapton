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
package org.mapton.api;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MEngine {

    public static final String KEY_STATUS_COORDINATE = "Status.Coordinate";
    public static final String KEY_STATUS_PROGRESS = "Status.Progress";

    protected static final Logger LOGGER = Logger.getLogger(MEngine.class.getName());
    private static final TreeMap<String, MEngine> ENGINES = new TreeMap<>();

    protected final MOptions mMaptonOptions = MOptions.getInstance();
    private Double mAltitude;
    private Double mElevation;
    private Callable<BufferedImage> mImageRenderer;
    private boolean mInitialized;
    private MLatLon mLatLonMouse;
    private Double mLatitude;
    private Double mLockedLatitude;
    private Double mLockedLongitude;
    private Double mLongitude;

    static {
        Lookup.getDefault().lookupResult(MEngine.class).addLookupListener((LookupEvent ev) -> {
            populateEngines();
        });

        populateEngines();
    }

    public static MEngine byName(String name) {
        return ENGINES.getOrDefault(name, null);
    }

    private static void populateEngines() {
        ENGINES.clear();
        Lookup.getDefault().lookupAll(MEngine.class).forEach((engine) -> {
            ENGINES.put(engine.getName(), engine);
        });
    }

    public MEngine() {
    }

    public void displayContextMenu(Point screenXY) {
        mLockedLatitude = mLatitude;
        mLockedLongitude = mLongitude;

//        aaaSwingUtilities.invokeLater(() -> {
//            MapTopComponent tc = (MapTopComponent) WindowManager.getDefault().findTopComponent("MapTopComponent");
//            tc.displayContextMenu(screenXY);
//        });
    }

    public void fitToBounds(MLatLonBox latLonBox) {
//        aaaNbLog.i(getClass().getSimpleName(), "fitToBounds not implemented");
    }

    public Double getAltitude() {
        return mAltitude;
    }

    public MLatLon getCenter() {
//        aaaNbLog.i(getClass().getSimpleName(), "getCenter not implemented");
        return new MLatLon(0, 0);
    }

    public Double getElevation() {
        return mElevation;
    }

    public Callable<BufferedImage> getImageRenderer() {
        return mImageRenderer;
    }

    public MLatLon getLatLon() {
        return new MLatLon(mLatitude, mLongitude);
    }

    public MLatLon getLatLonMouse() {
        return mLatLonMouse;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public double getLatitudeProj() {
//        aaareturn mMaptonOptions.getMapCooTrans().getLatitude(mLatitude, mLongitude);
        return 0;
    }

    public Node getLayerView() {
//        aaaNbLog.i(getClass().getSimpleName(), "getLayerView not implemented");

        return new Pane();
    }

    public MLatLon getLockedLatLon() {
        return new MLatLon(mLockedLatitude, mLockedLongitude);
    }

    public Double getLockedLatitude() {
        return mLockedLatitude;
    }

    public double getLockedLatitudeProj() {
//        aaareturn mMaptonOptions.getMapCooTrans().getLatitude(mLockedLatitude, mLockedLongitude);
        return 0;
    }

    public Double getLockedLongitude() {
        return mLockedLongitude;
    }

    public double getLockedLongitudeProj() {
//        aaareturn mMaptonOptions.getMapCooTrans().getLongitude(mLockedLatitude, mLockedLongitude);
        return 0;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public double getLongitudeProj() {
//        aaareturn mMaptonOptions.getMapCooTrans().getLongitude(mLatitude, mLongitude);
        return 0;
    }

    public abstract String getName();

    public Node getRulerView() {
//        aaaNbLog.i(getClass().getSimpleName(), "getRulerView not implemented");

        return new Pane();
    }

    public MStatusZoomMode getStatusZoomMode() {
        return MStatusZoomMode.ABSOLUTE;
    }

    public abstract Node getStyleView();

    public abstract Node getUI();

    public double getZoom() {
//        aaaNbLog.i(getClass().getSimpleName(), "getZoom not implemented");
        return 0.2;
    }

    public final void goHome() {
        panTo(mMaptonOptions.getMapHome(), mMaptonOptions.getMapHomeZoom());
    }

    public final void initialized() {
        mInitialized = true;
        panTo(mMaptonOptions.getMapCenter(), mMaptonOptions.getMapZoom());
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public boolean isSwing() {
        return true;
    }

    public void log(String message) {
//        aaaNbLog.v(getClass().getSimpleName(), message);
    }

    public void onActivate() {
    }

    public void onClosing() {
    }

    public void onDeactivate() {
    }

    public void onOpening() {
    }

    public void onWhatsHere(String s) {
//        aaaNbLog.i(getClass().getSimpleName(), "displayWhatsHere not implemented");
    }

    public void panTo(MLatLon latLon) {
//        aaaNbLog.i(getClass().getSimpleName(), "panTo not implemented");
    }

    public void panTo(MLatLon latLon, double zoom) {
//        aaaNbLog.i(getClass().getSimpleName(), "panTo(Zoom) not implemented");
    }

    public void setImageRenderer(Callable<BufferedImage> imageRenderer) {
        mImageRenderer = imageRenderer;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public final void setStatusMousePositionData(MLatLon latLonMouse, Double elevation, Double altitude) {
        mLatLonMouse = latLonMouse;

        if (latLonMouse != null) {
            mLatitude = latLonMouse.getLatitude();
            mLongitude = latLonMouse.getLongitude();
        } else {
            mLatitude = null;
            mLongitude = null;
        }

        mElevation = elevation;
        mAltitude = altitude;

        Mapton.getGlobalState().put(KEY_STATUS_COORDINATE, latLonMouse);
    }

    public void setStatusProgress(double progress) {
        Mapton.getGlobalState().put(KEY_STATUS_PROGRESS, progress);
    }

    /**
     * Zoom to the specified zoom value. 0=fully zoomed out 1=fully zoomed in
     *
     * @param zoom
     */
    public void zoomTo(double zoom) {
        panTo(getCenter(), zoom);
    }
}
