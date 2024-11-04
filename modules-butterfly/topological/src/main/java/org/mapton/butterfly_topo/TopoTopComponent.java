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
package org.mapton.butterfly_topo;

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
        dtd = "-//org.mapton.butterfly-topo//Topo//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TopoTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@ActionID(category = "Butterfly", id = "org.mapton.butterfly-topo.TopoTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DO-T"),
    @ActionReference(path = "Menu/MapTools/Butterfly", position = 0)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ControlPointAction",
        preferredID = "TopoTopComponent"
)
@Messages({
    "CTL_ControlPointAction=Control point"
})
public final class TopoTopComponent extends MTopComponent {

    public TopoTopComponent() {
        setName(Bundle.CTL_ControlPointAction());
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    private Scene createScene() {
        var topoView = new TopoView();

        return new Scene(topoView.getView());
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
