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
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;

/**
 *
 * @author Patrik Karlström
 */
class ConvergencePairListCell extends ListCell<BTopoConvergencePair> {

    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mPointNamesLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;

    public ConvergencePairListCell() {
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
        var desc2 = "ΔΔL=%.1f mm  ΔΔR=%.1f mm  ΔΔH=%.1f mm".formatted(
                ddd * 1000,
                pair.getDeltaROverTime() * 1000,
                pair.getDeltaZOverTime() * 1000
        );

        var desc3 = "ΔL=%.3f  ΔR=%.3f  ΔH=%.3f  ".formatted(
                pair.getDistance(),
                pair.getDeltaR(),
                pair.getDeltaZ()
        );

        mDesc2Label.setText(desc2);
        mDesc3Label.setText(desc3);

        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mPointNamesLabel.setStyle(mStyleBold);
        mVBox = new VBox(
                mPointNamesLabel,
                mDesc2Label,
                mDesc3Label
        );
    }

}
