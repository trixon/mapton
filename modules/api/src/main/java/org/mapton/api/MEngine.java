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
package org.mapton.api;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javax.swing.JComponent;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MEngine {

    public static final String KEY_STATUS_COORDINATE = "Status.Coordinate";
    public static final String KEY_STATUS_PROGRESS = "Status.Progress";

    protected static final Logger LOGGER = Logger.getLogger(MEngine.class.getName());
    private static final TreeMap<String, MEngine> ENGINES = new TreeMap<>();
    private static final HashSet<MEngineListener> sEngineListeners = new HashSet<>();

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
        Lookup.getDefault().lookupResult(MEngine.class).addLookupListener(lookupEvent -> {
            populateEngines();
        });

        populateEngines();
    }

    public static boolean addEngineListener(MEngineListener engineListener) {
        return sEngineListeners.add(engineListener);
    }

    public static MEngine byName(String name) {
        return ENGINES.getOrDefault(name, null);
    }

    public static void clearEngineListener(MEngineListener engineListener) {
        sEngineListeners.clear();
    }

    public static boolean removeEngineListener(MEngineListener engineListener) {
        return sEngineListeners.remove(engineListener);
    }

    private static void populateEngines() {
        ENGINES.clear();
        Lookup.getDefault().lookupAll(MEngine.class).forEach(engine -> {
            ENGINES.put(engine.getName(), engine);
        });
    }

    public MEngine() {
    }

    public abstract void create(Runnable postCreateRunnable);

    public void displayContextMenu(Point screenXY) {
        mLockedLatitude = mLatitude;
        mLockedLongitude = mLongitude;

        Runnable r = () -> {
            for (var engineListener : sEngineListeners) {
                try {
                    engineListener.displayContextMenu(screenXY);
                } catch (Exception e) {
                    Mapton.getLog().e(getClass().getSimpleName(), e.getMessage());
                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(() -> {
                r.run();
            });
        }
    }

    public void fitToBounds(MLatLonBox latLonBox) {
        Mapton.getLog().i(getClass().getSimpleName(), "fitToBounds not implemented");
    }

    public void fitToBounds(Geometry geometry) {
        Mapton.getLog().i(getClass().getSimpleName(), "fitToBounds not implemented");
    }

    public void fitToBounds(ArrayList<Coordinate> coordinates) {
        Mapton.getLog().i(getClass().getSimpleName(), "fitToBounds not implemented");
    }

    public Double getAltitude() {
        return mAltitude;
    }

    public MLatLon getCenter() {
        Mapton.getLog().i(getClass().getSimpleName(), "getCenter not implemented");
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
        return options().getMapCooTrans().getLatitude(mLatitude, mLongitude);
    }

    public Node getLayerBackgroundView() {
        Mapton.getLog().i(getClass().getSimpleName(), "getLayerBackgroundView not implemented");

        return null;
    }

    public Node getLayerObjectView() {
        Mapton.getLog().i(getClass().getSimpleName(), "getLayerObjectView not implemented");

        return null;
    }

    public Node getLayerOptionsView() {
        Mapton.getLog().i(getClass().getSimpleName(), "getLayerOptionsView not implemented");

        return null;
    }

    public Node getLayerOverlayView() {
        Mapton.getLog().i(getClass().getSimpleName(), "getLayerOverlayView not implemented");

        return null;
    }

    public MLatLon getLockedLatLon() {
        return new MLatLon(mLockedLatitude, mLockedLongitude);
    }

    public Double getLockedLatitude() {
        return mLockedLatitude;
    }

    public double getLockedLatitudeProj() {
        return options().getMapCooTrans().getLatitude(mLockedLatitude, mLockedLongitude);
    }

    public Double getLockedLongitude() {
        return mLockedLongitude;
    }

    public double getLockedLongitudeProj() {
        return options().getMapCooTrans().getLongitude(mLockedLatitude, mLockedLongitude);
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public double getLongitudeProj() {
        return options().getMapCooTrans().getLongitude(mLatitude, mLongitude);
    }

    public abstract JComponent getMapComponent();

    public abstract Node getMapNode();

    public abstract String getName();

    public Node getRulerView() {
        Mapton.getLog().i(getClass().getSimpleName(), "getRulerView not implemented");

        return null;
    }

    public MStatusZoomMode getStatusZoomMode() {
        return MStatusZoomMode.ABSOLUTE;
    }

    public abstract Node getStyleView();

    public Double getViewAltitude() {
        return .0;
    }

    public Double getViewHeading() {
        return .0;
    }

    public Double getViewPitch() {
        return .0;
    }

    public Double getViewRoll() {
        return .0;
    }

    public double getZoom() {
        Mapton.getLog().i(getClass().getSimpleName(), "getZoom not implemented");
        return 0.2;
    }

    public final void goHome() {
        panTo(options().getMapHome(), options().getMapHomeZoom());
    }

    public void hideContextMenu() {
        Runnable r = () -> {
            for (var engineListener : sEngineListeners) {
                try {
                    engineListener.hideContextMenu();
                } catch (Exception e) {
                    Mapton.getLog().e(getClass().getSimpleName(), e.getMessage());
                }
            }
        };

        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(() -> {
                r.run();
            });
        }
    }

    public abstract void initEngine();

    public final void initialized() {
        mInitialized = true;
        panTo(options().getMapCenter(), options().getMapZoom());
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public boolean isSwing() {
        return true;
    }

    public void log(String message) {
        Mapton.getLog().v(getClass().getSimpleName(), message);
    }

    public void onActivate() {
    }

    public void onClosing() {
    }

    public void onDeactivate() {
    }

    public void onOpening() {
    }

    public void onStyleSwap() {
        Mapton.getLog().i(getClass().getSimpleName(), "style swap not implemented");
    }

    public void onWhatsHere(String s) {
        Mapton.getLog().i(getClass().getSimpleName(), "displayWhatsHere not implemented");
    }

    public void panTo(MLatLon latLon) {
        Mapton.getLog().i(getClass().getSimpleName(), "panTo not implemented");
    }

    public void panTo(MLatLon latLon, double zoom) {
        Mapton.getLog().i(getClass().getSimpleName(), "panTo(Zoom) not implemented");
    }

    public void panTo(double zoom, MLatLon latLon) {
        Mapton.getLog().i(getClass().getSimpleName(), "panTo(Zoom) not implemented");
    }

    /**
     * Used to re-parent SwingNodes
     */
    public void refreshUI() {
    }

    public void setImageRenderer(Callable<BufferedImage> imageRenderer) {
        mImageRenderer = imageRenderer;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLockedLatitude(Double lockedLatitude) {
        mLockedLatitude = lockedLatitude;
    }

    public void setLockedLongitude(Double lockedLongitude) {
        mLockedLongitude = lockedLongitude;
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

    public void setViewAltitude(Double viewAltitude) {
    }

    public void setViewHeading(Double viewHeading) {
    }

    public void setViewPitch(Double viewPitch) {
    }

    public void setViewRoll(Double viewRoll) {
    }

    public void swapStyle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Zoom to the specified zoom value. 0=fully zoomed out 1=fully zoomed in
     *
     * @param zoom
     */
    public void zoomTo(double zoom) {
        panTo(getCenter(), zoom);
    }

    protected MOptions options() {
        return MOptions.getInstance();
    }
}
