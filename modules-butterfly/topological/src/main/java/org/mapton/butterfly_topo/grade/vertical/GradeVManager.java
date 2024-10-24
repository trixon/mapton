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
package org.mapton.butterfly_topo.grade.vertical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVManager extends GradeManagerBase {

    public static final Double MAX_HORIZONTAL_DISTANCE = 10.0;
    public static final Double MIN_HORIZONTAL_DISTANCE = 0.0;
    public static final Double MIN_VERTICAL_DISTANCE = 1.0;
    public static final Double MAX_VERTICAL_DISTANCE = 50.0;
    private final GradeVPropertiesBuilder mPropertiesBuilder = new GradeVPropertiesBuilder();

    public static GradeVManager getInstance() {
        return Holder.INSTANCE;
    }

    private GradeVManager() {
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
        var sourcePoints = mTopoManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._3d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                .toList();

        for (var p1 : sourcePoints) {
            if (ObjectUtils.anyNull(p1.getZeroX(), p1.getZeroY())) {
                continue;
            }
            var point = new Point2D(p1.getZeroX(), p1.getZeroY());
            for (var p2 : sourcePoints) {
                if (ObjectUtils.anyNull(p2.getZeroX(), p2.getZeroY())) {
                    continue;
                }

                double distanceR = point.distance(p2.getZeroX(), p2.getZeroY());
                double distanceH = Math.abs(p2.getZeroZ() - p1.getZeroZ());

                if (p1 != p2
                        && MathHelper.isBetween(MIN_HORIZONTAL_DISTANCE, MAX_HORIZONTAL_DISTANCE, distanceR)
                        && MathHelper.isBetween(MIN_VERTICAL_DISTANCE, MAX_VERTICAL_DISTANCE, distanceH)) {
                    if (!pointToPoints.computeIfAbsent(p2.getName(), k -> new HashSet<>()).contains(p1.getName())) {//Skip A-B, B-A
                        pointToPoints.computeIfAbsent(p1.getName(), k -> new HashSet<>()).add(p2.getName());
                    }
                }
            }
        }

        var grades = new ArrayList<BTopoGrade>();
        for (var entry : pointToPoints.entrySet()) {
            var p1 = mTopoManager.getItemForKey(entry.getKey());
            for (var n2 : entry.getValue()) {
                var p2 = mTopoManager.getItemForKey(n2);
                var grade = new BTopoGrade(BAxis.VERTICAL, p1, p2);
                if (grade.getCommonObservations().size() > 1
                        //                            && ( Math.abs(pair.getZQuota()) > 0.00001)) {
                        && (Math.abs(grade.ext().getDiff().getRQuota()) > 0.00001 || Math.abs(grade.ext().getDiff().getZQuota()) > 0.00001)) {
                    grades.add(grade);
                }
            }
        }

        Collections.sort(grades, (o1, o2) -> Double.valueOf(Math.abs(o2.ext().getDiff().getRQuota())).compareTo(Math.abs(o1.ext().getDiff().getRQuota())));

        grades.forEach(t -> {
            var first = new MLatLon(t.getP1().getLat(), t.getP1().getLon());
            var second = new MLatLon(t.getP2().getLat(), t.getP2().getLon());
            var d = first.distance(second);
            var b = first.getBearing(second);
            var mid = first.getDestinationPoint(b, d * .5);
            t.setLat(mid.getLatitude());
            t.setLon(mid.getLongitude());
        });

        FxHelper.runLater(() -> {
            getAllItems().setAll(grades);
            getFilteredItems().setAll(grades);
            getTimeFilteredItems().setAll(grades);
        });
    }

    @Override
    protected void applyTemporalFilter() {
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoGrade> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GradeVManager INSTANCE = new GradeVManager();
    }
}
