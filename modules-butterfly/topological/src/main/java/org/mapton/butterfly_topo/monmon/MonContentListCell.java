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
package org.mapton.butterfly_topo.monmon;

import java.util.Objects;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.topo.BTopoMonmon;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class MonContentListCell extends BContentListCell<BTopoMonmon> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();
    private final Label mStationLabel = new Label();

    public MonContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BTopoMonmon p) {
        setText(null);
        var header = "%s  %s".formatted(p.getOrigin(), p.getName());
        var sta = p.getStatus();
        if (StringUtils.isNotBlank(sta)) {
            header = "%s [%s]".formatted(header, sta);
        }
        mNameLabel.setText(header);
        mStationLabel.setText(p.getStationName());
        var firstRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawFirstDate()), "");
        var lastRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "");
        mDateLabel.setText("%s — %s".formatted(firstRaw, lastRaw));
        setGraphic(mVBox);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(
                mNameLabel,
                mStationLabel,
                mDateLabel
        );
    }

}
