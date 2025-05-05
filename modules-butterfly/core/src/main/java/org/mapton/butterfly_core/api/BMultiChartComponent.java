/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BMultiChartComponent {

    public static final double LIMIT_DISTANCE_BLAST = 40.0;

    public String getAxisLabel() {
        return "m";
    }

    public String getDecimalPattern() {
        return "0.000";
    }

    public abstract BaseManager getManager();

    public abstract String getName();

    public abstract List<? extends BXyzPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate);

    public void panTo(String pointName) {
        var p = getManager().getItemForKey(pointName);
        Mapton.getEngine().panTo(getManager().getLatLonForItem(p));
    }

    public void sortPointList(ArrayList<? extends BBase> pointList) {
        pointList.sort(new Comparator<BBase>() {
            @Override
            public int compare(BBase o1, BBase o2) {
                var v1 = getDeltaForPeriod(o1);
                var v2 = getDeltaForPeriod(o2);

                return Double.compare(v2, v1);
            }

            private double getDeltaForPeriod(BBase p) {
                TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartComponent.class);
                return Math.abs(map.lastEntry().getValue());
            }
        });
    }
}
