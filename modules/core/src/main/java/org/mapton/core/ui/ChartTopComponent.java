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
package org.mapton.core.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.mapton.api.LineChartX;
import org.mapton.api.MChartLine;
import org.mapton.api.MKey;
import org.mapton.api.MSelectionLockManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

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
        putClientProperty("print.name", "Mapton - %s".formatted(Dict.CHART.toString())); // NOI18N
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

    class ChartView extends BorderPane {

        private Label mInvalidPlaceholderLabel;
        private Label mPlaceholderLabel;
        private ProgressBar mProgressBar;

        public ChartView() {
            createUI();
            initListeners();
        }

        private void createUI() {
            mPlaceholderLabel = new Label(NbBundle.getMessage(ChartView.class, "chart_placeholder"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
            mPlaceholderLabel.setDisable(true);

            mInvalidPlaceholderLabel = new Label(NbBundle.getMessage(ChartView.class, "chart_invalid_object"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
            mInvalidPlaceholderLabel.setDisable(true);

            mProgressBar = new ProgressBar(-1);
            mProgressBar.setPrefWidth(400);

            setCenter(mPlaceholderLabel);
        }

        private void initListeners() {
            Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
                Platform.runLater(() -> {
                    refresh(evt.getValue());
                });
            }, MKey.CHART);

            Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
                Platform.runLater(() -> {
                    setCenter(mProgressBar);
                });
            }, MKey.CHART_WAIT);
        }

        private void refresh(Object o) {
            if (MSelectionLockManager.getInstance().isLocked()) {
                return;
            }

            Node centerObject = null;

            if (o == null) {
                centerObject = mPlaceholderLabel;
            } else if (o instanceof MChartLine chartLine) {
                centerObject = new LineChartX(chartLine).getNode();
            } else if (o instanceof Node node) {
                centerObject = node;
            } else {
                centerObject = mInvalidPlaceholderLabel;
            }

            setCenter(centerObject);

            if (!centerObject.getClass().getPackageName().equals("de.gsi.chart")) {
                FxHelper.loadDarkTheme(centerObject.getScene());
            }
        }
    }
}
