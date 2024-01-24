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
package org.mapton.butterfly_format.types.topo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.TreeMap;
import javafx.geometry.Point2D;
import javafx.util.Pair;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoTiltH extends BBasePoint {

    private final TreeMap<LocalDate, Pair<Double, Double>> mCommonObservations = new TreeMap<>();
    private final BTopoControlPoint mP1;
    private final BTopoControlPoint mP2;

    public BTopoTiltH(BTopoControlPoint p1, BTopoControlPoint p2) {
        mP1 = p1;
        mP2 = p2;
        setName("%s - %s".formatted(p1.getName(), p2.getName()));

        var map1 = new HashMap<LocalDate, Double>();
        var map2 = new HashMap<LocalDate, Double>();

        for (var o : p1.ext().getObservationsTimeFiltered()) {
            var z = o.getMeasuredZ();
            if (z != null) {
                map1.put(o.getDate().toLocalDate(), z);
            }
        }

        for (var o : p2.ext().getObservationsTimeFiltered()) {
            var z = o.getMeasuredZ();
            if (z != null) {
                map2.put(o.getDate().toLocalDate(), z);
            }
        }

        for (var entry : map1.entrySet()) {
            if (map2.containsKey(entry.getKey())) {
                mCommonObservations.put(entry.getKey(), new Pair<>(map1.get(entry.getKey()), map2.get(entry.getKey())));
            }
        }
    }

    public TreeMap<LocalDate, Pair<Double, Double>> getCommonObservations() {
        return mCommonObservations;
    }

    public LocalDate getDateFirst() {
        return mCommonObservations.firstKey();
    }

    public LocalDate getDateLast() {
        return mCommonObservations.lastKey();
    }

    public double getDelta() {
        var firstPair = mCommonObservations.firstEntry().getValue();
        var lastPair = mCommonObservations.lastEntry().getValue();
        var firstZ1 = firstPair.getKey();
        var firstZ2 = firstPair.getValue();
        var lastZ1 = lastPair.getKey();
        var lastZ2 = lastPair.getValue();
        double a = firstZ2 - firstZ1;
        double z = lastZ2 - lastZ1;

        return Math.abs(z - a) * 1000;
    }

    public double getDistancePlane() {
        var x1 = new Point2D(mP1.getZeroX(), mP1.getZeroY());
        var x2 = new Point2D(mP2.getZeroX(), mP2.getZeroY());

        return x1.distance(x2);
    }

    public double getDistanceHeight() {
        return Math.abs(mP2.getZeroZ() - mP1.getZeroZ());
    }

    public BTopoControlPoint getP1() {
        return mP1;
    }

    public BTopoControlPoint getP2() {
        return mP2;
    }

    public Double getTilt() {
        return getDelta() / getDistancePlane();
    }

}
