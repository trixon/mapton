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
package se.trixon.mapton.core.testing;

import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.shapes.Circle;
import java.awt.Dimension;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.mapton.core.api.MaptonTopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.testing//Fx1//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "Fx1TopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "se.trixon.mapton.core.testing.Fx1TopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_Fx1Action",
        preferredID = "Fx1TopComponent"
)
@Messages({
    "CTL_Fx1Action=Fx1",
    "CTL_Fx1TopComponent=Fx1 Window",
    "HINT_Fx1TopComponent=This is a Fx1 window"
})
public final class Fx1TopComponent extends MaptonTopComponent {

    public Fx1TopComponent() {
        setName(Bundle.CTL_Fx1TopComponent());
        setToolTipText(Bundle.HINT_Fx1TopComponent());

//        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
//        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.FALSE);
//        putClientProperty(TopComponent.PROP_, Boolean.TRUE);
    }

    private Scene createScene() {
        Button button = new Button("hello");
        button.setOnAction((ActionEvent event) -> {
//            //MapOptions aa = mMapton.getMapOptions().mapType(MapTypeIdEnum.SATELLITE).zoom(1);
            GoogleMap mMap = getMap();
            LatLong infoWindowLocation = new LatLong(57.67, 12);
//
            Circle circle = new Circle();
            circle.setCenter(infoWindowLocation);
            circle.setRadius(200);
            mMap.addMapShape(circle);
////            mMapton.getMapOptions().center(infoWindowLocation);
//            mMap.panTo(infoWindowLocation);
//            mMap.setZoom(15);
//            MarkerOptions markerOptions = new MarkerOptions();
//            markerOptions.position(infoWindowLocation);
//
//            Marker marker = new Marker(markerOptions);
//            mMap.addMarker(marker);
//
//            InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
//            infoWindowOptions.content("<h2>Header</h2>"
//                    + "Content row #1<br>"
//                    + "Content row #2");
//
//            InfoWindow infoWindow = new InfoWindow(infoWindowOptions);
//            infoWindow.open(mMap, marker);
        });
        BorderPane root = new BorderPane(button);
        final SwingNode swingNode = new SwingNode();
//        createSwingContent(swingNode);
        root.setBottom(swingNode);

        return new Scene(root);
    }

    private void createSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            JButton jButton = new JButton("Click me!");
            // jButton.setBounds(0,0,80,50);

            jButton.setPreferredSize(new Dimension(80, 50));

            JPanel panel = new JPanel();
            // panel.setLayout(null);
            panel.add(jButton);

            swingNode.setContent(panel);
//
//            javax.swing.Action quickSearchAction = Actions.forID("Edit", "org.netbeans.modules.quicksearch.QuickSearchAction");
//            CallableSystemAction d = (CallableSystemAction) quickSearchAction;
//            swingNode.setContent(d.getMenuPresenter());

        });
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }
}
