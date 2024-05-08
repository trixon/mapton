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

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;

/**
 *
 * @author Patrik Karlström
 */
class ConvergenceGroupListCell extends ListCell<BTopoConvergenceGroup> {

    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();
    private final Label mNameLabel = new Label();
    private final String mStyleBold = "-fx-font-weight: bold;";
    private final String mStyleMono = "-fx-font-family: monospace;";
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

    private void addContent(BTopoConvergenceGroup group) {
        setText(null);
        var header = group.getName();
        if (StringUtils.isNotBlank(group.getStatus())) {
            header = "%s [%s]".formatted(header, group.getStatus());
        }

        mNameLabel.setText(header);
        try {
            var maxD = group.ext2().getMaxDeltaDistanceOverTime();
            var maxL = group.ext2().getMaxDeltaROverTime();
            var maxH = group.ext2().getMaxDeltaZOverTime();

            mDesc2Label.setText("ΔL=%.3f (%s)".formatted(maxD.getDeltaDistanceOverTime(), maxD.ext().getShortName()));
            mDesc3Label.setText("ΔP=%.3f (%s)".formatted(maxL.getDeltaROverTime(), maxL.ext().getShortName()));
            mDesc4Label.setText("ΔH=%.3f (%s)".formatted(maxH.getDeltaZOverTime(), maxH.ext().getShortName()));

        } catch (Exception e) {
        }
        setGraphic(mVBox);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mDesc2Label.setStyle(mStyleMono);
        mDesc3Label.setStyle(mStyleMono);
        mDesc4Label.setStyle(mStyleMono);

        mVBox = new VBox(
                mNameLabel,
                mDesc2Label,
                mDesc3Label,
                mDesc4Label
        );
    }

}
