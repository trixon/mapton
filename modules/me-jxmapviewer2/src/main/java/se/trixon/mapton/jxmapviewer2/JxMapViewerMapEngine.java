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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.Node;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.MathHelper;
import se.trixon.mapton.core.api.LatLon;
import se.trixon.mapton.core.api.LatLonBox;
import se.trixon.mapton.core.api.MapEngine;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class JxMapViewerMapEngine extends MapEngine {

    public static final String LOG_TAG = "JxMapViewer2";
    private JXMapViewer mMap;
    private MapKit mMapKit;

    public JxMapViewerMapEngine() {
    }

    @Override
    public void fitToBounds(LatLonBox latLonBox) {
        Set<GeoPosition> positions = new HashSet<>();
        positions.add(toGeoPosition(latLonBox.getNorthEast()));
        positions.add(toGeoPosition(latLonBox.getSouthWest()));
        mMap.zoomToBestFit(positions, 0.9);
        mMap.setCenterPosition(toGeoPosition(latLonBox.getCenter()));
        System.out.println(ToStringBuilder.reflectionToString(latLonBox, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println(ToStringBuilder.reflectionToString(latLonBox.getCenter(), ToStringStyle.MULTI_LINE_STYLE));
    }

    @Override
    public LatLon getCenter() {
        return toLatLon(mMapKit.getCenterPosition());
    }

    @Override
    public String getName() {
        return "Open Street Map (JxMapViewer2)";
    }

    @Override
    public Node getStyleView() {
        return null;
    }

    @Override
    public Object getUI() {
        if (mMapKit == null) {
            init();
            initListeners();
        }

        return mMapKit;
    }

    @Override
    public void onWhatsHere(String s) {
        NbMessage.information("WHATS HERE?", s);
    }

    @Override
    public void panTo(LatLon latLong) {
        panTo(latLong, mMap.getZoom());
    }

    @Override
    public void panTo(LatLon latLon, double zoom) {
        panAndZoomTo(toGeoPosition(latLon), MathHelper.round(toLocalZoom(zoom)));
    }

    private void init() {
        mMapKit = new MapKit();
        mMap = mMapKit.getMainMap();
        setImageRenderer(() -> {
            mMapKit.getZoomSlider().setVisible(false);
            mMapKit.getZoomInButton().setVisible(false);
            mMapKit.getZoomOutButton().setVisible(false);

            BufferedImage image = GraphicsHelper.componentToImage(mMap, null);

            mMapKit.getZoomSlider().setVisible(true);
            mMapKit.getZoomInButton().setVisible(true);
            mMapKit.getZoomOutButton().setVisible(true);

            return image;
        });
        NbLog.v(LOG_TAG, "Loaded and ready");
    }

    private void initListeners() {
        mMap.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    displayContextMenu(e.getLocationOnScreen());
                }
            }
        });

        mMap.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                GeoPosition geoPosition = mMap.convertPointToGeoPosition(e.getPoint());
                setLatLonMouse(toLatLon(geoPosition));
            }
        });

        mMapKit.getZoomSlider().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setZoom(toGlobalZoom());
                System.out.println("Jx");
                System.out.println("zoom: " + mMap.getZoom());
                System.out.println("toGlobal: " + toGlobalZoom());
                System.out.println("toLocal: " + toLocalZoom(toGlobalZoom()));
                System.out.println("");
            }
        });
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

    private double toGlobalZoom() {
        final double steps = 17;
        final int zoom = mMap.getZoom();

        return (steps - zoom) / steps;
    }

    private LatLon toLatLon(GeoPosition geoPosition) {
        return new LatLon(
                geoPosition.getLatitude(),
                geoPosition.getLongitude()
        );
    }

    private double toLocalZoom(double globalZoom) {
        final double steps = 17;

        return steps - steps * globalZoom;
    }

}
