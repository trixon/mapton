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
package org.mapton.core_nb.ui.chart;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MChartLine;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Generic Property TopComponent
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core_nb.ui.chart//Chart//EN",
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

    private Label mInvalidPlaceholderLabel;
    private Label mPlaceholderLabel;
    private BorderPane mRoot;
    private ProgressBar mProgressBar;

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
        mPlaceholderLabel = new Label(getBundleString("placeholder"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
        mPlaceholderLabel.setDisable(true);

        mInvalidPlaceholderLabel = new Label(getBundleString("invalid_object"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
        mInvalidPlaceholderLabel.setDisable(true);

        mProgressBar = new ProgressBar(-1);
        mProgressBar.setPrefWidth(400);
        mRoot = new BorderPane(mPlaceholderLabel);

        return new Scene(mRoot);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                refresh(evt.getValue());
            });
        }, MKey.CHART);

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                mRoot.setCenter(mProgressBar);
            });
        }, MKey.CHART_WAIT);
    }

    private void refresh(Object o) {
        Node centerObject = null;

        if (o == null) {
            centerObject = mPlaceholderLabel;
        } else if (o instanceof MChartLine) {
            centerObject = new LineChartX((MChartLine) o).getNode();
        } else if (o instanceof Node) {
            centerObject = (Node) o;
        } else {
            centerObject = mInvalidPlaceholderLabel;
        }

        mRoot.setCenter(centerObject);
    }
}
