/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.ui;

import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckListView;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private EngineBox mEngineBox;
    private CheckListView<String> mListView;
    private final MOptions mMOptions = MOptions.getInstance();

    public LayerView() {
        createUI();
        mMOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_ENGINE:
                    loadLayerView();
                    break;

                default:
                    break;
            }
        });

    }

    private void createUI() {
        mListView = new CheckListView<>();
        mEngineBox = new EngineBox();
        mEngineBox.backgroundProperty().bind(mListView.backgroundProperty());

        setCenter(mListView);
        setBottom(mEngineBox);

        loadLayerView();
    }

    private void loadLayerView() {
        Platform.runLater(() -> {
            setCenter(Mapton.getEngine().getLayerView());
        });
    }
}
