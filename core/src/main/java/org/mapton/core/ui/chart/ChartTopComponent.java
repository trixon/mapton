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
package org.mapton.core.ui.chart;

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MChart;
import org.mapton.api.MKey;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Generic Property TopComponent
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui.chart//Chart//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ChartTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ChartAction",
        preferredID = "ChartTopComponent"
)
public final class ChartTopComponent extends MTopComponent implements MMapMagnet {

    private ResourceBundle mBundle;
    private BorderPane mRoot;

    public ChartTopComponent() {
        setName(Dict.CHART.toString());
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initListeners();
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
        mBundle = NbBundle.getBundle(ChartTopComponent.class);

        Label label = new Label(mBundle.getString("placeholder"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
        label.setDisable(true);

        mRoot = new BorderPane(label);

        return new Scene(mRoot);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                refresh(evt.getValue());
            });
        }, MKey.CHART);
    }

    private void refresh(MChart chart) {
        System.out.println(ToStringBuilder.reflectionToString(chart, ToStringStyle.JSON_STYLE));
    }

}
