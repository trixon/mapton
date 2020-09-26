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
package org.mapton.editors;

import javafx.scene.Scene;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavPane;
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
        dtd = "-//org.mapton.editors//Editors//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "EditorsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 3)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_EditorsAction",
        preferredID = "EditorsTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.editors.EditorsTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-O"),
    @ActionReference(path = "Menu/Tools", position = 3)
})
@NbBundle.Messages({
    "CTL_EditorsAction=&Editors"
})
public final class EditorsTopComponent extends MTopComponent {

    public EditorsTopComponent() {
        setName(Dict.EDITORS.toString());
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
        return new Scene(new MSplitNavPane<>(MEditor.class, Dict.EDITOR.toString()));
    }
}
