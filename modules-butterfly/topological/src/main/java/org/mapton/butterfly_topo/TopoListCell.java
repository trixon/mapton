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
package org.mapton.butterfly_topo;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class TopoListCell extends ListCell<BTopoControlPoint> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;
    private final Tooltip mTooltip = new Tooltip();

    public TopoListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BTopoControlPoint tcp, boolean empty) {
        super.updateItem(tcp, empty);
        if (tcp == null || empty) {
            clearContent();
        } else {
            addContent(tcp);
        }
    }

    private void addContent(BTopoControlPoint o) {
        setText(null);
        var header = o.getName();
        if (StringUtils.isNotBlank(o.getStatus())) {
            header = "%s [%s]".formatted(header, o.getStatus());
        }

        var alarms = StringHelper.getJoinedUnique(", ",
                StringUtils.removeEndIgnoreCase(o.getNameOfAlarmHeight(), "_h"),
                StringUtils.removeEndIgnoreCase(o.getNameOfAlarmPlane(), "_p")
        );

        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(o.getCategory(), "NOVALUE"), alarms);

        var dateLatest = StringHelper.toString(o.getDateLatest() == null ? null : o.getDateLatest().toLocalDate(), "NOVALUE");
        var desc2 = dateLatest + " TODO: geometry";

        var dateRolling = StringHelper.toString(o.getDateRolling(), "NOVALUE");
        var desc3 = dateRolling + " TODO: Δ123 R";

        var dateZero = StringHelper.toString(o.getDateZero(), "NOVALUE");
        var desc4 = dateZero + " TODO: Δ123 0";

        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(desc2);
        mDesc3Label.setText(desc3);
        mDesc4Label.setText(desc4);

        mHeaderLabel.setTooltip(new Tooltip("Add custom tooltip: " + o.getName()));
        mTooltip.setText("asdfwaf");
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

        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
