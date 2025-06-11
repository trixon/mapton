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

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class ExtensoListCell extends ListCell<BGeoExtensometer> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final Tooltip mTooltip = new Tooltip();
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

    private void addContent(BGeoExtensometer ext) {
        setText(null);
        var header = ext.getName();
        if (StringUtils.isNotBlank(ext.getStatus())) {
            header = "%s [%s]".formatted(header, ext.getStatus());
        }

        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(ext.getCategory(), "NOVALUE"), ext.getAlarm1Id());
        var date = ext.getDateLatest();
        var dateLatest = StringHelper.toString(date == null ? null : date.toLocalDate(), "NOVALUE");

        var dateZero = StringHelper.toString(ext.getDateZero(), "NOVALUE");
        var list = ext.getPoints().stream().map(point -> "%.1f".formatted(point.ext().getDelta())).toList();

        String desc4 = String.join(" / ", list);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(dateLatest);
        mDesc3Label.setText(dateZero);
        mDesc4Label.setText(desc4);

//        mHeaderLabel.setTooltip(new Tooltip("Add custom tooltip: " + p.getName()));
//        mTooltip.setText("TODO");
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

        //mHeaderLabel.setGraphic(mAlarmIndicator);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));

        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
