/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade.distancez;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import static org.mapton.butterfly_topo.heightpair.HeightPairManager.MAX_2D_DISTANCE;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeDzManager extends GradeManagerBase {

    public static final Double MAX_RADIAL_DISTANCE = 50.0;
    public static final Double MIN_RADIAL_DISTANCE = 0.050;
    private final GradeDzPropertiesBuilder mPropertiesBuilder = new GradeDzPropertiesBuilder();

    public static GradeDzManager getInstance() {
        return Holder.INSTANCE;
    }

    private GradeDzManager() {
        super(BTopoGrade.class);
    }

    @Override
    public Object getObjectProperties(BTopoGrade selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
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

        var gradesAll = new ArrayList<BTopoGrade>();
        for (var entry : pointToPoints.entrySet()) {
            var p1 = mTopoManager.getItemForKey(entry.getKey());
            for (var n2 : entry.getValue()) {
                var p2 = mTopoManager.getItemForKey(n2);
                var grade = new BTopoGrade(BAxis.RESULTANT, p1, p2);
                if (grade.getCommonObservations().size() > 1 && true) {
                    gradesAll.add(grade);
                }
            }
        }

        Comparator<BTopoGrade> c1 = (o1, o2)
                -> Double.valueOf(o1.ext().getDiff().getPartialDiffDistanceAbs())
                        .compareTo(o2.ext().getDiff().getPartialDiffDistanceAbs());

        var gradesLim = gradesAll.stream()
                .sorted(c1.reversed())
                .limit(1000)
                .collect(Collectors.toCollection(ArrayList::new));

        gradesLim.forEach(g -> {
            var first = BCoordinatrix.toLatLon(g.getP1());
            var second = BCoordinatrix.toLatLon(g.getP2());
            var d = first.distance(second);
            var b = first.getBearing(second);
            var mid = first.getDestinationPoint(b, d * .5);
            g.setLat(mid.getLatitude());
            g.setLon(mid.getLongitude());
        });

        FxHelper.runLater(() -> {
            setItemsAll(gradesLim);
            setItemsFiltered(gradesLim);
            setItemsTimeFiltered(gradesLim);
        });
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoGrade> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GradeDzManager INSTANCE = new GradeDzManager();
    }
}
