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
package org.mapton.butterfly_structural.load;

import java.time.LocalDate;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class LoadListCell extends BListCell<BStructuralLoadCellPoint> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public LoadListCell() {
        createUI();
    }

    @Override
    protected void addContent(BStructuralLoadCellPoint p) {
        setText(null);
        setGraphic(mVBox);
        loadTooltip(p);
        mAlarmIndicator.update(p);

        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        var sign = "⇐";
        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE"), p.getAlarm1Id());
        var dateSB = new StringBuilder(StringHelper.toString(p.getDateLatest() == null ? null : p.getDateLatest().toLocalDate(), "NOVALUE"));
        LocalDate nextDate = null;
        if (nextDate != null) {
            dateSB.append(" (").append(nextDate.toString()).append(")");
            if (nextDate.isBefore(LocalDate.now())) {
                dateSB.append(" ").append(sign);
            }
        }

        var dateRolling = StringHelper.toString(p.getDateRolling(), "NOVALUE");

        var dateZero = StringHelper.toString(p.getDateZero(), "NOVALUE");
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(dateSB.toString());
        mDesc3Label.setText(dateRolling);
        mDesc4Label.setText(dateZero);
    }

    private void createUI() {
        mVBox.getChildren().setAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc4Label
        );

        mHeaderLabel.setGraphic(mAlarmIndicator);
        activateTooltip();
    }

    private class AlarmIndicator extends BAlarmIndicator<BStructuralLoadCellPoint> {

        public AlarmIndicator() {
            addNodes(m1dShape);
        }

        @Override
        public void update(BStructuralLoadCellPoint p) {
            m1dShape.setFill(LoadHelper.getAlarmColorHeightFx(p));
        }
    }
}
