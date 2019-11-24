/*
 * Copyright 2019 Patrik Karlström.
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
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core_nb.bookmark//Ruler//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "RulerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true, position = 99)
@ActionID(category = "Window", id = "org.mapton.core_nb.bookmark.RulerTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_RulerAction",
        preferredID = "RulerTopComponent"
)
@Messages({
    "CTL_RulerAction=Measure"
})
public final class RulerTopComponent extends MTopComponent {

    private RulerView mRulerView;

    public RulerTopComponent() {
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        setName(Dict.MEASURE.toString());
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    private Scene createScene() {
        mRulerView = new RulerView();

        return new Scene(mRulerView);
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }
}
