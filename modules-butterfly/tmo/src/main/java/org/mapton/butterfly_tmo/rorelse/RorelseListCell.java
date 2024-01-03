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
package org.mapton.butterfly_tmo.rorelse;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.tmo.BRorelse;

/**
 *
 * @author Patrik Karlström
 */
class RorelseListCell extends ListCell<BRorelse> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public RorelseListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BRorelse rorelse, boolean empty) {
        super.updateItem(rorelse, empty);
        if (rorelse == null || empty) {
            clearContent();
        } else {
            addContent(rorelse);
        }
    }

    private void addContent(BRorelse rorelse) {
        setText(null);
        mNameLabel.setText("%s [%s]".formatted(rorelse.getName(), rorelse.getStatus()));
        mDateLabel.setText(rorelse.getPlacering());
        mGroupLabel.setText(rorelse.getLägesbeskrivning());
        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox = new VBox(
                mNameLabel,
                mDateLabel,
                mGroupLabel
        );
    }

}
