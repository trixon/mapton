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

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import static org.mapton.butterfly_rock_earthquake.QuakeColorBy.AGE;
import static org.mapton.butterfly_rock_earthquake.QuakeColorBy.MAGNITUDE;

/**
 *
 * @author Patrik Karlström
 */
public class QuakeHelper {

    public static QuakeOptions sOptions = QuakeOptions.getInstance();
    private static final Color sColorsOfDay[] = {
        Color.RED,
        Color.ORANGE,
        Color.YELLOW,
        Color.GREEN,
        Color.BLUE,
        Color.BLUE.brighter().brighter(),
        Color.LIGHT_GRAY};

    public static Color getColor(BRockEarthquake quake) {
        var elapsedDays = Duration.between(quake.getDateLatest(), LocalDateTime.now()).toMillis() / TimeUnit.DAYS.toMillis(1);

        switch (sOptions.getColorBy()) {
            case AGE:
                return sColorsOfDay[Math.min((int) elapsedDays, sColorsOfDay.length - 1)];
            case MAGNITUDE:
                if (quake.getMag() != null) {
                    return ButterflyHelper.getColorAwt(ButterflyHelper.sGreenToRedColors, 5, quake.getMag() - 3);
                } else {
                    return Color.CYAN;
                }
            default:
                throw new AssertionError();
        }

    }

}
