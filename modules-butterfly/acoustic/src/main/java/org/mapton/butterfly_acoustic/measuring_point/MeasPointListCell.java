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
package org.mapton.butterfly_acoustic.measuring_point;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;

/**
 *
 * @author Patrik Karlström
 */
class MeasPointListCell extends ListCell<BAcousticVibrationPoint> {

    private final Label mNameLabel = new Label();
    private final Label mSoilLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;
    private final Label mWorkLabel = new Label();

    public MeasPointListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BAcousticVibrationPoint point, boolean empty) {
        super.updateItem(point, empty);
        if (point == null || empty) {
            clearContent();
        } else {
            addContent(point);
        }
    }

    private void addContent(BAcousticVibrationPoint point) {
        setText(null);
        mNameLabel.setText(point.getName());
        mWorkLabel.setText(point.getCategory());
        mSoilLabel.setText(point.getSoilMaterial());
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
                mWorkLabel,
                mSoilLabel
        );
    }

}
