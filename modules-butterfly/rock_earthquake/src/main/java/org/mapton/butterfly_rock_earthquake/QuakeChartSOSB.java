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

import org.mapton.api.MChartSOSB;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.mapton.butterfly_rock_earthquake.chart.QuakeChartOverlay;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSimpleObjectStorageBoolean.Misc.class)
public class QuakeChartSOSB extends MChartSOSB {

    public QuakeChartSOSB() {
        setName(Bundle.CTL_EarthquakeAction());
        setColor(FxHelper.colorToFxColor(QuakeChartOverlay.COLOR));
        setTooltipText("De %d kraftigaste/km.".formatted(QuakeChartOverlay.MAX_COUNT));
    }

}
