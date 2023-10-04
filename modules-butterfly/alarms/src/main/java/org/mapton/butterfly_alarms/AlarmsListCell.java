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
package org.mapton.butterfly_alarms;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_format.types.BAlarm;

/**
 *
 * @author Patrik Karlström
 */
class AlarmsListCell extends ListCell<BAlarm> {

    private final Label mDesc1Label = new Label();
    private final Label mNameLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public AlarmsListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BAlarm alarm, boolean empty) {
        super.updateItem(alarm, empty);
        if (alarm == null || empty) {
            clearContent();
        } else {
            addContent(alarm);
        }
    }

    private void addContent(BAlarm alarm) {
        setText(null);
        mNameLabel.setText(alarm.getName());
        mDesc1Label.setText("%s: %s".formatted(alarm.getDescription(), alarm.getId()));
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
