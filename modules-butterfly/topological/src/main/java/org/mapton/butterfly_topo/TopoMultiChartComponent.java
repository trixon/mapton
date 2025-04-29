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
package org.mapton.butterfly_topo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BMultiChartComponent;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.api.TopoManager;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TopoMultiChartComponent extends BMultiChartComponent {

    private final Predicate<BTopoControlPoint> mPredicate;

    public TopoMultiChartComponent(Predicate<BTopoControlPoint> predicate) {
        mPredicate = predicate;
    }

    @Override
    public List<BTopoControlPoint> getPointsAndSeries(MLatLon latLon, LocalDate firstDate, LocalDate lastDate) {
        var pointList = TopoManager.getInstance().getTimeFilteredItems().stream()
                .filter(mPredicate)
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

        var pointsToExclude = new ArrayList<BTopoControlPoint>();
        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BTopoControlPointObservation();
                        oo.setDate(o.getDate());
                        oo.setMeasuredZ(o.getMeasuredZ());
                        oo.ext().setAccuZ(o.ext().getAccuZ());
                        return oo;
                    })
                    .toList();

            if (observations.size() > 1) {
                var map = new TreeMap<LocalDateTime, Double>();
                var firstAccuZ = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getAccuZ());
                for (var o : observations) {
                    var accuZ = MathHelper.convertDoubleToDouble(o.ext().getAccuZ());
                    var z = o.getMeasuredZ() - observations.getFirst().getMeasuredZ();
                    z = z + firstAccuZ - accuZ;
                    map.put(o.getDate(), z);
                }
                if (Math.abs(map.lastEntry().getValue()) > 0.002) {
                    p.setValue(BMultiChartComponent.class, map);
                } else {
                    pointsToExclude.add(p);
                }
            } else {
                pointsToExclude.add(p);
            }
        }

        pointList.removeAll(pointsToExclude);
        sortPointList(pointList);

        return pointList;
    }

}
