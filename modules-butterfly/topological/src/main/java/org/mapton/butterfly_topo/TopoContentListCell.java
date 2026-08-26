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

import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class TopoContentListCell extends BContentListCell<BTopoControlPoint> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public TopoContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BTopoControlPoint p) {
        setText(null);
        setGraphic(mVBox);
        loadTooltip(p);
        mAlarmIndicator.update(p);

        var alarms = StringHelper.getJoinedUnique(", ",
                Strings.CI.removeEnd(p.getAlarm1Id(), "_h"),
                Strings.CI.removeEnd(p.getAlarm2Id(), "_p")
        );
        var desc1 = "%s/%s".formatted(
                StringUtils.defaultIfBlank(p.getClassification(), "NOVALUE"),
                StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE")
        );

        var dateZero = StringHelper.toString(p.getDateZero(), "NOVALUE");
        String deltaZero = p.ext().deltaZero().getDelta1d2d(3);
        var desc4 = "%s: %s".formatted(dateZero, deltaZero);

        mHeaderLabel.setText(getHeader(p));
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(alarms);
        mDesc3Label.setText(getDateLatestAndNext(p));
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

    private class AlarmIndicator extends BAlarmIndicator<BTopoControlPoint> {

        public AlarmIndicator() {
            addNodes(m1dShape, m2dShape);
        }

        @Override
        public void update(BTopoControlPoint p) {
            m1dShape.setFill(TopoHelper.getAlarmColorHeightFx(p));
            m1dShape.setVisible(p.getDimension() != BDimension._2d);
            m2dShape.setFill(TopoHelper.getAlarmColorPlaneFx(p));
            m2dShape.setVisible(p.getDimension() != BDimension._1d);
        }
    }
}
