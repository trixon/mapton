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
package org.mapton.butterfly_acoustic.blast;

import java.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class BlastListCell extends ListCell<BAcousticBlast> {

    private final Label mDateLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final Label mNameLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public BlastListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BAcousticBlast blast, boolean empty) {
        super.updateItem(blast, empty);
        if (blast == null || empty) {
            clearContent();
        } else {
            addContent(blast);
        }
    }

    private void addContent(BAcousticBlast blast) {
        setText(null);
        var date = Objects.toString(DateHelper.toDateTimeString(blast.getDateLatest()), "-");
        mNameLabel.setText(blast.getName());
        mDateLabel.setText("%s %s".formatted(date, blast.getComment()));
        mGroupLabel.setText(blast.getGroup());
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
