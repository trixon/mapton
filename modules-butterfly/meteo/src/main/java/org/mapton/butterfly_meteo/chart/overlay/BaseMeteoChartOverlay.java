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
package org.mapton.butterfly_meteo.chart.overlay;

import java.time.LocalDate;
import java.util.List;
import org.mapton.butterfly_core.api.BChartOverlay;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_format.types.BScope;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseMeteoChartOverlay extends BChartOverlay {

    public static final int MAX_COUNT = 5;
    public static final int MAX_DISTANCE = 30_000;

    public BaseMeteoChartOverlay(String title) {
        super(title);
    }

    protected List<BMeteoPoint> getGlobalPoints(BBasePoint p, LocalDate startDate) {
        var points = p.getButterfly().meteo().getMeteoPoints().stream().filter(p2 -> p2.getScope() == BScope.GLOBAL).toList();
        points = ButterflyHelper.getLimitedPoints(p, points, true, MAX_DISTANCE, MAX_COUNT, startDate, true);

        return points;
    }

}
