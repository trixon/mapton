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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RulerView extends BorderPane {

    private Button mClearButton;
    private ComboBox<String> mModeComboBox;
    private ToggleSwitch mToggleSwitch;

    public RulerView() {
        createUI();
    }

    private void createUI() {
        mModeComboBox = new ComboBox<>();
        mModeComboBox.getItems().setAll(
                Dict.LINE.toString(),
                Dict.PATH_GFX.toString(),
                Dict.POLYGON.toString(),
                Dict.CIRCLE.toString()
        );

        mToggleSwitch = new ToggleSwitch(Dict.ACTIVE.toString());
        mToggleSwitch.setSelected(true);
        mToggleSwitch.setAlignment(Pos.BASELINE_RIGHT);

        setTop(mModeComboBox);

        VBox vBox = new VBox(8);
        setCenter(vBox);
        mModeComboBox.prefWidthProperty().bind(widthProperty());
        mModeComboBox.getSelectionModel().select(0);

        mClearButton = new Button(Dict.CLEAR.toString());
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);

        HBox hBox = new HBox(mClearButton, spring, mToggleSwitch);
        hBox.setPadding(new Insets(0, 8, 4, 8));
        setBottom(hBox);

        getTop().disableProperty().bind(mToggleSwitch.selectedProperty().not());
        getCenter().disableProperty().bind(mToggleSwitch.selectedProperty().not());
        mClearButton.disableProperty().bind(mToggleSwitch.selectedProperty().not());
    }
}
