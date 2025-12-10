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
package org.mapton.butterfly_remote.insar;

import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class InsarListCell extends BListCell<BRemoteInsarPoint> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final InsarAttributeManager mAttributeManager = InsarAttributeManager.getInstance();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public InsarListCell() {
        createUI();
    }

    @Override
    protected void addContent(BRemoteInsarPoint p) {
        setText(null);
        setGraphic(mVBox);
        loadTooltip(p);
        mAlarmIndicator.update(p);

        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        var descGrpCat = "%s: %s".formatted(p.getGroup(), p.getCategory());
        var dateZero = StringHelper.toString(p.getDateZero(), "NOVALUE");
        var dateZeroAndValue = "%s: %s".formatted(dateZero, p.ext().deltaZero().getDelta1(1, 1000, true));
        mHeaderLabel.setText(header);
        mDesc1Label.setText(descGrpCat);
        mDesc2Label.setText(mAttributeManager.getValueByColorByWithHeader(p));
        mDesc3Label.setText(getDateLatestAndNext(p));
        mDesc4Label.setText(dateZeroAndValue);
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

    private class AlarmIndicator extends BAlarmIndicator<BRemoteInsarPoint> {

        public AlarmIndicator() {
            addNodes(m1dShape);
        }

        @Override
        public void update(BRemoteInsarPoint p) {
            m1dShape.setFill(mAttributeManager.getColorFx(p));
        }

    }
}
