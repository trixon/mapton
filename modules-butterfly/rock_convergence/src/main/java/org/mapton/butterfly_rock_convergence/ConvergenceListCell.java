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
package org.mapton.butterfly_rock_convergence;

import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceObservation;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
class ConvergenceListCell extends BListCell<BTopoConvergenceGroup> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public ConvergenceListCell() {
        createUI();
    }

    @Override
    protected void addContent(BTopoConvergenceGroup g) {
        setText(null);
        setGraphic(mVBox);
        loadTooltip(g);
        mAlarmIndicator.update(g);

        var header = g.getName();
        if (StringUtils.isNotBlank(g.getStatus())) {
            header = "%s [%s]".formatted(header, g.getStatus());
        }
        var dateLast = StringHelper.toString(g.getDateLatest() == null ? null : g.getDateLatest().toLocalDate(), "NOVALUE");
        var dateZero = StringHelper.toString(g.getDateZero(), "NOVALUE");
        var date = "%s — %s  (%d)".formatted(dateZero, dateLast, g.ext().getPairs().size());

        mHeaderLabel.setText(header);
        mDesc1Label.setText(g.ext().getDeltaString("1d", BTopoConvergenceObservation.FUNCTION_1D));
        mDesc2Label.setText(g.ext().getDeltaString("2d", BTopoConvergenceObservation.FUNCTION_2D));
        mDesc3Label.setText(g.ext().getDeltaString("3d", BTopoConvergenceObservation.FUNCTION_3D));
        mDesc4Label.setText(date);
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

    private class AlarmIndicator extends BAlarmIndicator<BTopoConvergenceGroup> {

        public AlarmIndicator() {
            addNodes(m1dShape, m2dShape, m3dShape);
        }

        @Override
        public void update(BTopoConvergenceGroup p) {
            var color1 = ButterflyHelper.getAlarmColorFx(p.ext().getAlarmLevel(BTopoConvergenceObservation.FUNCTION_1D));
            var color2 = ButterflyHelper.getAlarmColorFx(p.ext().getAlarmLevel(BTopoConvergenceObservation.FUNCTION_2D));
            var color3 = ButterflyHelper.getAlarmColorFx(p.ext().getAlarmLevel(BTopoConvergenceObservation.FUNCTION_3D));

            m1dShape.setFill(color1);
            m2dShape.setFill(color2);
            m3dShape.setFill(color3);
        }
    }

}
