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
package se.trixon.mapton.core.map;

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
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.mapton.core.api.FxTopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.map//Map//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "se.trixon.mapton.core.map.MapTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapAction",
        preferredID = "MapTopComponent"
)
@Messages({
    "CTL_MapAction=Map",
    "CTL_MapTopComponent=Map Window",
    "HINT_MapTopComponent=This is a Map window"
})
public final class MapTopComponent extends FxTopComponent {

    private GoogleMapView mMapView;
    private BorderPane mRoot;

    public MapTopComponent() {
        setName(Dict.MAP.toString());
//        setToolTipText(Bundle.HINT_MapTopComponent());
//        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    private Scene createScene() {
        mMapView = new GoogleMapView();
        mMapView.addMapInializedListener(() -> {
            MapOptions mMapOptions;
            LatLong infoWindowLocation = new LatLong(57.66, 12);

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

        mRoot = new BorderPane(mMapView);

        return new Scene(mRoot);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
