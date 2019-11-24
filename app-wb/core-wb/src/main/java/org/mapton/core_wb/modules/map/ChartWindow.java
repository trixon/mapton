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
package org.mapton.core_wb.modules.map;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MChartLine;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.windowsystemfx.Window;
import se.trixon.windowsystemfx.WindowSystemComponent;

@WindowSystemComponent.Description(
        iconBase = "",
        preferredId = "org.mapton.core_wb.modules.map.ChartWindow",
        parentId = "chart",
        position = 1
)
@ServiceProvider(service = Window.class)
public final class ChartWindow extends Window {

    private Label mInvalidPlaceholderLabel;
    private BorderPane mNode;
    private Label mPlaceholderLabel;
    private ProgressBar mProgressBar;

    public ChartWindow() {
        setName(Dict.CHART.toString());
    }

    @Override
    public Node getNode() {
        if ((mNode == null)) {
            createUI();
            initListeners();
        }

        return mNode;
    }

    private void createUI() {
        mPlaceholderLabel = new Label(NbBundle.getMessage(ChartWindow.class, "chart_placeholder"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
        mPlaceholderLabel.setDisable(true);

        mInvalidPlaceholderLabel = new Label(NbBundle.getMessage(ChartWindow.class, "chart_invalid_object"), MaterialIcon._Editor.SHOW_CHART.getImageView(getIconSizeToolBar()));
        mInvalidPlaceholderLabel.setDisable(true);

        mProgressBar = new ProgressBar(-1);
        mProgressBar.setPrefWidth(400);
        mNode = new BorderPane(mPlaceholderLabel);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                refresh(evt.getValue());
            });
        }, MKey.CHART);

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                mNode.setCenter(mProgressBar);
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

        mNode.setCenter(centerObject);
    }
}
