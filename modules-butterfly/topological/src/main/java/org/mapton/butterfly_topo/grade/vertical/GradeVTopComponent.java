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
package org.mapton.butterfly_topo.grade.vertical;

import java.util.ResourceBundle;
import javafx.scene.Scene;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.butterfly-topo//GradeV//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "GradeVTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@ActionID(category = "Butterfly", id = "org.mapton.butterfly-topo.GradeVTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/MapTools/Butterfly", position = 11)
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_GradeVAction",
        preferredID = "GradeVTopComponent"
)
@Messages({
    "CTL_GradeVAction=Control points, vertical grade"
})
public final class GradeVTopComponent extends MTopComponent {

    private final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);

    public GradeVTopComponent() {
        setName(mBundle.getString("grade_v"));
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
        var gradeView = new GradeVView();

        return new Scene(gradeView.getView());
    }
}
