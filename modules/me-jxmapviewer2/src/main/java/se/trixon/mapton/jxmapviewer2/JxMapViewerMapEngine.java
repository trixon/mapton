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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.MathHelper;
import se.trixon.mapton.api.MEngine;
import se.trixon.mapton.api.MLatLon;
import se.trixon.mapton.api.MLatLonBox;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEngine.class)
public class JxMapViewerMapEngine extends MEngine {

    public static final String LOG_TAG = "JxMapViewer2";
    private JXMapViewer mMap;
    private MapKit mMapKit;

    public JxMapViewerMapEngine() {
    }

    @Override
    public void fitToBounds(MLatLonBox latLonBox) {
        Set<GeoPosition> positions = new HashSet<>();
        positions.add(toGeoPosition(latLonBox.getNorthEast()));
        positions.add(toGeoPosition(latLonBox.getSouthWest()));
        mMap.zoomToBestFit(positions, 0.9);
        mMap.setCenterPosition(toGeoPosition(latLonBox.getCenter()));
        System.out.println(ToStringBuilder.reflectionToString(latLonBox, ToStringStyle.MULTI_LINE_STYLE));
        System.out.println(ToStringBuilder.reflectionToString(latLonBox.getCenter(), ToStringStyle.MULTI_LINE_STYLE));
    }

    @Override
    public MLatLon getCenter() {
        return toLatLon(mMapKit.getCenterPosition());
    }

    @Override
    public String getName() {
        return "Open Street Map";
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
    public double getZoom() {
        return toGlobalZoom();
    }

    @Override
    public void onWhatsHere(String s) {
        NbMessage.information("WHATS HERE?", s);
    }

    @Override
    public void panTo(MLatLon latLong) {
        panTo(latLong, mMap.getZoom());
    }

    @Override
    public void panTo(MLatLon latLon, double zoom) {
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
                setStatusMousePositionData(toLatLon(geoPosition), null, null);
            }
        });

        mMapKit.getZoomSlider().addChangeListener((ChangeEvent e) -> {
            log(String.format("GlobalZoom = %f", toGlobalZoom()));
        });
    }

    private void panAndZoomTo(GeoPosition geoPosition, int zoom) {
        mMapKit.setCenterPosition(geoPosition);
//        mMapKit.setAddressLocation(geoPosition);
        mMapKit.setZoom(zoom);
    }

    private GeoPosition toGeoPosition(MLatLon latLon) {
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

    private MLatLon toLatLon(GeoPosition geoPosition) {
        return new MLatLon(
                geoPosition.getLatitude(),
                geoPosition.getLongitude()
        );
    }

    private double toLocalZoom(double globalZoom) {
        final double steps = 17;

        return steps - steps * globalZoom;
    }

}
