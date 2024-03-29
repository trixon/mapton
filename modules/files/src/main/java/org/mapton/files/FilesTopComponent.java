/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.files;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.Mapton;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.files//Files//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "FilesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_FilesAction",
        preferredID = "FilesTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.files.FilesAction")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "D-U"),
    @ActionReference(path = "Menu/MapTools", position = 1250)
})
@NbBundle.Messages({
    "CTL_FilesAction=Files"
})
public final class FilesTopComponent extends MTopComponent {

    private BorderPane mRoot;

    public FilesTopComponent() {
        setName(Dict.FILES.toString());
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
        var titleLabel = Mapton.createTitle(Dict.FILES.toString());
        mRoot = new BorderPane(new FilesPane());
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }
}
