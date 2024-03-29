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

import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MDict;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private final Tab mBackgroundTab = new Tab(Dict.BACKGROUND.toString());
    private final Tab mDataTab = new Tab(Dict.OBJECT.toString());
    private final Tab mOptionsTab = new Tab(Dict.OPTIONS.toString());
    private final Tab mOverlayTab = new Tab(MDict.OVERLAY.toString());
    private final TabPane mTabPane = new TabPane();
    private final Preferences mPreferences = MOptions.getInstance().getPreferences().node("layerView");

    public static LayerView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerView() {
        createUI();

        MOptions.getInstance().engineProperty().addListener((p, o, n) -> {
            loadLayerView();
        });

    }

    private void createUI() {
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.setSide(Side.LEFT);
        mTabPane.getTabs().addAll(mDataTab,
                mOverlayTab,
                mBackgroundTab,
                mOptionsTab
        );

        setCenter(mTabPane);

        setPrefWidth(FxHelper.getUIScaled(300));
        loadLayerView();
    }

    private void loadLayerView() {
        Platform.runLater(() -> {
            mDataTab.setContent(Mapton.getEngine().getLayerObjectView());
            mDataTab.setDisable(mDataTab.getContent() == null);

            mOverlayTab.setContent(Mapton.getEngine().getLayerOverlayView());
            mOverlayTab.setDisable(mOverlayTab.getContent() == null);

            mBackgroundTab.setContent(Mapton.getEngine().getLayerBackgroundView());
            mBackgroundTab.setDisable(mBackgroundTab.getContent() == null);

            var layerOptionsView = Mapton.getEngine().getLayerOptionsView();
            var optionsScrollPane = new ScrollPane(layerOptionsView);
            optionsScrollPane.setFitToWidth(true);
            mOptionsTab.setContent(optionsScrollPane);
            mOptionsTab.setDisable(layerOptionsView == null);

            int activeTab = mPreferences.getInt("activeTab", 0);
            mTabPane.getSelectionModel().select(activeTab);
            mTabPane.getSelectionModel().selectedIndexProperty().addListener((p, o, n) -> {
                mPreferences.putInt("activeTab", n.intValue());
            });
        });
    }

    private static class Holder {

        private static final LayerView INSTANCE = new LayerView();
    }
}
