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
import javafx.geometry.Point3D;
import javafx.util.Pair;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoPointPair extends BBasePoint {

    private final TreeMap<LocalDate, Pair<Point3D, Point3D>> mCommonObservations = new TreeMap<>();
    private final BTopoControlPoint mP1;
    private final BTopoControlPoint mP2;

    public BTopoPointPair(BTopoControlPoint p1, BTopoControlPoint p2) {
        this.mP1 = p1;
        this.mP2 = p2;

        setName("%s - %s".formatted(p1.getName(), p2.getName()));

        var map1 = createObservationMap(p1);
        var map2 = createObservationMap(p2);

        for (var entry : map1.entrySet()) {
            if (map2.containsKey(entry.getKey())) {
                mCommonObservations.put(entry.getKey(), new Pair<>(map1.get(entry.getKey()), map2.get(entry.getKey())));
            }
        }
    }

    public TreeMap<LocalDate, Pair<Point3D, Point3D>> getCommonObservations() {
        return mCommonObservations;
    }

    public LocalDate getDateFirst() {
        return mCommonObservations.firstKey();
    }

    public LocalDate getDateLast() {
        return mCommonObservations.lastKey();
    }

    public Point3D getDeltaPair3D() {
        var firstPair = mCommonObservations.firstEntry().getValue();
        var lastPair = mCommonObservations.lastEntry().getValue();
        var firstPoint1 = firstPair.getKey();
        var firstPoint2 = firstPair.getValue();
        var lastPoint1 = lastPair.getKey();
        var lastPoint2 = lastPair.getValue();

        var deltaPoint1 = lastPoint1.subtract(firstPoint1);
        var deltaPoint2 = lastPoint2.subtract(firstPoint2);

        return deltaPoint2.subtract(deltaPoint1);
    }

    public double getDistanceHeight() {
        return Math.abs(mP2.getZeroZ() - mP1.getZeroZ());
    }

    public double getDistancePlane() {
        var x1 = new Point2D(mP1.getZeroX(), mP1.getZeroY());
        var x2 = new Point2D(mP2.getZeroX(), mP2.getZeroY());

        return x1.distance(x2);
    }

    public BTopoControlPoint getP1() {
        return mP1;
    }

    public BTopoControlPoint getP2() {
        return mP2;
    }

    public double getPartialDiffR() {
        return Math.hypot(getDeltaPair3D().getX(), getDeltaPair3D().getY());
    }

    public double getPartialDiffZ() {
        return getDeltaPair3D().getZ();
    }

    public Double getRAngleDeg() {
        return Math.toDegrees(getRAngleRad());
    }

    public Double getRAngleGon() {
        return getRAngleDeg() * 200.0 / 180.0;
    }

    public Double getRAngleRad() {
        return Math.tanh(getRQuota());
    }

    public Double getRPerMille() {
        return getRQuota() * 1000;
    }

    public Double getRPercentage() {
        return getRQuota() * 100;
    }

    public Double getRQuota() {
        return getPartialDiffR() / getDistanceHeight();
    }

    public Double getZAngleDeg() {
        return Math.toDegrees(getZAngleRad());
    }

    public Double getZAngleGon() {
        return getZAngleDeg() * 200.0 / 180.0;
    }

    public Double getZAngleRad() {
        return Math.tanh(getZQuota());
    }

    public Double getZPerMille() {
        return getZQuota() * 1000;
    }

    public Double getZPercentage() {
        return getZQuota() * 100;
    }

    public Double getZQuota() {
        return getPartialDiffZ() / getDistancePlane();
    }

    private HashMap<LocalDate, Point3D> createObservationMap(BTopoControlPoint p) {
        var map1 = new HashMap<LocalDate, Point3D>();
        if (p.getDimension() == BDimension._1d) {
            for (var o : p.ext().getObservationsTimeFiltered()) {
                var z = o.getMeasuredZ();
                if (z != null) {
                    map1.put(o.getDate().toLocalDate(), new Point3D(0, 0, z));
                }
            }
        } else if (p.getDimension() == BDimension._3d) {
            for (var o : p.ext().getObservationsTimeFiltered()) {
                if (ObjectUtils.allNotNull(o.getMeasuredX(), o.getMeasuredY(), o.getMeasuredZ())) {
                    map1.put(o.getDate().toLocalDate(), new Point3D(o.getMeasuredX(), o.getMeasuredY(), o.getMeasuredZ()));
                }
            }
        }

        return map1;
    }

}
