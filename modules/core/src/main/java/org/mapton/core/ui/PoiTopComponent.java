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
package org.mapton.core.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MDict;
import org.mapton.core.ui.poi.PoisViewManager;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.poi//Poi//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "PoiTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "topLeft", openAtStartup = false, position = 2)
@TopComponent.OpenActionRegistration(
        displayName = "POI",
        preferredID = "PoiTopComponent"
)
public final class PoiTopComponent extends MTopComponent {

    private BorderPane mBorderPane;

    public PoiTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        setName(MDict.POI.toString());
        setPopOverHolder(true);
    }

    @Override
    protected void fxComponentOpened() {
        super.fxComponentOpened();
        if (mBorderPane != null) {
            mBorderPane.setCenter(PoisViewManager.getInstance().getPoisView());
        }
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    private Scene createScene() {
        mBorderPane = new BorderPane(PoisViewManager.getInstance().getPoisView());

        return new Scene(mBorderPane);
    }
}
