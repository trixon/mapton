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
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.shapes.Circle;
import javafx.scene.layout.Pane;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.core.api.MapEngineProvider;
import se.trixon.mapton.core.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngineProvider.class)
public class GMapsFXMapEngineProvider extends MapEngineProvider {

    private MapOptions mMapOptions;
    private GoogleMapView mMapView;

    public GMapsFXMapEngineProvider() {
    }

    private void init() {
        mMapView = new GoogleMapView();
        mMapView.addMapInitializedListener(() -> {
            LatLong infoWindowLocation = new LatLong(Mapton.MYLAT, Mapton.MYLON);

            mMapOptions = new MapOptions()
                    .center(infoWindowLocation)
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .rotateControl(true)
                    .streetViewControl(false)
                    .zoom(15);

            GoogleMap map = mMapView.createMap(mMapOptions);

            Circle circle = new Circle();
            circle.setCenter(infoWindowLocation);
            circle.setRadius(200);
            map.addMapShape(circle);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(infoWindowLocation);

            Marker marker = new Marker(markerOptions);
            map.addMarker(marker);

            InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
            infoWindowOptions.content("<h2>Header</h2>"
                    + "Content row #1<br>"
                    + "Content row #2");

            InfoWindow infoWindow = new InfoWindow(infoWindowOptions);
            infoWindow.open(map, marker);
        });
    }

    @Override
    public String getName() {
        return "GMapsFX";
    }

    @Override
    public Pane getUI() {
        if (mMapView == null) {
            init();
        }

        return mMapView;
    }

}
