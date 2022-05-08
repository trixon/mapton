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
package org.mapton.transformation;

import javafx.scene.Scene;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.transformation//Transformation//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TransformationTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 10)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TransformationAction",
        preferredID = "TransformationTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.transformation.TransformationTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-T"),
    @ActionReference(path = "Menu/Tools", position = 1)
})
@NbBundle.Messages({
    "CTL_TransformationAction=&Transformer"
})
public final class TransformationTopComponent extends MTopComponent {

    public TransformationTopComponent() {
        setName(Actions.cutAmpersand(Bundle.CTL_TransformationAction()));
    }

    @Override
    protected void initFX() {
        setScene(new Scene(new TransformationView()));

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
