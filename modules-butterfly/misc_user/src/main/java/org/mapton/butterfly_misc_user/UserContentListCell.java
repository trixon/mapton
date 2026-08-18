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
package org.mapton.butterfly_misc_user;

import java.util.Objects;
import javafx.scene.control.Label;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
class UserContentListCell extends BContentListCell<BXyzPoint> {

    private final Label mDateLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final Label mNameLabel = new Label();

    public UserContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BXyzPoint xyz) {
        setText(null);
        var date = Objects.toString(DateHelper.toDateTimeString(xyz.getDateLatest()), "-");
        mNameLabel.setText(xyz.getName());
        mDateLabel.setText("%s, Z %+.1f m".formatted(date, xyz.getZeroZ()));
        mGroupLabel.setText(xyz.getGroup());
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
