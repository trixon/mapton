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

import java.util.List;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_topo.TopoHelper;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class ConvergenceGroupListCell extends ListCell<BTopoConvergenceGroup> {

    private final Label mDesc1Label = new Label();
//    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final String mStyleMono = "-fx-font-family: monospace;";
    private final Tooltip mTooltip = new Tooltip();
    private VBox mVBox;
    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();

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
        setText(null);
        mTooltip.setText(Strings.CS.replace(g.getComment(), "\\n", "\r"));

        var header = g.getName();
        if (StringUtils.isNotBlank(g.getStatus())) {
            header = "%s [%s]".formatted(header, g.getStatus());
        }
        header = header + " (%d)".formatted(g.ext().getPairs().size());
        var alarm = g.ext().getAlarm(BComponent.HEIGHT);

        var alarmInfo = g.getAlarm1Id();
        var desc1 = "%s: %s".formatted(alarmInfo, String.join(", ", List.of(alarm.getLimit1(), alarm.getLimit2(), alarm.getLimit3())));
        var dateLast = StringHelper.toString(g.getDateLatest() == null ? null : g.getDateLatest().toLocalDate(), "NOVALUE");
        var dateZero = StringHelper.toString(g.getDateZero(), "NOVALUE");
        mAlarmIndicator.update(g);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(alarmInfo);
//        mDesc2Label.setText(Strings.CI.remove(g.getRef(), g.getName()));
        mDesc3Label.setText(g.ext().getLastDiff());
        mDesc4Label.setText("%s — %s".formatted(dateZero, dateLast));

        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
        mTooltip.setText("");
    }

    private void createUI() {
        mHeaderLabel.setStyle(mStyleBold);
        mVBox = new VBox(
                mHeaderLabel,
                mDesc1Label,
                //                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );

        mHeaderLabel.setGraphic(mAlarmIndicator);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

    private class AlarmIndicator extends HBox {

        private static final double SIZE = FxHelper.getUIScaled(12);
        private Circle mResultantShape;

        public AlarmIndicator() {
            super(SIZE / 4);
            createUI();
        }

        public void update(BTopoConvergenceGroup p) {
            mResultantShape.setFill(TopoHelper.getAlarmColorFx(p));
        }

        private void createUI() {
            mResultantShape = new Circle(SIZE / 2);
            var hPane = new StackPane(mResultantShape);

            getChildren().setAll(hPane);
        }
    }

}
