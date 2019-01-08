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
package org.mapton.ww_mapollage;

import java.io.IOException;
import java.util.ResourceBundle;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.mapollage.OperationListener;
import se.trixon.mapollage.ProfileManager;
import se.trixon.mapollage.ui.MainApp;

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
    private MainApp mMainApp;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();

    public MapollageTopComponent() {
        setName("Mapollage");
        try {
            mProfileManager.load();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

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

        MainApp.setEmbedded(true);
        mMainApp = new MainApp();
        mMainApp.initEmbedded();
        mMainApp.setOperationListener(new OperationListener() {
            @Override
            public void onOperationError(String message) {
            }

            @Override
            public void onOperationFailed(String message) {
            }

            @Override
            public void onOperationFinished(String message, int placemarkCount) {
            }

            @Override
            public void onOperationInterrupted() {
            }

            @Override
            public void onOperationLog(String message) {
            }

            @Override
            public void onOperationProcessingStarted() {
            }

            @Override
            public void onOperationProgress(String message) {
            }

            @Override
            public void onOperationProgress(int value, int max) {
            }

            @Override
            public void onOperationStarted() {
            }
        });

        Label titleLabel = createTitle("Mapollage");
        mRoot = new BorderPane(mMainApp.getRoot());
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }
}
