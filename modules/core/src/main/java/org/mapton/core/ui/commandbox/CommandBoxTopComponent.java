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
package org.mapton.core.ui.commandbox;

import javafx.scene.Scene;
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
        dtd = "-//org.mapton.core.ui.commandbox//CommandBox//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CommandBoxTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "topLeft", openAtStartup = false, position = 3)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CommandBoxAction",
        preferredID = "CommandBoxTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.core.ui.commandbox.CommandBoxTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-A"),
    @ActionReference(path = "Menu/Tools", position = 9)
})
@NbBundle.Messages({
    "CTL_CommandBoxAction=Comm&ands"
})
public final class CommandBoxTopComponent extends MTopComponent {

    public CommandBoxTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_SLIDING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        setName(Dict.COMMANDS.toString());
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
        return new Scene(new CommandBoxView());
    }
}
