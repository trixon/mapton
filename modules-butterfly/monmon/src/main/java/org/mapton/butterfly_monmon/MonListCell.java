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
package org.mapton.butterfly_monmon;

import java.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class MonListCell extends ListCell<BMonmon> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mStationLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public MonListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BMonmon mon, boolean empty) {
        super.updateItem(mon, empty);
        if (mon == null || empty) {
            clearContent();
        } else {
            addContent(mon);
        }
    }

    private void addContent(BMonmon mon) {
        setText(null);
        mNameLabel.setText(mon.getName());
        mStationLabel.setText(mon.getStationName());
        var firstRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawFirstDate()), "");
        var lastRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawLastDate()), "");
        mDateLabel.setText("%s — %s".formatted(firstRaw, lastRaw));
        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox = new VBox(mNameLabel, mStationLabel, mDateLabel);
    }

}
