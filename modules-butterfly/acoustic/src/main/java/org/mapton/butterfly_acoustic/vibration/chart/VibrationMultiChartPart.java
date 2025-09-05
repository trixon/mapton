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
package org.mapton.butterfly_acoustic.vibration.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_acoustic.vibration.VibrationManager;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationObservation;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartPart.class)
public class VibrationMultiChartPart extends BMultiChartPart {

    public VibrationMultiChartPart() {
    }

    @Override
    public String getAxisLabel() {
        return "mm/s";
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
        return VibrationManager.getInstance();
    }

    @Override
    public String getName() {
        return "Vibrationer";
    }

    @Override
    public ArrayList<BAcousticVibrationPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = VibrationManager.getInstance().getTimeFilteredItems().stream()
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
                .filter(p -> {
                    var distance = latLon.distance(new MLatLon(p.getLat(), p.getLon()));
                    p.setValue("DistanceToBlast", distance);
                    return distance <= LIMIT_DISTANCE_BLAST;
                }).collect(Collectors.toCollection(ArrayList::new));

        var pointsToExclude = new ArrayList<BXyzPoint>();
        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BAcousticVibrationObservation();
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
        pointList.sort((o1, o2) -> Double.compare(o1.getValue("DistanceToBlast"), o2.getValue("DistanceToBlast")));

        return pointList;
    }

}
