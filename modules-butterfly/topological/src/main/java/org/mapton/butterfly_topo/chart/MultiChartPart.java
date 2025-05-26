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
package org.mapton.butterfly_topo.chart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.api.TopoManager;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MultiChartPart extends BMultiChartPart {

//    public final double CUT_OFF_LIMIT = 0.002;
    public final double CUT_OFF_LIMIT = 0.0;
    private final BDimension mDimension;

    public MultiChartPart(BDimension dimension) {
        mDimension = dimension;
    }

    @Override
    public String getCategory() {
        return BTopoControlPoint.class.getName();
    }

    @Override
    public BaseManager getManager() {
        return TopoManager.getInstance();
    }

    @Override
    public ArrayList<BTopoControlPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = TopoManager.getInstance().getTimeFilteredItems().stream()
                .filter(p -> {
                    switch (mDimension) {
                        case _1d -> {
                            return p.getDimension() != BDimension._2d;
                        }
                        case _2d -> {
                            return p.getDimension() != BDimension._1d;
                        }
                        case _3d -> {
                            return p.getDimension() == BDimension._3d;
                        }
                        default ->
                            throw new AssertionError();
                    }
                })
                .filter(p -> {
                    try {
                        if (p.ext().getDateLatest().toLocalDate().isBefore(LocalDate.now().minusMonths(3))) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }

                    return true;
                })
                .filter(p -> {
                    return latLon.distance(BCoordinatrix.toLatLon(p)) <= LIMIT_DISTANCE_TOPO;
                })
                .collect(Collectors.toCollection(ArrayList::new));

        var pointsToExclude = new ArrayList<BTopoControlPoint>();

        for (var p : pointList) {
            var observations = p.ext().getObservationsTimeFiltered().stream()
                    .filter(o -> DateHelper.isBetween(firstDate, lastDate, o.getDate().toLocalDate()))
                    //                    .filter(o -> o.getMeasuredZ() != null)
                    .map(o -> {
                        var oo = new BTopoControlPointObservation();
                        oo.setDate(o.getDate());
                        oo.setMeasuredX(o.getMeasuredX());
                        oo.setMeasuredY(o.getMeasuredY());
                        oo.setMeasuredZ(o.getMeasuredZ());
                        oo.ext().setAccuX(o.ext().getAccuX());
                        oo.ext().setAccuY(o.ext().getAccuY());
                        oo.ext().setAccuZ(o.ext().getAccuZ());
                        return oo;
                    })
                    .toList();

            if (observations.size() > 1) {
                var map = new TreeMap<LocalDateTime, Double>();
                var firstAccuX = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getAccuX());
                var firstAccuY = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getAccuY());
                var firstAccuZ = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getAccuZ());
                var firstDelta1d = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getDeltaZ());
                var firstDelta2d = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getDelta2d());
                var firstDelta3d = MathHelper.convertDoubleToDouble(observations.getFirst().ext().getDelta3d());

                for (var o : observations) {
                    var value = 0.0;

                    switch (mDimension) {
                        case _1d -> {
                            var accuZ = MathHelper.convertDoubleToDouble(o.ext().getAccuZ());
                            var valueZ = o.getMeasuredZ() - observations.getFirst().getMeasuredZ();
                            valueZ = valueZ + firstAccuZ - accuZ;

                            value = valueZ;
                        }
                        case _2d -> {
                            var accuX = MathHelper.convertDoubleToDouble(o.ext().getAccuX());
                            var valueX = o.getMeasuredX() - observations.getFirst().getMeasuredX();
                            valueX = valueX + firstAccuX - accuX;

                            var accuY = MathHelper.convertDoubleToDouble(o.ext().getAccuY());
                            var valueY = o.getMeasuredY() - observations.getFirst().getMeasuredY();
                            valueY = valueY + firstAccuY - accuY;

                            value = Math.hypot(valueX, valueY);
                        }
                        case _3d -> {
                            var accuZ = MathHelper.convertDoubleToDouble(o.ext().getAccuZ());
                            var valueZ = o.getMeasuredZ() - observations.getFirst().getMeasuredZ();
                            valueZ = valueZ + firstAccuZ - accuZ;

                            var accuX = MathHelper.convertDoubleToDouble(o.ext().getAccuX());
                            var valueX = o.getMeasuredX() - observations.getFirst().getMeasuredX();
                            valueX = valueX + firstAccuX - accuX;

                            var accuY = MathHelper.convertDoubleToDouble(o.ext().getAccuY());
                            var valueY = o.getMeasuredY() - observations.getFirst().getMeasuredY();
                            valueY = valueY + firstAccuY - accuY;

                            value = Math.hypot(valueX, valueY);
                            value = Math.hypot(value, valueZ);
                            if (valueZ < 0) {
                                value = -value;
                            }
                        }
                        default ->
                            throw new AssertionError();
                    }
                    map.put(o.getDate(), value);
                }

                if (Math.abs(map.lastEntry().getValue()) > CUT_OFF_LIMIT) {
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

        return pointList.stream().limit(20).collect(Collectors.toCollection(ArrayList::new));
    }
}
