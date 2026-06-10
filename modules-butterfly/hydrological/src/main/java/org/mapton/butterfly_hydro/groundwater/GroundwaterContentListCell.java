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
package org.mapton.butterfly_hydro.groundwater;

import javafx.scene.control.Label;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;

/**
 *
 * @author Patrik Karlström
 */
class GroundwaterContentListCell extends BContentListCell<BHydroGroundwaterPoint> {

    private final Label mDesc1Label = new Label();
    private final Label mNameLabel = new Label();

    public GroundwaterContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BHydroGroundwaterPoint gw) {
        setText(null);
        mNameLabel.setText(gw.getName());
        mDesc1Label.setText("%s: %s".formatted(gw.getGroup(), gw.getCategory()));
        setGraphic(mVBox);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(mNameLabel, mDesc1Label);
    }

}
