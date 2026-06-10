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
package org.mapton.butterfly_geo_reinforcement;

import java.util.Objects;
import javafx.scene.control.Label;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.geo.BGeoReinforcementPoint;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class ReinforcementContentListCell extends BContentListCell<BGeoReinforcementPoint> {

    private final Label mDateLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final Label mNameLabel = new Label();

    public ReinforcementContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BGeoReinforcementPoint drillPoint) {
        setText(null);
        var date = Objects.toString(DateHelper.toDateTimeString(drillPoint.getDateLatest()), "-");
        mNameLabel.setText(drillPoint.getName());
        mDateLabel.setText("%s %s".formatted(date, drillPoint.getComment()));
        mGroupLabel.setText(drillPoint.getGroup());
        setGraphic(mVBox);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(
                mNameLabel,
                mDateLabel,
                mGroupLabel
        );
    }

}
