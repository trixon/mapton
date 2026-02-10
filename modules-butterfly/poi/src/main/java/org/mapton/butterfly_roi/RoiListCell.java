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
package org.mapton.butterfly_roi;

import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.BRoi;

/**
 *
 * @author Patrik Karlström
 */
class RoiListCell extends BListCell<BRoi> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Label mDesc4Label = new Label();

    public RoiListCell() {
        createUI();
    }

//    @Override
//    protected void updateItem(BRoi roi, boolean empty) {
//        super.updateItem(roi, empty);
//        if (roi == null || empty) {
//            clearContent();
//        } else {
//            addContent(roi);
//        }
//    }
    @Override
    protected void addContent(BRoi p) {
        setText(null);
        setGraphic(mVBox);

        var header = "%s  %s".formatted(p.getOrigin(), p.getName());
        var sta = p.getStatus();
        var cls = p.getClassification();
        if (!StringUtils.isAllBlank(sta, cls)) {
            var sb = new StringBuilder();
            if (StringUtils.isNotBlank(sta)) {
                sb.append(sta);
                if (StringUtils.isNotBlank(cls)) {
                    sb.append(" ");
                }
            }
            if (StringUtils.isNotBlank(cls)) {
                sb.append(cls);
            }
            header = "%s [%s]".formatted(header, sb.toString());
        }

        var desc1 = "%s: %s".formatted(
                StringUtils.defaultIfBlank(p.getGroup(), "NOVALUE"),
                StringUtils.defaultIfBlank(p.getCategory(), "NOVALUE")
        );

//        var date = Objects.toString(DateHelper.toDateTimeString(p.getDateLatest()), "-");
//        mHeaderLabel.setText(p.getName());
//        mDateLabel.setText("%s %s".formatted(date, p.getComment()));
//        mGroupLabel.setText(p.getGroup());
//        setGraphic(mVBox);
        mHeaderLabel.setText(header);
        mDesc1Label.setText(desc1);
        mDesc2Label.setText(p.getComment());
    }

//    private void clearContent() {
//        setText(null);
//        setGraphic(null);
//    }
    private void createUI() {
        mVBox.getChildren().setAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label
        //                mDesc3Label
        //                mDesc4Label
        );

//        mHeaderLabel.setGraphic(mAlarmIndicator);
        activateTooltip();
    }

}
