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
package org.mapton.butterfly_topo.heightpair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoHeightPair;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.heightpair.chart.TopoHeightPairChartBuilder;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class HeightPairManager extends BaseManager<BTopoHeightPair> {

    public static final Double MAX_2D_DISTANCE = 10.0;
//    public static final Double MIN_GRADE_H = 0.000005;
//    public static final Double MIN_RADIAL_DISTANCE = 0.0;

    private final TopoHeightPairChartBuilder mChartBuilder = new TopoHeightPairChartBuilder();
    private final HeightPairPropertiesBuilder mPropertiesBuilder = new HeightPairPropertiesBuilder();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public static HeightPairManager getInstance() {
        return Holder.INSTANCE;
    }

    public HeightPairManager() {
        super(BTopoHeightPair.class);
    }

    @Override
    public Object getObjectChart(BTopoHeightPair p) {
        return mChartBuilder.build(p);
    }

    @Override
    public Object getObjectProperties(BTopoHeightPair selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    public void load() {
        var pointToPoints = new TreeMap<String, HashSet<String>>();
        var dimensionToPointsMap = mTopoManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() != BDimension._2d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                .collect(Collectors.groupingBy(BTopoControlPoint::getDimension));

        if (dimensionToPointsMap.containsKey(BDimension._1d) && dimensionToPointsMap.containsKey(BDimension._3d)) {
            for (var p1 : dimensionToPointsMap.get(BDimension._1d)) {
                var point = new Point2D(p1.getZeroX(), p1.getZeroY());
                for (var p2 : dimensionToPointsMap.get(BDimension._3d)) {
                    if (p1.getZeroZ() > p2.getZeroZ()) {
                        continue;
                    }
                    var distance = point.distance(p2.getZeroX(), p2.getZeroY());
                    if (p1 != p2 && distance <= MAX_2D_DISTANCE) {
                        if (!pointToPoints.computeIfAbsent(p2.getName(), k -> new HashSet<>()).contains(p1.getName())) {//Skip A-B, B-A
                            pointToPoints.computeIfAbsent(p1.getName(), k -> new HashSet<>()).add(p2.getName());
                        }
                    }
                }
            }
        }

        var pairsAll = new ArrayList<BTopoHeightPair>();
        for (var entry : pointToPoints.entrySet()) {
            var p1 = mTopoManager.getItemForKey(entry.getKey());
            for (var n2 : entry.getValue()) {
                var p2 = mTopoManager.getItemForKey(n2);
                var pair = new BTopoHeightPair(p1, p2);
                if (pair.getCommonObservations().size() > 1) {
                    pairsAll.add(pair);
                }
            }
        }

//        Comparator<BTopoGrade> c1 = (o1, o2)
//                -> Integer.valueOf(o1.ext().getAlarmLevelHeight(Math.abs(o1.ext().getDiff().getZQuota())))
//                        .compareTo(o2.ext().getAlarmLevelHeight(Math.abs(o2.ext().getDiff().getZQuota())));
//        Comparator<BTopoGrade> c2 = (o1, o2)
//                -> Double.valueOf(Math.abs(o1.ext().getDiff().getZQuota()))
//                        .compareTo(Math.abs(o2.ext().getDiff().getZQuota()));
        var pairsLim = pairsAll.stream()
                //                .sorted(c1.reversed().thenComparing(c2.reversed()))
                .limit(1000)
                .collect(Collectors.toCollection(ArrayList::new));

        pairsLim.forEach(pair -> {
            var first = BCoordinatrix.toLatLon(pair.getP1());
            var second = BCoordinatrix.toLatLon(pair.getP2());
            var d = first.distance(second);
            var b = first.getBearing(second);
            var mid = first.getDestinationPoint(b, d * .5);
            pair.setLat(mid.getLatitude());
            pair.setLon(mid.getLongitude());
        });

        FxHelper.runLater(() -> {
            setItemsAll(pairsLim);
            setItemsFiltered(pairsLim);
            setItemsTimeFiltered(pairsLim);
        });
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoHeightPair> items) {
    }

    private static class Holder {

        private static final HeightPairManager INSTANCE = new HeightPairManager();
    }
}
