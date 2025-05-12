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
package org.mapton.butterfly_geo.inclinometer.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPointObservation;
import org.mapton.butterfly_geo.inclinometer.InclinoManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartPart.class)
public class InclinoMultiChartPart extends BMultiChartPart {

    public InclinoMultiChartPart() {
    }

    @Override
    public String getAxisLabel() {
        return "mm";
    }

    @Override
    public String getCategory() {
        return BAcousticBlast.class.getName();
    }

    @Override
    public String getDecimalPattern() {
        return "0.0";
    }

    @Override
    public BaseManager getManager() {
        return InclinoManager.getInstance();
    }

    @Override
    public String getName() {
        return "Inklinometrar";
    }

    @Override
    public ArrayList<BGeoInclinometerPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = InclinoManager.getInstance().getTimeFilteredItems().stream()
                .filter(p -> {
                    return latLon.distance(new MLatLon(p.getLat(), p.getLon())) <= LIMIT_DISTANCE_BLAST;
                })
                .filter(p -> {
                    try {
                        if (p.ext().getDateFirst().toLocalDate().isAfter(lastDate)
                                || p.ext().getDateLatest().toLocalDate().isBefore(firstDate)) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        for (var p : pointList) {
            System.out.println(p.getName());
        }

        var pointsToExclude = new ArrayList<BGeoInclinometerPoint>();
        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BGeoInclinometerPointObservation();
                        oo.setDate(o.getDate());
                        oo.setMeasuredZ(o.getMeasuredZ());
                        return oo;
                    })
                    .toList();

            if (observations.size() > 1) {
                var map = new TreeMap<LocalDateTime, Double>();
                for (var o : observations) {
                    map.put(o.getDate(), o.getMeasuredZ() - observations.getFirst().getMeasuredZ());
                }
                p.setValue(BMultiChartPart.class, map);
            } else {
                pointsToExclude.add(p);
            }
        }

        pointList.removeAll(pointsToExclude);
        sortPointList(pointList);

        return pointList;
    }

}
