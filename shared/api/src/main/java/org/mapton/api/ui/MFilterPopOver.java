/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.api.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MFilterPopOver extends PopOver {

    public static final int GAP = FxHelper.getUIScaled(8);
    public static final int WIDTH = FxHelper.getUIScaled(96);
    private final Button allButton = new Button(Dict.SHOW_ALL.toString());
    private final Button clearButton = new Button(Dict.CLEAR.toString());
    private final HBox mButtonBox;

    public static void autoSize(VBox vBox) {
        for (Node node : vBox.getChildren()) {
            VBox.setVgrow(node, Priority.ALWAYS);

            if (node instanceof Control) {
                Control c = (Control) node;
                c.setPrefWidth(2 * WIDTH + GAP);
            }
        }
        vBox.setPadding(FxHelper.getUIScaledInsets(GAP));
    }

    public MFilterPopOver() {
        setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        setHeaderAlwaysVisible(true);
        setCloseButtonEnabled(false);
        setDetachable(true);
        setAnimated(true);

        setTitle(Dict.FILTER.toString());
        allButton.setOnAction((event) -> {
            reset();
        });
        clearButton.setOnAction((event) -> {
            clear();
        });

        allButton.setPrefWidth(WIDTH);
        clearButton.setPrefWidth(WIDTH);
        mButtonBox = new HBox(GAP, allButton, clearButton);
        mButtonBox.setAlignment(Pos.CENTER);
    }

    public abstract void clear();

    public HBox getButtonBox() {
        return mButtonBox;
    }

    public abstract void reset();

}
