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
package org.mapton.butterfly_geo_extensometer;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;

/**
 *
 * @author Patrik Karlström
 */
class ExtensoListCell extends ListCell<BGeoExtensometer> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mStationLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public ExtensoListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BGeoExtensometer extenso, boolean empty) {
        super.updateItem(extenso, empty);
        if (extenso == null || empty) {
            clearContent();
        } else {
            addContent(extenso);
        }
    }

    private void addContent(BGeoExtensometer extenso) {
        setText(null);
        mNameLabel.setText(extenso.getName());
        mStationLabel.setText(extenso.getSensors());
//        var firstRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawFirstDate()), "");
//        var lastRaw = Objects.toString(DateHelper.toDateString(mon.getControlPoint().ext().getObservationRawLastDate()), "");
//        mDateLabel.setText("%s — %s".formatted(firstRaw, lastRaw));
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
