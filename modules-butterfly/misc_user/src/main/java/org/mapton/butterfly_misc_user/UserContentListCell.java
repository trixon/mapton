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

import java.time.LocalDate;
import javafx.scene.control.Label;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_format.types.BSystemUser;

/**
 *
 * @author Patrik Karlström
 */
class UserContentListCell extends BContentListCell<BSystemUser> {

    private final Label mDateLabel = new Label();
    private final Label mGroupLabel = new Label();
    private final Label mAccessLabel = new Label();
    private final Label mNameLabel = new Label();

    public UserContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BSystemUser user) {
        setText(null);
        var name = "%s %s [%s]".formatted(user.getOrigin(), user.getName(), user.getStatus());
        var expired = user.getExpires().isBefore(LocalDate.now())
                ? "  💀 %s 💀".formatted(user.getExpires().toString())
                : "";
        mNameLabel.setText(name);
        mDateLabel.setText("%s (%.0f)%s".formatted(
                user.getDateLatest().toLocalDate().toString(),
                user.getZeroZ(),
                expired
        ));
        mGroupLabel.setText(user.getGroup());
        mAccessLabel.setText(user.getCategory().replaceAll("\\d+_", ""));
        setGraphic(mVBox);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(
                mNameLabel,
                mDateLabel,
                mGroupLabel,
                mAccessLabel
        );
    }

}
