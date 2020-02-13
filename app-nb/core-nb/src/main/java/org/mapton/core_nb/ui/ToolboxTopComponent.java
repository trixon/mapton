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
package org.mapton.core_nb.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core_nb.ui//Toolbox//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ToolboxTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false, position = 1)
public final class ToolboxTopComponent extends MTopComponent implements MMapMagnet {

    private BorderPane mRoot;

    public ToolboxTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        setName(Dict.TOOLBOX.toString());
        setPopOverHolder(true);
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        mRoot.setCenter(new MapToolboxView());
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
        return new Scene(mRoot = new BorderPane());
    }
}
