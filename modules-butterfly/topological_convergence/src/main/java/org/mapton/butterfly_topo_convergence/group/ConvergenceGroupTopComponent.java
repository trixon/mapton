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
package org.mapton.butterfly_topo_convergence.group;

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
        dtd = "-//org.mapton.butterfly-topo-convergence//ConvergenceGroup//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ConvergenceGroupTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@ActionID(category = "Butterfly", id = "org.mapton.butterfly-topo-convergence.ConvergenceGroupTopComponent")
@ActionReferences({
    //    @ActionReference(path = "Shortcuts", name = "DO-E"),
    @ActionReference(path = "Menu/MapTools/Butterfly", position = 1)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ConvergenceGroupAction",
        preferredID = "ConvergenceGroupTopComponent"
)
@Messages({
    "CTL_ConvergenceGroupAction=🕱 Convergence groups"
})
public final class ConvergenceGroupTopComponent extends MTopComponent {

    public ConvergenceGroupTopComponent() {
        setName(Bundle.CTL_ConvergenceGroupAction());
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    private Scene createScene() {
        var convergenceView = new ConvergenceGroupView();

        return new Scene(convergenceView.getView());
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
