/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.mapjfx;

import com.sothawo.mapjfx.Configuration;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.Extent;
import com.sothawo.mapjfx.MapType;
import com.sothawo.mapjfx.MapView;
import com.sothawo.mapjfx.XYZParam;
import com.sothawo.mapjfx.event.MapLabelEvent;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.sothawo.mapjfx.event.MarkerEvent;
import com.sothawo.mapjfx.offline.OfflineCache;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.swing.JComponent;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MAttribution;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEngine.class)
public class MapJfxMapEngine extends MEngine {

    public static final String LOG_TAG = "OpenLayers";
    private BookmarkPlotter mBookmarkPlotter;
    private File mCacheDir;
    private MapView mMapView;
    private final OfflineCache mOfflineCache = OfflineCache.INSTANCE;

    public MapJfxMapEngine() {
    }

    @Override
    public void fitToBounds(MLatLonBox latLonBox) {
        mMapView.setExtent(Extent.forCoordinates(toCoordinate(latLonBox.getSouthWest()), toCoordinate(latLonBox.getNorthEast())));
    }

    public File getCacheDir() {
        return mCacheDir;
    }

    @Override
    public MLatLon getCenter() {
        return toLatLon(mMapView.getCenter());
    }

    @Override
    public JComponent getMapComponent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getMapNode() {
        if (mMapView == null) {
            init();
        }

        updateToolbarDocumentInfo();

        return mMapView;
    }

    public MapView getMapView() {
        return mMapView;
    }

    @Override
    public String getName() {
        return "OpenLayers";
    }

    @Override
    public Node getStyleView() {
        return null;
    }

    @Override
    public double getZoom() {
        return toGlobalZoom();
    }

    @Override
    public boolean isSwing() {
        return false;
    }

    @Override
    public void onWhatsHere(String s) {
        Mapton.getLog().i(LOG_TAG, s);
    }

    @Override
    public void panTo(MLatLon latLon) {
        mMapView.setCenter(toCoordinate(latLon));
    }

    @Override
    public void panTo(MLatLon latLong, double zoom) {
        mMapView.setZoom(MathHelper.round(toLocalZoom(zoom)));
        panTo(latLong);
    }

    private void init() {
        mCacheDir = new File(Mapton.getCacheDir(), "mapjfx");

        try {
            FileUtils.forceMkdir(mCacheDir);
            mOfflineCache.setCacheDirectory(mCacheDir.toPath());
            Mapton.getLog().v(LOG_TAG, OfflineCache.INSTANCE.getCacheDirectory().toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        mMapView = new MapView();
        mMapView.setOnContextMenuRequested((e) -> {
            displayContextMenu(new Point((int) e.getScreenX(), (int) e.getScreenY()));
        });

        mMapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                new Thread(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(2);
                        Platform.runLater(() -> {
                            initMap();
                        });
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            }
        });

        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(2);
                Platform.runLater(() -> {
                    initListeners();
                    mMapView.initialize(Configuration.builder()
                            .showZoomControls(false)
                            .build()
                    );
                    Mapton.getLog().i(LOG_TAG, "Loaded and ready");
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();

        setImageRenderer(() -> {
            WritableImage image = mMapView.snapshot(new SnapshotParameters(), null);

            return SwingFXUtils.fromFXImage(image, null);
        });
    }

    private void initListeners() {
        // add an event handler for singleclicks, set the click marker to the new position when it's visible
        mMapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            event.consume();
//            labelEvent.setText("Event: map clicked at: " + event.getCoordinate());
//            if (checkDrawPolygon.isSelected()) {
//                handlePolygonClick(event);
//            }
//            if (markerClick.getVisible()) {
//                boolean needToAddMarker = (null == markerClick.getPosition());
//                markerClick.setPosition(event.getCoordinate());
//                if (needToAddMarker) {
//                    // adding can only be done after coordinate is set
//                    mMapView.addMarker(markerClick);
//                }
//            }
        });

        // add an event handler for MapViewEvent#MAP_EXTENT and set the extent in the map
        mMapView.addEventHandler(MapViewEvent.MAP_EXTENT, event -> {
            event.consume();
            mMapView.setExtent(event.getExtent());
        });

        // add an event handler for extent changes and display them in the status label
        mMapView.addEventHandler(MapViewEvent.MAP_BOUNDING_EXTENT, event -> {
            event.consume();
//            System.out.println(event.getExtent().toString());
        });

        mMapView.addEventHandler(MapViewEvent.MAP_RIGHTCLICKED, event -> {
            event.consume();
//            System.out.println("Event: map right clicked at: " + event.getCoordinate());
        });
        mMapView.addEventHandler(MarkerEvent.MARKER_CLICKED, event -> {
            event.consume();
//            System.out.println("Event: marker clicked: " + event.getMarker().getId());
        });
        mMapView.addEventHandler(MarkerEvent.MARKER_RIGHTCLICKED, event -> {
            event.consume();
//            System.out.println("Event: marker right clicked: " + event.getMarker().getId());
        });
        mMapView.addEventHandler(MapLabelEvent.MAPLABEL_CLICKED, event -> {
            event.consume();
//            System.out.println("Event: label clicked: " + event.getMapLabel().getText());
        });
        mMapView.addEventHandler(MapLabelEvent.MAPLABEL_RIGHTCLICKED, event -> {
            event.consume();
//            System.out.println("Event: label right clicked: " + event.getMapLabel().getText());
        });

        mMapView.addEventHandler(MapViewEvent.MAP_POINTER_MOVED, event -> {
            setStatusMousePositionData(toLatLon(event.getCoordinate()), null, null);
        });

//        logger.trace("map handlers initialized");
        mMapView.zoomProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            //aaaMapton.getInstance().zoomProperty().set(toGlobalZoom());
        });
    }

    private void initMap() {
        Mapton.getLog().v(LOG_TAG, "Initializing map...");
        XYZParam xyzParams = new XYZParam()
                .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
                .withAttributions(
                        "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");
        mMapView.setXYZParam(xyzParams);
        mMapView.setMapType(MapType.XYZ);

        mBookmarkPlotter = new BookmarkPlotter(this);

        Mapton.getLog().v(LOG_TAG, "Map initialized");
    }

    private Coordinate toCoordinate(MLatLon latLon) {
        return new Coordinate(
                latLon.getLatitude(),
                latLon.getLongitude()
        );
    }

    private double toGlobalZoom() {
        final double steps = 28;
        final double zoom = mMapView.getZoom();

        return zoom / steps;
    }

    private MLatLon toLatLon(Coordinate coordinate) {
        return new MLatLon(
                coordinate.getLatitude(),
                coordinate.getLongitude()
        );
    }

    private double toLocalZoom(double globalZoom) {
        return globalZoom * 28;
    }

    private void updateToolbarDocumentInfo() {
        LinkedHashMap<String, MAttribution> attributions = new LinkedHashMap<>();

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("OpenStreetMap");
        attribution.setProviderUrl("https://www.openstreetmap.org");
        attribution.setLicenseName("Open Data Commons Open Database License (ODbL)");
        attribution.setLicenseUrl("https://opendatacommons.org/licenses/odbl/");
        attribution.setRawHtml("© OpenStreetMap contributors");
        attributions.put("OSM", attribution);

        MDocumentInfo documentInfo = new MDocumentInfo(getName(), attributions);
        Mapton.getGlobalState().put(MKey.MAP_DOCUMENT_INFO, documentInfo);
    }
}
