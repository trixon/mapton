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
package org.mapton.butterfly_activities;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.BAreaActivity;

/**
 *
 * @author Patrik Karlström
 */
class ActListCell extends ListCell<BAreaActivity> {

    private final Label mDesc1Label = new Label();
    private final Label mNameLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public ActListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BAreaActivity aa, boolean empty) {
        super.updateItem(aa, empty);
        if (aa == null || empty) {
            clearContent();
        } else {
            addContent(aa);
        }
    }

    private void addContent(BAreaActivity aa) {
        setText(null);
        mNameLabel.setText(aa.getName());
        mDesc1Label.setText(aa.getDescription());
        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox = new VBox(mNameLabel, mDesc1Label);
    }

}
