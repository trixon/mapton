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
package org.mapton.butterfly_structural.load.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPointObservation;
import org.mapton.butterfly_structural.load.LoadManager;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BlastMultiChartPart extends BMultiChartPart {

    private final Predicate<BStructuralLoadCellPoint> mPredicate;

    public BlastMultiChartPart(Predicate<BStructuralLoadCellPoint> predicate) {
        mPredicate = predicate;
    }

    @Override
    public String getCategory() {
        return BAcousticBlast.class.getName();
    }

    @Override
    public BaseManager getManager() {
        return LoadManager.getInstance();
    }

    @Override
    public ArrayList<BStructuralLoadCellPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = LoadManager.getInstance().getTimeFilteredItems().stream()
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
                    return latLon.distance(BCoordinatrix.toLatLon(p)) <= LIMIT_DISTANCE_BLAST;
                }).collect(Collectors.toCollection(ArrayList::new));

        var pointsToExclude = new ArrayList<BStructuralLoadCellPoint>();
        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BStructuralLoadCellPointObservation();
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
                    var value = o.getMeasuredZ() - observations.getFirst().getMeasuredZ();
                    value = value + firstAccuZ - accuZ;
                    map.put(o.getDate(), value);
                }
                if (Math.abs(map.lastEntry().getValue()) > 0.002) {
                    p.setValue(BMultiChartPart.class, map);
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
