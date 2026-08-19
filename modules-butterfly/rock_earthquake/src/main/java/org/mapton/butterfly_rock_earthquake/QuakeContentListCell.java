/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_rock_earthquake;

import javafx.scene.control.Label;
import org.mapton.api.MOptions;
import org.mapton.butterfly_core.api.BContentListCell;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;

/**
 *
 * @author Patrik Karlström
 */
class QuakeContentListCell extends BContentListCell<BRockEarthquake> {

    private final Label mDateLabel = new Label();
    private final Label mNameLabel = new Label();

    public QuakeContentListCell() {
        createUI();
    }

    @Override
    protected void addContent(BRockEarthquake quake) {
        setText(null);
        var distance = MOptions.getInstance().getMapHome().distance(BCoordinatrix.toLatLon(quake));
        mNameLabel.setText("M %.1f %s @%.1f km (%,.0f km)".formatted(
                quake.getMag(),
                quake.getMagType(),
                quake.getZeroZ(),
                distance / 1000.0
        ));
        mDateLabel.setText(DATE_TIME_FORMATTER_UTC.format(quake.getDateLatest().plusSeconds(30)));
        setGraphic(mVBox);
    }

    private void createUI() {
        mNameLabel.setStyle(mStyleBold);
        mVBox.getChildren().setAll(
                mNameLabel,
                mDateLabel
        );
    }

}
