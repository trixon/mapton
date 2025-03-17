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
package org.mapton.butterfly_topo_convergence.group;

import java.time.LocalDate;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class ConvergenceGroupListCell extends ListCell<BTopoConvergenceGroup> {

    private final Label mDesc1Label = new Label();

    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final String mStyleMono = "-fx-font-family: monospace;";
    private final Tooltip mTooltip = new Tooltip();
    private VBox mVBox;

    public ConvergenceGroupListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BTopoConvergenceGroup group, boolean empty) {
        super.updateItem(group, empty);
        if (group == null || empty) {
            clearContent();
        } else {
            addContent(group);
        }
    }

    private void addContent(BTopoConvergenceGroup g) {
//        setText(null);
//        var header = group.getName();
//        if (StringUtils.isNotBlank(group.getStatus())) {
//            header = "%s [%s]".formatted(header, group.getStatus());
//        }
//
//        mNameLabel.setText(header);
//        try {
//            var maxD = group.ext2().getMaxDeltaDistanceOverTime();
//            var maxL = group.ext2().getMaxDeltaROverTime();
//            var maxH = group.ext2().getMaxDeltaZOverTime();
//
//            mDesc2Label.setText("ΔL=%.3f (%s)".formatted(maxD.getDeltaDistanceOverTime(), maxD.ext().getShortName()));
//            mDesc3Label.setText("ΔP=%.3f (%s)".formatted(maxL.getDeltaROverTime(), maxL.ext().getShortName()));
//            mDesc4Label.setText("ΔH=%.3f (%s)".formatted(maxH.getDeltaZOverTime(), maxH.ext().getShortName()));
//
//        } catch (Exception e) {
//        }
//        setGraphic(mVBox);
        setText(null);
        var header = g.getName();
        if (StringUtils.isNotBlank(g.getStatus())) {
            header = "%s [%s]".formatted(header, g.getStatus());
        }

        var alarms = StringHelper.getJoinedUnique(", ",
                StringUtils.removeEndIgnoreCase(g.getAlarm1Id(), "_h"),
                StringUtils.removeEndIgnoreCase(g.getAlarm2Id(), "_p")
        );
        var sign = "⇐";
        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(g.getCategory(), "NOVALUE"), alarms);
        var dateSB = new StringBuilder(StringHelper.toString(g.getDateLatest() == null ? null : g.getDateLatest().toLocalDate(), "NOVALUE"));
        var nextDate = g.ext().getObservationRawNextDate();
        if (nextDate != null) {
            dateSB.append(" (").append(nextDate.toString()).append(")");
            if (nextDate.isBefore(LocalDate.now())) {
                dateSB.append(" ").append(sign);
            }
        }

        var dateRolling = StringHelper.toString(g.getDateRolling(), "NOVALUE");
        var desc3 = "%s: %s".formatted(dateRolling, g.ext2().getNumOfObservations());

        var dateZero = StringHelper.toString(g.getDateZero(), "NOVALUE");
        var desc4 = "%s: %d/%d".formatted(dateZero, g.ext().getNumOfObservationsFiltered(), g.ext().getNumOfObservations());

//        mAlarmIndicator.update(g);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(dateSB.toString());
        mDesc3Label.setText(desc3);
        mDesc4Label.setText(desc4);

        mHeaderLabel.setTooltip(new Tooltip("Add custom tooltip: " + g.getName()));
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
//        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
