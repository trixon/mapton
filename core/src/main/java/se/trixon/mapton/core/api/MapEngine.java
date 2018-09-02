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

import java.awt.Point;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.scene.Node;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.AppStatusPanel;
import se.trixon.mapton.core.ui.MapTopComponent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapEngine {

    private static final TreeMap<String, MapEngine> ENGINES = new TreeMap<>();
    private static double mZoom;

    protected final MaptonOptions mMaptonOptions = MaptonOptions.getInstance();

    private LatLon mLatLonMouse;
    private double mLatitude;
    private double mLongitude;

    static {
        Lookup.getDefault().lookupResult(MapEngine.class).addLookupListener((LookupEvent ev) -> {
            populateEngines();
        });

        populateEngines();
    }

    public static MapEngine byName(String name) {
        return ENGINES.getOrDefault(name, null);
    }

    public static double getZoom() {
        System.out.println("getGlobalZoom: " + mZoom);
        return mZoom;
    }

    public static void setZoom(double zoom) {
        mZoom = zoom;
        System.out.println("setGlobalZoom: " + mZoom);
    }

    private static void populateEngines() {
        ENGINES.clear();
        Lookup.getDefault().lookupAll(MapEngine.class).forEach((engine) -> {
            ENGINES.put(engine.getName(), engine);
        });
    }

    public MapEngine() {
    }

    public void displayContextMenu(Point screenXY) {
        SwingUtilities.invokeLater(() -> {
            MapTopComponent tc = (MapTopComponent) WindowManager.getDefault().findTopComponent("MapTopComponent");
            tc.displayContextMenu(screenXY);
        });
    }

    public LatLon getCenter() {
        NbLog.i(getClass().getSimpleName(), "getCenter not implemented");
        return new LatLon(0, 0);
    }

//    public abstract MapController getController();
//    public void fitBounds(GeocoderGeometry geometry) {
////        try {
////            getMap().fitBounds(geometry.getBounds());
////        } catch (netscape.javascript.JSException e) {
////            panTo(geometry.getLocation());
////        }
//    }
    public LatLon getLatLonMouse() {
        return mLatLonMouse;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLatitudeProj() {
        return mMaptonOptions.getMapCooTrans().getLatitude(mLatitude, mLongitude);
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLongitudeProj() {
        return mMaptonOptions.getMapCooTrans().getLongitude(mLatitude, mLongitude);
    }

    public abstract String getName();

    public abstract Node getStyleView();

    public abstract Object getUI();

    public final void goHome() {
        System.out.println("homeZoom=" + mMaptonOptions.getMapHomeZoom());
        panTo(mMaptonOptions.getMapHome(), mMaptonOptions.getMapHomeZoom());
    }

    public final void initialized() {
        panTo(mMaptonOptions.getMapCenter(), mMaptonOptions.getMapZoom());
    }

    public boolean isSwing() {
        return true;
    }

    public void panTo(LatLon latLon) {
        NbLog.i(getClass().getSimpleName(), "panTo not implemented");
    }

    public void panTo(LatLon latLon, double zoom) {
        NbLog.i(getClass().getSimpleName(), "panTo(Zoom) not implemented");
    }

    public final void setLatLonMouse(LatLon latLonMouse) {
        mLatLonMouse = latLonMouse;
        mLatitude = latLonMouse.getLatitude();
        mLongitude = latLonMouse.getLongitude();

        Platform.runLater(() -> {
            AppStatusPanel.getInstance().getProvider().updateLatLong();
        });
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

}
