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
package org.mapton.butterfly_meteo;

import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.BListCell;
import org.mapton.butterfly_format.types.BMeteoPoint;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class MeteoListCell extends BListCell<BMeteoPoint> {

    private final Label mDesc1Label = new Label();
    private final Label mDesc2Label = new Label();
    private final Label mDesc3Label = new Label();
    private final Tooltip mTooltip = new Tooltip();

    public MeteoListCell() {
        createUI();
    }

    @Override
    protected void addContent(BMeteoPoint p) {
        setText(null);
        var header = p.getName();
        if (StringUtils.isNotBlank(p.getStatus())) {
            header = "%s [%s]".formatted(header, p.getStatus());
        }

        mHeaderLabel.setText(header);
        mDesc1Label.setText(Strings.CI.replace(p.ext().getDateLatestAsString(), "T", " "));
        try {
            mDesc2Label.setText("%s, %.0f m ö.h.".formatted(p.getDateValidFrom().toString(), p.getZeroZ()));
        } catch (Exception e) {
            mDesc2Label.setText("-");
        }
        var operatorOrigin = "%s :: %s".formatted(StringUtils.defaultIfBlank(p.getOrigin(), "-"), StringUtils.defaultIfBlank(p.getOperator(), "-"));
        mDesc3Label.setText(operatorOrigin);

        mHeaderLabel.setTooltip(new Tooltip("Add custom tooltip: " + p.getName()));
        mTooltip.setText("TODO");
        setGraphic(mVBox);
    }

    private void createUI() {
        mHeaderLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(
                mHeaderLabel,
                mDesc1Label,
                mDesc2Label,
                mDesc3Label
        );

//        mHeaderLabel.setGraphic(mAlarmIndicator);
        mHeaderLabel.setGraphicTextGap(FxHelper.getUIScaled(8));

        mVBox.getChildren().stream()
                .filter(c -> c instanceof Control)
                .map(c -> (Control) c)
                .forEach(o -> o.setTooltip(mTooltip));

        mTooltip.setShowDelay(Duration.seconds(2));
    }

}
