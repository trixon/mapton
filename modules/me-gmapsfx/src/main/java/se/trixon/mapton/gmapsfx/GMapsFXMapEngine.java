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
package se.trixon.mapton.gmapsfx;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.MapStateEventType;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.api.MapEngine;
import se.trixon.mapton.core.api.MapStyleProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class GMapsFXMapEngine extends MapEngine {

    public static final String LOG_TAG = "GMapsFX";

    private InfoWindow mInfoWindow;
    private GoogleMap mMap;
    private MapOptions mMapOptions;
    private GoogleMapView mMapView;
    private Slider mZoomSlider;

    public GMapsFXMapEngine() {
    }

    @Override
    public String getName() {
        return "GMapsFX";
    }

    @Override
    public Node getUI() {
        if (mMapView == null) {
            init();
        }

        return mMapView;
    }

    @Override
    public boolean isSwing() {
        return false;
    }

    private void init() {
        mMapView = new GoogleMapView(Locale.getDefault().getLanguage(), mOptions.getMapKey());

        mMapView.addMapInitializedListener(() -> {
            mInfoWindow = new InfoWindow();
            mMapOptions = new MapOptions()
                    .center(mOptions.getMapCenter())
                    .zoom(mOptions.getMapZoom())
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .rotateControl(true)
                    .clickableIcons(false)
                    .streetViewControl(false)
                    .mapTypeControl(false)
                    .fullscreenControl(false)
                    .scaleControl(true)
                    .styleString(MapStyleProvider.getStyle(mOptions.getMapStyle()))
                    .zoomControl(false);

            mZoomSlider = new Slider(0, 22, 1);
            mZoomSlider.setPadding(new Insets(8, 0, 0, 8));
            mZoomSlider.setBlockIncrement(1);
            mMapView.getChildren().add(mZoomSlider);
//            Mapton.getAppToolBar().setDisable(false);

            initMap();

            Platform.runLater(() -> {
                mMap.setZoom(mOptions.getMapZoom());
                mMap.setCenter(mOptions.getMapCenter());
            });

//            mStatusBar = AppStatusPanel.getInstance().getProvider();
//            if (mOptions.isMapOnly()) {
//                mRoot.setBottom(mStatusBar);
//            }
//
//            initListeners();
            NbLog.v(LOG_TAG, "Loaded and ready");
        });
    }

    private void initMap() {
        NbLog.v(LOG_TAG, "Initializing map...");

        mMapOptions.styleString(MapStyleProvider.getStyle(mOptions.getMapStyle()));
        if (mMap != null) {
            mMapOptions
                    .center(mMap.getCenter())
                    .zoom(mMap.getZoom());
        }

        mMap = mMapView.createMap(mMapOptions);
        mMap.setMapType(mOptions.getMapType());
        mMap.zoomProperty().bindBidirectional(mZoomSlider.valueProperty());

        mMap.addStateEventHandler(MapStateEventType.zoom_changed, () -> {
//            mMapController.setZoom(mMap.getZoom());
        });

        mMap.addMouseEventHandler(UIEventType.mousemove, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
//            mMapController.setLatLong(latLong);
//            AppStatusPanel.getInstance().getProvider().updateLatLong();
        });

        NbLog.v(LOG_TAG, "Map initialized");
    }

}
