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
package org.mapton.core.ui;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class RulerView extends BorderPane {

    public RulerView() {
        createUI();

        MOptions.getInstance().engineProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            loadRulerView();
        });

    }

    private void createUI() {
        loadRulerView();
    }

    private void loadRulerView() {
        Platform.runLater(() -> {
            setCenter(Mapton.getEngine().getRulerView());
        });
    }
}
