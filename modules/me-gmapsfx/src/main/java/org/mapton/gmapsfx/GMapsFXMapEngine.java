/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.gmapsfx;

import com.dlsc.gmapsfx.GoogleMapView;
import com.dlsc.gmapsfx.javascript.event.GMapMouseEvent;
import com.dlsc.gmapsfx.javascript.event.UIEventType;
import com.dlsc.gmapsfx.javascript.object.GoogleMap;
import com.dlsc.gmapsfx.javascript.object.InfoWindow;
import com.dlsc.gmapsfx.javascript.object.LatLong;
import com.dlsc.gmapsfx.javascript.object.LatLongBounds;
import com.dlsc.gmapsfx.javascript.object.MapOptions;
import com.dlsc.gmapsfx.javascript.object.MapTypeIdEnum;
import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.swing.JComponent;
import org.mapton.api.MAttribution;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;
import static org.mapton.gmapsfx.ModuleOptions.*;
import org.mapton.gmapsfx.api.MapStyle;
import org.mapton.google_maps.api.GoogleMaps;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEngine.class)
public class GMapsFXMapEngine extends MEngine {

    public static final String LOG_TAG = "GMapsFX";

    private BookmarkPlotter mBookmarkPlotter;
    private InfoWindow mInfoWindow;
    private GoogleMap mMap;
    private MapOptions mMapOptions;
    private GoogleMapView mMapView;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private StyleView mStyleView;

    public GMapsFXMapEngine() {
        mStyleView = new StyleView();
    }

    @Override
    public void create(Runnable postCreateRunnable) {
        FxHelper.runLater(() -> {
            if (mMapView == null) {
                init();
                postCreateRunnable.run();
            } else {
                postCreateRunnable.run();
            }
        });
    }

    @Override
    public void fitToBounds(MLatLonBox latLonBox) {
        mMap.fitBounds(new LatLongBounds(toLatLong(latLonBox.getSouthWest()), toLatLong(latLonBox.getNorthEast())));
    }

    @Override
    public MLatLon getCenter() {
        return toLatLon(mMap.getCenter());
    }

    public GoogleMap getMap() {
        return mMap;
    }

    @Override
    public JComponent getMapComponent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Node getMapNode() {
        updateToolbarDocumentInfo();

        return mMapView;
    }

    @Override
    public String getName() {
        return "Google Maps";
    }

    @Override
    public Node getStyleView() {
        return mStyleView;
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
        mInfoWindow.setContent(s);
        mInfoWindow.setPosition(toLatLong(getLatLonMouse()));
        mInfoWindow.open(mMap);
    }

    @Override
    public void panTo(MLatLon latLon) {
        mMap.panTo(toLatLong(latLon));
    }

    @Override
    public void panTo(MLatLon latLong, double zoom) {
        mMap.setZoom(MathHelper.round(toLocalZoom(zoom)));
        panTo(latLong);
    }

    private void init() {
        mMapView = new GoogleMapView(Locale.getDefault().getLanguage(), GoogleMaps.getKey());

        mMapView.addMapInitializedListener(() -> {
            mInfoWindow = new InfoWindow();
            mMapOptions = new MapOptions()
                    .zoom(5)
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .rotateControl(true)
                    .clickableIcons(false)
                    .streetViewControl(false)
                    .mapTypeControl(false)
                    .fullscreenControl(false)
                    .scaleControl(true)
                    .styleString(MapStyle.getStyle(mOptions.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE)))
                    .zoomControl(false);

            initMap();
            initialized();
            initListeners();

            setImageRenderer(() -> {
                WritableImage image = mMapView.snapshot(new SnapshotParameters(), null);

                return SwingFXUtils.fromFXImage(image, null);
            });

            mBookmarkPlotter = new BookmarkPlotter(this);
            Mapton.getLog().i(LOG_TAG, "Loaded and ready");
        });
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case ModuleOptions.KEY_MAP_STYLE:
                        final MLatLon old = getCenter();
                        initMap();
                        FxHelper.runLaterDelayed(100, () -> {
                            panTo(old);
                        });
                        break;

                    case ModuleOptions.KEY_MAP_TYPE:
                        mMap.setMapType(mOptions.getMapType());
                        updateToolbarDocumentInfo();
                        break;

                    default:
                }
            });
        });

        mMap.zoomProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            Mapton.getInstance().zoomProperty().set(toGlobalZoom());
        });

        mMapView.setOnContextMenuRequested((e) -> {
            displayContextMenu(new Point((int) e.getScreenX(), (int) e.getScreenY()));
        });
    }

    private void initMap() {
        Mapton.getLog().v(LOG_TAG, "Initializing map...");

        mMapOptions.styleString(MapStyle.getStyle(mOptions.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE)));
        mMap = mMapView.createMap(mMapOptions);
        mMap.setMapType(mOptions.getMapType());

        mMap.addMouseEventHandler(UIEventType.mousemove, (GMapMouseEvent event) -> {
            setStatusMousePositionData(toLatLon(event.getLatLong()), null, null);
        });

        Mapton.getLog().v(LOG_TAG, "Map initialized");
    }

    private double toGlobalZoom() {
        final double steps = 22;
        final int zoom = mMap.getZoom();

        return zoom / (steps - 0);
    }

    private MLatLon toLatLon(LatLong latLong) {
        return new MLatLon(
                latLong.getLatitude(),
                latLong.getLongitude()
        );
    }

    private LatLong toLatLong(MLatLon latLon) {
        return new LatLong(
                latLon.getLatitude(),
                latLon.getLongitude()
        );
    }

    private double toLocalZoom(double globalZoom) {
        return globalZoom * 22;
    }

    private void updateToolbarDocumentInfo() {
        String name = "";
        switch (mOptions.getMapType().toString()) {
            case "ROADMAP":
                name = Dict.MAP_TYPE_ROADMAP.toString();
                break;

            case "SATELLITE":
                name = Dict.MAP_TYPE_SATELLITE.toString();
                break;

            case "HYBRID":
                name = Dict.MAP_TYPE_HYBRID.toString();
                break;

            case "TERRAIN":
                name = Dict.MAP_TYPE_TERRAIN.toString();
                break;
        }

        LinkedHashMap<String, MAttribution> attributions = new LinkedHashMap<>();

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Google");
        attribution.setProviderUrl("https://www.google.com/maps");
        attribution.setLicenseName("GOOGLE TERMS OF SERVICE");
        attribution.setLicenseUrl("https://www.google.com/help/terms_maps/");
        attributions.put("com.google.maps", attribution);

        MDocumentInfo documentInfo = new MDocumentInfo(name, attributions);
        Mapton.getGlobalState().put(MKey.MAP_DOCUMENT_INFO, documentInfo);
    }
}
