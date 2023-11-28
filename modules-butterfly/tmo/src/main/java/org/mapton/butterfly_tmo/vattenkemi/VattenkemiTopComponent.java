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
package org.mapton.butterfly_tmo.vattenkemi;

import javafx.scene.Scene;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.butterfly-tmo//Vattenkemi//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "VattenkemiTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@ActionID(category = "Butterfly", id = "org.mapton.butterfly-tmo.vattenkemi.VattenkemiTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DO-V"),
    @ActionReference(path = "Menu/MapTools/Butterfly/TMO", position = 3)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_VattenkemiAction",
        preferredID = "VattenkemiTopComponent"
)
@Messages({
    "CTL_VattenkemiAction=Vattenkemi"
})
public final class VattenkemiTopComponent extends MTopComponent {

    public VattenkemiTopComponent() {
        setName(Bundle.CTL_VattenkemiAction());
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
        var vattenkemiView = new VattenkemiView();

        return new Scene(vattenkemiView.getView());
    }
}
