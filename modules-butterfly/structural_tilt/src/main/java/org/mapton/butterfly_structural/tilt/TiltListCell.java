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
package org.mapton.butterfly_structural.tilt;

import java.time.LocalDate;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class TiltListCell extends ListCell<BStructuralTiltPoint> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final Tooltip mTooltip = new Tooltip();
    private VBox mVBox;

    public TiltListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BStructuralTiltPoint p, boolean empty) {
        super.updateItem(p, empty);
        if (p == null || empty) {
            clearContent();
        } else {
            addContent(p);
        }
    }

    private void addContent(BStructuralTiltPoint p) {
        setText(null);
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
        mAlarmIndicator.update(p);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(dateSB.toString());
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

        public void update(BStructuralTiltPoint p) {
//            mResultantShape.setFill(TopoHelper.getAlarmColorHeightFx(p));
            mResultantShape.setFill(Color.BLUE);
        }

        private void createUI() {
            mResultantShape = new Circle(SIZE / 2);
            var hPane = new StackPane(mResultantShape);

            getChildren().setAll(hPane);
        }
    }
}
