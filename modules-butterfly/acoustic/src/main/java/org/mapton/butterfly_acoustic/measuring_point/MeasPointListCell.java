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

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class MeasPointListCell extends ListCell<BAcousticVibrationPoint> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final Tooltip mTooltip = new Tooltip();
    private VBox mVBox;

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

    private void addContent(BAcousticVibrationPoint p) {
        setText(null);
        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(p.getGroup(), "NOVALUE"), StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE"));
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(StringUtils.replace(p.ext().getDateLatestAsString(), "T", " "));
        mDesc3Label.setText(StringUtils.replace(p.ext().getDateFirstAsString(), "T", " "));
        mDesc4Label.setText(p.getComment());

        mHeaderLabel.setTooltip(new Tooltip("Add custom tooltip: " + p.getName()));
        mTooltip.setText("TODO");
        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mHeaderLabel.setStyle(mStyleBold);
        mVBox = new VBox(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );

//        mHeaderLabel.setGraphic(mAlarmIndicator);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));

        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
