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
package org.mapton.butterfly_tmo.grundvatten;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;

/**
 *
 * @author Patrik Karlström
 */
class GrundvattenListCell extends ListCell<BGrundvatten> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mMagasinLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public GrundvattenListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BGrundvatten grundvatten, boolean empty) {
        super.updateItem(grundvatten, empty);
        if (grundvatten == null || empty) {
            clearContent();
        } else {
            addContent(grundvatten);
        }
    }

    private void addContent(BGrundvatten grundvatten) {
        setText(null);
        var date = "-";
        try {
            date = GrundvattenHelper.getLevelAndDate(grundvatten.ext().getObservationRawLast());
        } catch (Exception e) {
            //nvm
        }

        mNameLabel.setText("%s [%s]".formatted(grundvatten.getName(), grundvatten.getStatus()));
        mMagasinLabel.setText(grundvatten.getGrundvattenmagasin());
        mDateLabel.setText(date);
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
                mMagasinLabel,
                mDateLabel
        );
    }

}
