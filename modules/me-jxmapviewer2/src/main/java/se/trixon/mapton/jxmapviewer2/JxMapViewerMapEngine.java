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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javafx.scene.Node;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.api.LatLon;
import se.trixon.mapton.core.api.MapEngine;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class JxMapViewerMapEngine extends MapEngine {

    public static final String LOG_TAG = "JxMapViewer2";
    private MapKit mMapKit;

    public JxMapViewerMapEngine() {
    }

    @Override
    public LatLon getCenter() {
        return toLatLon(mMapKit.getCenterPosition());
    }

    @Override
    public String getName() {
        return "JxMapViewer2";
    }

    @Override
    public Node getStyleView() {
        return null;
    }

    @Override
    public Object getUI() {
        if (mMapKit == null) {
            init();
        }

        return mMapKit;
    }

    @Override
    public void panTo(LatLon latLong) {
        panTo(latLong, mMapKit.getMainMap().getZoom());
    }

    @Override
    public void panTo(LatLon latLon, int zoom) {
        panAndZoomTo(toGeoPosition(latLon), zoom);
    }

    private void init() {
        mMapKit = new MapKit();
        mMapKit.getMainMap().addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                GeoPosition geoPosition = mMapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
                setLatLonMouse(toLatLon(geoPosition));
            }
        });

        mMapKit.getZoomSlider().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("Jx zoom: " + mMapKit.getMainMap().getZoom());
            }
        });

        NbLog.v(LOG_TAG, "Loaded and ready");
    }

    private void panAndZoomTo(GeoPosition geoPosition, int zoom) {
        mMapKit.setCenterPosition(geoPosition);
//        mMapKit.setAddressLocation(geoPosition);
        mMapKit.setZoom(zoom);
    }

    private GeoPosition toGeoPosition(LatLon latLon) {
        return new GeoPosition(
                latLon.getLatitude(),
                latLon.getLongitude()
        );
    }

    private LatLon toLatLon(GeoPosition geoPosition) {
        return new LatLon(
                geoPosition.getLatitude(),
                geoPosition.getLongitude()
        );
    }

}
