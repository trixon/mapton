/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.poi_nb;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MDict;
import org.mapton.base.ui.poi.PoisView;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
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
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@ActionID(category = "Mapton", id = "org.mapton.poi_nb.PoiTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "POI",
        preferredID = "PoiTopComponent"
)
public final class PoiTopComponent extends MTopComponent implements MMapMagnet {

    private BorderPane mRoot;

    public PoiTopComponent() {
        setName(MDict.POI.toString());
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
        mRoot = new BorderPane(new PoisView());

        return new Scene(mRoot);
    }
}
