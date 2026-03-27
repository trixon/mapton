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
package org.mapton.butterfly_acoustic.vibration;

import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;

/**
 *
 * @author Patrik Karlström
 */
class VibrationListCell extends BListCell<BAcousticVibrationPoint> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();

    public VibrationListCell() {
        createUI();
    }

    @Override
    protected void addContent(BAcousticVibrationPoint p) {
        setText(null);
        setGraphic(mVBox);
        loadTooltip(p);
        mAlarmIndicator.update(p);
        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        var desc1 = "%s: %s".formatted(StringUtils.defaultIfBlank(p.getGroup(), "NOVALUE"), StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE"));
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(Strings.CI.replace(p.ext().getDateLatestAsString(), "T", " "));
        mDesc3Label.setText(Strings.CI.replace(p.ext().getDateFirstAsString(), "T", " "));
        mDesc4Label.setText(p.getComment());

        setGraphic(mVBox);
    }

    private void createUI() {
        mHeaderLabel.setStyle(mStyleBold);
        mVBox.getChildren().addAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );

        mHeaderLabel.setGraphic(mAlarmIndicator);
//        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
        activateTooltip();
    }

    private class AlarmIndicator extends BAlarmIndicator<BAcousticVibrationPoint> {

        public AlarmIndicator() {
            addNodes(m1dShape);
        }

        @Override
        public void update(BAcousticVibrationPoint p) {
            m1dShape.setFill(VibrationHelper.getAlarmColorFx(p));
        }
    }

}
