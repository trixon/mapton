/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.base.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.mapton.api.LineChartX;
import org.mapton.api.MChartLine;
import org.mapton.api.MKey;
import org.mapton.api.MSelectionLockManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.openide.util.NbBundle;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class ChartView extends BorderPane {

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
        } else if (o instanceof MChartLine) {
            centerObject = new LineChartX((MChartLine) o).getNode();
        } else if (o instanceof Node) {
            centerObject = (Node) o;
        } else {
            centerObject = mInvalidPlaceholderLabel;
        }

        setCenter(centerObject);

        if (!centerObject.getClass().getPackageName().equals("de.gsi.chart")) {
            FxHelper.loadDarkTheme(centerObject.getScene());
        }
    }
}
