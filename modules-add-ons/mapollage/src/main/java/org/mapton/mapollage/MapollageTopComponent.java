/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.mapollage;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoSourceManager;
import org.mapton.mapollage.ui.Tabs;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

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
        Label titleLabel = Mapton.createTitleDev("Mapollage");
        Tabs tabs = new Tabs();
        BorderPane innerBorderPane = new BorderPane(tabs);
        Button refreshButton = new Button(Dict.REFRESH.toString());
        refreshButton.prefWidthProperty().bind(innerBorderPane.widthProperty());
        refreshButton.setOnAction((event) -> {
            MapoSourceManager.getInstance().load();
            Mapton.getGlobalState().put(Mapo.KEY_MAPO, tabs.getMapo());
        });

        innerBorderPane.setTop(refreshButton);

        mRoot = new BorderPane(innerBorderPane);
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }
}
