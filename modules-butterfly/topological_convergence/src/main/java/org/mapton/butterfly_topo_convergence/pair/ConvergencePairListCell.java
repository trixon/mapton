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

    private final Label mGroupLabel = new Label();
    private final Label mDeltaLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private VBox mVBox;
    private final Label mPointNamesLabel = new Label();

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
        mPointNamesLabel.setText(pair.getName());
        mGroupLabel.setText(pair.getConvergenceGroup().getName());

        var deltas = "ΔL=%.3f  ΔR=%.3f  ΔH=%.3f  ".formatted(
                pair.getDistance(),
                pair.getDeltaR(),
                pair.getDeltaZ()
        );
        mDeltaLabel.setText(deltas);

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
                mGroupLabel,
                mDeltaLabel
        );
    }

}
