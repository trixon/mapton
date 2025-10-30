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
package org.mapton.butterfly_structural.strain;

import java.time.LocalDate;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class StrainListCell extends BListCell<BStructuralStrainGaugePoint> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public StrainListCell() {
        createUI();
    }

    @Override
    protected void addContent(BStructuralStrainGaugePoint p) {
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
//        var nextDate = p.ext().getObservationRawNextDate();
        LocalDate nextDate = null;
        if (nextDate != null) {
            dateSB.append(" (").append(nextDate.toString()).append(")");
            if (nextDate.isBefore(LocalDate.now())) {
                dateSB.append(" ").append(sign);
            }
        }

        var dateRolling = StringHelper.toString(p.getDateRolling(), "NOVALUE");
        var desc3 = "%s: %s".formatted(dateRolling, p.ext().getDeltaRolling());

        var dateZero = StringHelper.toString(p.getDateZero(), "NOVALUE");
        var desc4 = "%s: %s".formatted(dateZero, p.ext().getDeltaZero());
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(dateSB.toString());
        mDesc3Label.setText(desc3);
        mDesc4Label.setText(desc4);
    }

    private void createUI() {
        mVBox.getChildren().setAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );

        mHeaderLabel.setGraphic(mAlarmIndicator);
        activateTooltip();
    }

    private class AlarmIndicator extends BAlarmIndicator<BStructuralStrainGaugePoint> {

        public AlarmIndicator() {
            addNodes(m1dShape);
        }

        @Override
        public void update(BStructuralStrainGaugePoint p) {
            m1dShape.setFill(StrainHelper.getAlarmColorHeightFx(p));
        }
    }
}
