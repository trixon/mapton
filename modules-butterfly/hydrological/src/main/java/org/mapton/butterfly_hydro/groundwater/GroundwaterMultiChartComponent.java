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
package org.mapton.butterfly_hydro.groundwater;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartComponent.class)
public class GroundwaterMultiChartComponent extends BMultiChartComponent {

    public GroundwaterMultiChartComponent() {
    }

    @Override
    public String getName() {
        return "Grundvatten";
    }

    @Override
    public String getDecimalPattern() {
        return "0.0";
    }

    @Override
    public ArrayList<BHydroGroundwaterPoint> getPointsAndSeries(MLatLon latLon, LocalDate firstDate, LocalDate lastDate) {
        var pointList = GroundwaterManager.getInstance().getTimeFilteredItems().stream()
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
                    return latLon.distance(new MLatLon(p.getLat(), p.getLon())) <= DISTANCE_BLAST;
                }).collect(Collectors.toCollection(ArrayList::new));

        var pointsToExclude = new ArrayList<BXyzPoint>();
        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getGroundwaterLevel() != null)
                    .map(o -> {
                        var oo = new BHydroGroundwaterPointObservation();
                        oo.setDate(o.getDate());
                        oo.setGroundwaterLevel(o.getGroundwaterLevel());
                        return oo;
                    })
                    .toList();

            if (observations.size() > 1) {
                var map = new TreeMap<LocalDateTime, Double>();
                for (var o : observations) {
                    map.put(o.getDate(), o.getGroundwaterLevel() - observations.getFirst().getGroundwaterLevel());
                }
                p.setValue(BMultiChartComponent.class, map);
            } else {
                pointsToExclude.add(p);
            }
        }

        pointList.removeAll(pointsToExclude);
        sortPointList(pointList);

        return pointList;
    }

}
