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
package org.mapton.core_nb.ui.datasources;

import javafx.scene.Scene;
import org.mapton.core_nb.api.MTopComponent;
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
        dtd = "-//org.mapton.datasources//DataSources//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DataSourcesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 4)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DataSourcesAction",
        preferredID = "DataSourcesTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.datasources.DataSourcesTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-D"),
    @ActionReference(path = "Menu/Window", position = 4)
})
@NbBundle.Messages({
    "CTL_DataSourcesAction=&Data sources"
})
public final class DataSourcesTopComponent extends MTopComponent {

    private DataSourcesPane mDataSourcesPane;

    public DataSourcesTopComponent() {
        setName(Dict.DATA_SOURCES.toString());
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
        mDataSourcesPane.save();
    }

    private Scene createScene() {
        mDataSourcesPane = new DataSourcesPane();

        return new Scene(mDataSourcesPane);
    }

}
