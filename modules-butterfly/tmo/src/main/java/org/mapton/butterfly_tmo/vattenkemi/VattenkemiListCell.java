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
package org.mapton.butterfly_tmo.vattenkemi;

import java.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.tmo.BVattenkemi;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class VattenkemiListCell extends ListCell<BVattenkemi> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public VattenkemiListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BVattenkemi grundvatten, boolean empty) {
        super.updateItem(grundvatten, empty);
        if (grundvatten == null || empty) {
            clearContent();
        } else {
            addContent(grundvatten);
        }
    }

    private void addContent(BVattenkemi grundvatten) {
        setText(null);
        var date = Objects.toString(DateHelper.toDateString(grundvatten.getInstallationsdatum()), "-");
        mNameLabel.setText(grundvatten.getName());
        mDateLabel.setText("%s %s".formatted(date, grundvatten.getComment()));
        mGroupLabel.setText(grundvatten.getGroup());
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
