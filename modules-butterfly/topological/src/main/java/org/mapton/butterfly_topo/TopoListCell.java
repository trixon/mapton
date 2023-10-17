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
    protected void updateItem(BTopoControlPoint p, boolean empty) {
        super.updateItem(p, empty);
        if (p == null || empty) {
            clearContent();
        } else {
            addContent(p);
        }
    }

    private void addContent(BTopoControlPoint p) {
        setText(null);
        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        var alarms = StringHelper.getJoinedUnique(", ",
                StringUtils.removeEndIgnoreCase(p.getNameOfAlarmHeight(), "_h"),
                StringUtils.removeEndIgnoreCase(p.getNameOfAlarmPlane(), "_p")
        );

        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE"), alarms);

        var dateLatest = StringHelper.toString(p.getDateLatest() == null ? null : p.getDateLatest().toLocalDate(), "NOVALUE");
        var desc2 = dateLatest + " TODO: geometry";

        var dateRolling = StringHelper.toString(p.getDateRolling(), "NOVALUE");
        String deltaRolling = p.ext().deltaRolling().getDelta(3);
        var desc3 = "%s: %s".formatted(dateRolling, deltaRolling);

        var dateZero = StringHelper.toString(p.getDateZero(), "NOVALUE");
        String deltaZero = p.ext().deltaZero().getDelta(3);
        var desc4 = "%s: %s".formatted(dateZero, deltaZero);

        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(desc2);
        mDesc3Label.setText(desc3);
        mDesc4Label.setText(desc4);

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

        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
