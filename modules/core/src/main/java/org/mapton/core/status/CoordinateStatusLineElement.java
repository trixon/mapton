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
package org.mapton.core.status;

import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MCooTrans;
import org.mapton.api.MEngine;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 598)
public class CoordinateStatusLineElement implements StatusLineElementProvider {

    private JLabel mLabel;

    public CoordinateStatusLineElement() {
    }

    @Override
    public Component getStatusLineElement() {
        if (mLabel == null) {
            init();
            initListeners();
        }

        return mLabel;
    }

    private void init() {
        mLabel = new JLabel();
        mLabel.setFont(new Font("monospaced", Font.PLAIN, mLabel.getFont().getSize()));
        mLabel.setBorder(new EmptyBorder(0, 0, 0, SwingHelper.getUIScaled(8)));
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            updateMousePositionData();
        }, MEngine.KEY_STATUS_COORDINATE);
    }

    private void updateMousePositionData() {
        MEngine engine = Mapton.getEngine();

        if (engine != null) {
            if (engine.getLatitude() != null) {
                double latitude = engine.getLatitude();
                double longitude = engine.getLongitude();

                if (latitude != 0 && longitude != 0) {
                    String altitude = "";
                    if (engine.getAltitude() != null) {
                        double metersAltitude = engine.getAltitude();
                        if (Math.abs(metersAltitude) >= 1000) {
                            altitude = "%s %,7d km".formatted(Dict.ALTITUDE.toString(), (int) Math.round(metersAltitude / 1e3));
                        } else {
                            altitude = "%s %,7d m".formatted(Dict.ALTITUDE.toString(), (int) Math.round(metersAltitude));
                        }
                    }

                    String elevation = "";
                    if (engine.getElevation() != null) {
                        elevation = "%s %,6d %s".formatted(Dict.ELEVATION.toString(), (int) engine.getElevation().doubleValue(), Dict.METERS.toString().toLowerCase());
                    }

                    String text = "";
                    try {
                        text = "%s, %s, %s".formatted(
                                altitude,
                                elevation,
                                MCooTrans.getCooTrans(MOptions.getInstance().getMapCooTransName()).getString(latitude, longitude)
                        );
                    } catch (Exception e) {
                        //nvm
                    }

                    mLabel.setText(StringUtils.removeStart(text, ", , "));
                }
            } else {
                mLabel.setText("");
            }
        }
    }
}
