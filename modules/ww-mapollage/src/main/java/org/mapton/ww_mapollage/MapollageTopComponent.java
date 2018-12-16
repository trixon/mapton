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
package org.mapton.ww_mapollage;

import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.ww_mapollage//Mapollage//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapollageTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public final class MapollageTopComponent extends MTopComponent implements MMapMagnet {

    private final Options mOptions = Options.getInstance();
    private BorderPane mRoot;

    public MapollageTopComponent() {
        setName("Mapollage");
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

    private Scene createScene() {
        ResourceBundle bundle = NbBundle.getBundle(MapollageTopComponent.class);

        Label titleLabel = new Label("Mapollage");

        VBox titleBox = new VBox(8, titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        Font defaultFont = Font.getDefault();

        titleLabel.prefWidthProperty().bind(titleBox.widthProperty());
        titleLabel.setBackground(Mapton.getThemeBackground());
        titleLabel.setAlignment(Pos.BASELINE_CENTER);
        titleLabel.setFont(new Font(defaultFont.getSize() * 2));

        mRoot = new BorderPane();
        mRoot.setTop(titleBox);

        return new Scene(mRoot);
    }
}
