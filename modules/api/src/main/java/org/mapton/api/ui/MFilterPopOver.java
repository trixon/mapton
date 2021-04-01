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
package org.mapton.api.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MFilterPopOver extends MPopOver {

    private final Button allButton = new Button(Dict.SHOW_ALL.toString());
    private final Button clearButton = new Button(Dict.CLEAR.toString());
    private final HBox mButtonBox;

    public MFilterPopOver() {
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

        getAction().setGraphic(MaterialIcon._Content.FILTER_LIST.getImageView(getIconSizeToolBarInt()));
    }

    public abstract void clear();

    public HBox getButtonBox() {
        return mButtonBox;
    }

    public abstract void reset();

}
