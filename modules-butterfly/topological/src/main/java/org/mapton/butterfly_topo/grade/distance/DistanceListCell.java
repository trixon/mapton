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
package org.mapton.butterfly_topo.grade.distance;

import javafx.scene.control.Label;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
class DistanceListCell extends BListCell<BTopoGrade> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();

    public DistanceListCell() {
        createUI();
    }

    @Override
    protected void addContent(BTopoGrade p) {
        setText(null);
        setGraphic(mVBox);
        mAlarmIndicator.update(p);

        var header = p.getName();
        var gradeDiff = p.ext().getDiff();

        mHeaderLabel.setText(header);
        mDesc1Label.setText("%.0f mm".formatted(gradeDiff.getPartialDiffDistance()));
        mDesc2Label.setText("Δ=%.1f m, ΔH=%.1f m, ΔP=%.1f m".formatted(
                p.getDistance3d(),
                p.getDistanceHeight(),
                p.getDistancePlane()
        ));
        mDesc3Label.setText("%s (%d)".formatted(p.getPeriod(), p.getCommonObservations().size()));
    }

    private void createUI() {
        mVBox.getChildren().setAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc3Label
        );

        mHeaderLabel.setGraphic(mAlarmIndicator);
    }

    private class AlarmIndicator extends BAlarmIndicator<BTopoGrade> {

        public AlarmIndicator() {
            addNodes(m1dShape);
        }

        @Override
        public void update(BTopoGrade p) {
            m1dShape.setFill(SwingHelper.colorToColor(TopoHelper.getGradeDistanceColor(p)));
            m1dShape.setVisible(true);
        }
    }
}
