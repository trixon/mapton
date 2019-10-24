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
package org.mapton.workbench.modules.map;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mapton.api.Mapton;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RulerStage extends Stage {

    private final Scene mScene;

    public RulerStage() {
        initStyle(StageStyle.UTILITY);
        setTitle(Dict.RULER.toString());
        setAlwaysOnTop(true);
        mScene = new Scene(new Pane());
        setScene(mScene);
        updateScene();

        Mapton.optionsGeneral().engineProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            updateScene();
        });

    }

    private void updateScene() {
        Node rulerView = Mapton.getEngine().getRulerView();
        if (rulerView != null) {
            mScene.setRoot((Parent) rulerView);
        } else {
            mScene.setRoot(new Label());
        }
    }
}
