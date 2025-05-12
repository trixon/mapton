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
package org.mapton.butterfly_topo_convergence.group.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_topo_convergence.api.ConvergenceGroupManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartPart.class)
public class ConvergenceGroupMultiChartPart extends BMultiChartPart {

    public ConvergenceGroupMultiChartPart() {
    }

    @Override
    public String getAxisLabel() {
        return "Avstånd";
    }

    @Override
    public String getCategory() {
        return BAcousticBlast.class.getName();
    }

    @Override
    public BaseManager getManager() {
        return ConvergenceGroupManager.getInstance();
    }

    @Override
    public String getName() {
        return "Konvergensgrupper";
    }

    @Override
    public String getDecimalPattern() {
        return "0";
    }

    @Override
    public ArrayList<BTopoConvergenceGroup> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = ConvergenceGroupManager.getInstance().getTimeFilteredItems().stream()
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
                    return latLon.distance(new MLatLon(p.getLat(), p.getLon())) <= LIMIT_DISTANCE_BLAST;
                }).collect(Collectors.toCollection(ArrayList::new));

        var pointsToExclude = new ArrayList<BXyzPoint>();

        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BXyzPointObservation();
                        oo.setDate(o.getDate());
                        oo.setMeasuredZ(latLon.distance(new MLatLon(p.getLat(), p.getLon())));
                        return oo;
                    })
                    .toList();

            if (observations.size() > 1) {
                var sign = -1d;
                for (var o : observations) {
                    if (DateHelper.isBetween(date, date.plusDays(1), o.getDate().toLocalDate())) {
                        sign = 1d;
                        break;
                    }
                }

                var map = new TreeMap<LocalDateTime, Double>();
                for (var o : observations) {
                    map.put(o.getDate(), sign * o.getMeasuredZ());
                }
                p.setValue(BMultiChartPart.class, map);
            } else {
                pointsToExclude.add(p);
            }
        }

        pointList.removeAll(pointsToExclude);

        var debtList = pointList.stream().filter(p -> {
            TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartPart.class);
            return map.lastEntry().getValue() < 0;
        }).sorted(new Comparator<BTopoConvergenceGroup>() {
            @Override
            public int compare(BTopoConvergenceGroup o1, BTopoConvergenceGroup o2) {
                var v1 = getDeltaForPeriod(o1);
                var v2 = getDeltaForPeriod(o2);

                return Double.compare(v2, v1);
            }

            private double getDeltaForPeriod(BBase p) {
                TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartPart.class);
                return map.lastEntry().getValue();
            }
        })
                .collect(Collectors.toCollection(ArrayList::new));

        var okList = new ArrayList<>(pointList);

        sortPointList(pointList);

        okList.removeAll(debtList);
        debtList.addAll(okList);
        return debtList;
    }

    @Override
    public void sortPointList(ArrayList<? extends BBase> pointList) {
        pointList.sort(new Comparator<BBase>() {
            @Override
            public int compare(BBase o1, BBase o2) {
                var v1 = getDeltaForPeriod(o1);
                var v2 = getDeltaForPeriod(o2);

                return Double.compare(v1, v2);
            }

            private double getDeltaForPeriod(BBase p) {
                TreeMap<LocalDateTime, Double> map = p.getValue(BMultiChartPart.class);
                Double value = map.lastEntry().getValue();

                return value;
//                if (value >= 0) {
//                    return LIMIT_DISTANCE_BLAST - value;
//                } else {
//                    return LIMIT_DISTANCE_BLAST - value;
//                }
            }
        });
    }
    /*
    sortera
    negativa fallande  -1 -10 -100
    positiva stigande  1    10  100

    positiva


    --
    plotta avstånd
        + med ok mätning
        - där saknas

    sortera lista
        - först
        minsta minus först (närmast salva)
     */
}
