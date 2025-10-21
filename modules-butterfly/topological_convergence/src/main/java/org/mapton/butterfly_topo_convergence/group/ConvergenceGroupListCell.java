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

import java.util.function.Function;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceObservation;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class ConvergenceGroupListCell extends ListCell<BTopoConvergenceGroup> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
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
        setText(null);
        mTooltip.setText(Strings.CS.replace(g.getComment(), "\\n", "\r"));

        var header = g.getName();
        if (StringUtils.isNotBlank(g.getStatus())) {
            header = "%s [%s]".formatted(header, g.getStatus());
        }
        var dateLast = StringHelper.toString(g.getDateLatest() == null ? null : g.getDateLatest().toLocalDate(), "NOVALUE");
        var dateZero = StringHelper.toString(g.getDateZero(), "NOVALUE");
        var date = "%s — %s  (%d)".formatted(dateZero, dateLast, g.ext().getPairs().size());

        mAlarmIndicator.update(g);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(g.ext().getDeltaString("1d", BTopoConvergenceObservation.FUNCTION_1D));
        mDesc2Label.setText(g.ext().getDeltaString("2d", BTopoConvergenceObservation.FUNCTION_2D));
        mDesc3Label.setText(g.ext().getDeltaString("3d", BTopoConvergenceObservation.FUNCTION_3D));
        mDesc4Label.setText(date);

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
        private Circle m1dShape;
        private Polygon m2dShape;
        private Rectangle m3dShape;

        public AlarmIndicator() {
            super(SIZE / 4);
            createUI();
        }

        private int getAlarmLevel(BTopoConvergenceGroup group, Function<BTopoConvergenceObservation, Double> function) {
            try {
                var pair = group.ext().getPairWithLargestDiff(function);
                return pair.ext().getAlarmLevel(function);
            } catch (Exception e) {
                return -1;
            }

        }

        public void update(BTopoConvergenceGroup p) {
            var color1 = ButterflyHelper.getAlarmColorFx(getAlarmLevel(p, BTopoConvergenceObservation.FUNCTION_1D));
            var color2 = ButterflyHelper.getAlarmColorFx(getAlarmLevel(p, BTopoConvergenceObservation.FUNCTION_2D));
            var color3 = ButterflyHelper.getAlarmColorFx(getAlarmLevel(p, BTopoConvergenceObservation.FUNCTION_3D));

            m1dShape.setFill(color1);
            m2dShape.setFill(color2);
            m3dShape.setFill(color3);
        }

        private void createUI() {
            m1dShape = new Circle(SIZE / 2);
            var pane1 = new StackPane(m1dShape);

            m2dShape = new Polygon();
            m2dShape.getPoints().addAll(new Double[]{
                SIZE / 2, 0.0,
                SIZE, SIZE,
                0.0, SIZE
            });
            var pane2 = new StackPane(m2dShape);

            m3dShape = new Rectangle(SIZE, SIZE);
            var pane3 = new StackPane(m3dShape);

            getChildren().setAll(pane1, pane2, pane3);
        }
    }

}
