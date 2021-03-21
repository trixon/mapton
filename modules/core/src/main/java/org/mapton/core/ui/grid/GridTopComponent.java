/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.core.ui.grid;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MDict;
import org.mapton.api.Mapton;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui//Grid//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "GridTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
public final class GridTopComponent extends MTopComponent {

    private GridView mGridForm;
    private BorderPane mRoot;

    public GridTopComponent() {
        setName(MDict.GRID.toString());
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
        mGridForm = new GridView(null);
        Label titleLabel = Mapton.createTitle(MDict.GRID.toString());
        mRoot = new BorderPane(mGridForm);
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }
}
