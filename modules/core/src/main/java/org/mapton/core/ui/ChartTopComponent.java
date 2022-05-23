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
package org.mapton.core.ui;

import javafx.scene.Scene;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.base.ui.ChartView;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Generic Property TopComponent
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui//Chart//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ChartTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapBottom", openAtStartup = false)
public final class ChartTopComponent extends MTopComponent {

    public ChartTopComponent() {
        setName(Dict.CHART.toString());
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.name", String.format("Mapton - %s", Dict.CHART.toString())); // NOI18N
    }

    @Override
    protected void initFX() {
        setScene(new Scene(new ChartView()));
        Mapton.getGlobalState().put(MKey.CHART, Mapton.getGlobalState().get(MKey.CHART));
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
