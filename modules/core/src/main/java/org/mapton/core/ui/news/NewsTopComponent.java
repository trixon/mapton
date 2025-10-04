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
package org.mapton.core.ui.news;

import javafx.scene.Scene;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Generic Property TopComponent
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui.news//News//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "NewsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 10)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NewsAction",
        preferredID = "NewsTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.core.ui.news.NewsTopComponent")
@ActionReferences({ //    @ActionReference(path = "Shortcuts", name = "D-0"),
//    @ActionReference(path = "Menu/Tools", position = 11)
})
@NbBundle.Messages({
    "CTL_NewsAction=&News"
})
public final class NewsTopComponent extends MTopComponent {

    public NewsTopComponent() {
        setName(Dict.NEWS.toString());
    }

    @Override
    protected void initFX() {
        setScene(new Scene(new NewsView()));
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
