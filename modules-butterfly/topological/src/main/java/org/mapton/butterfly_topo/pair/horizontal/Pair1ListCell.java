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
package org.mapton.butterfly_topo.pair.horizontal;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class Pair1ListCell extends ListCell<BTopoPointPair> {

    private final AlarmIndicator mAlarmIndicator = new AlarmIndicator();
    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mHeaderLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public Pair1ListCell() {
        createUI();
    }

    @Override
    protected void updateItem(BTopoPointPair p, boolean empty) {
        super.updateItem(p, empty);
        if (p == null || empty) {
            clearContent();
        } else {
            addContent(p);
        }
    }

    private void addContent(BTopoPointPair p) {
        setText(null);
        var header = p.getName();
//        if (StringUtils.isNotBlank(p.getStatus())) {
//            header = "%s [%s]".formatted(header, p.getStatus());
//        }

        mAlarmIndicator.update(p);
        mHeaderLabel.setText(header);
        mDesc1Label.setText("%.1f %% TODO Larm?".formatted(p.getZPercentage()));
        mDesc2Label.setText("ΔH=%.1f m, ΔR=%.1f m, ∂iH=%.1f mm".formatted(
                p.getDistanceHeight(),
                p.getDistancePlane(),
                p.getPartialDiffZ() * 1000
        ));
        mDesc3Label.setText("%s - %s (%d)".formatted(p.getDateFirst(), p.getDateLast(), p.getCommonObservations().size()));
//        mDesc4Label.setText("%.1f mm".formatted(p.getPartialDiffZ()));

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
    }

    private class AlarmIndicator extends HBox {

        private static final double SIZE = FxHelper.getUIScaled(12);
        private Circle mHeightShape;

        public AlarmIndicator() {
            super(SIZE / 4);
            createUI();
        }

        public void update(BTopoPointPair p) {
            mHeightShape.setFill(Color.BLUE);
            mHeightShape.setVisible(true);
        }

        private void createUI() {
            mHeightShape = new Circle(SIZE / 2);
            var hPane = new StackPane(mHeightShape);
            getChildren().setAll(hPane);
        }
    }
}
