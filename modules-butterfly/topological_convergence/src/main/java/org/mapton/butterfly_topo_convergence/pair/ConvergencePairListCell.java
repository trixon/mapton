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
package org.mapton.butterfly_topo_convergence.pair;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class ConvergencePairListCell extends ListCell<BTopoConvergencePair> {

    private final Color[] mColors;

    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private Polygon mPlaneShape;
    private final Label mPointNamesLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public ConvergencePairListCell() {
        mColors = new Color[]{
            //https://colordesigner.io/gradient-generator/?mode=hsl#00FF00-FF0000
            Color.web("#00ff00"),
            Color.web("#22ff00"),
            Color.web("#44ff00"),
            Color.web("#66ff00"),
            Color.web("#88ff00"),
            Color.web("#aaff00"),
            Color.web("#ccff00"),
            Color.web("#eeff00"),
            Color.web("#ffee00"),
            Color.web("#ffcc00"),
            Color.web("#ffaa00"),
            Color.web("#ff8800"),
            Color.web("#ff6600"),
            Color.web("#ff4400"),
            Color.web("#ff2200"),
            Color.web("#ff0000")
        };
        createUI();
    }

    @Override
    protected void updateItem(BTopoConvergencePair point, boolean empty) {
        super.updateItem(point, empty);
        if (point == null || empty) {
            clearContent();
        } else {
            addContent(point);
        }
    }

    private void addContent(BTopoConvergencePair pair) {
        setText(null);
        var desc1 = "%s (%s)".formatted(pair.getName(), pair.getConvergenceGroup().getName());
        mPointNamesLabel.setText(desc1);
        var ddd = 0.0;
        if (!pair.getObservations().isEmpty()) {
            ddd = pair.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();
        }
        var desc2 = "ΔL=%.1f mm  ΔP=%.1f mm  ΔH=%.1f mm".formatted(
                ddd * 1000,
                pair.getDeltaROverTime() * 1000,
                pair.getDeltaZOverTime() * 1000
        );

        var desc3 = "L=%.3f m  P=%.3f m  H=%.3f m".formatted(
                pair.getDistance(),
                pair.getDeltaR(),
                pair.getDeltaZ()
        );

        var desc4 = "";
        if (!pair.getObservations().isEmpty()) {
            desc4 = "%s - %s (%d)".formatted(
                    pair.getObservations().getFirst().getDate().toLocalDate().toString(),
                    pair.getObservations().getLast().getDate().toLocalDate().toString(),
                    pair.getObservations().size()
            );
        }

        mDesc2Label.setText(desc2);
        mDesc3Label.setText(desc3);
        mDesc4Label.setText(desc4);

        mPlaneShape.setFill(mColors[pair.getLevel(mColors.length)]);

        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        final double SIZE = FxHelper.getUIScaled(12);
        mPlaneShape = new Polygon();
        mPlaneShape.getPoints().addAll(new Double[]{
            SIZE / 2, 0.0,
            SIZE, SIZE,
            0.0, SIZE
        });

        mPointNamesLabel.setGraphic(mPlaneShape);
        mPointNamesLabel.setGraphicTextGap(FxHelper.getUIScaled(8));
        mPointNamesLabel.setStyle(mStyleBold);
        mVBox = new VBox(
                mPointNamesLabel,
                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );
    }

}
