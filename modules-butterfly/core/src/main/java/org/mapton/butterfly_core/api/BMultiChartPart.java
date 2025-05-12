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
import java.util.TreeMap;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BMultiChartPart {

    public static final double LIMIT_DISTANCE_BLAST = 40.0;
    public static final double LIMIT_DISTANCE_TOPO = 40.0;

    public String getAxisLabel() {
        return "m";
    }

    public abstract String getCategory();

    public String getDecimalPattern() {
        return "0.000";
    }

    public abstract BaseManager getManager();

    public abstract String getName();

    public abstract ArrayList<? extends BXyzPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate);

    public void panTo(String pointName) {
        var p = getManager().getItemForKey(pointName);
        Mapton.getEngine().panTo(getManager().getLatLonForItem(p));
    }

    public void select(String pointName) {
        var p = getManager().getItemForKey(pointName);
        FxHelper.runLater(() -> getManager().selectedItemProperty().setValue(p));
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
                TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartPart.class);
                return Math.abs(map.lastEntry().getValue());
            }
        });
    }
}
