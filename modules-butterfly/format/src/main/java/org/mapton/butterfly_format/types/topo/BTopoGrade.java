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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.TreeMap;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoGrade extends BBasePoint {

    private final BAxis mAxis;
    private final TreeMap<LocalDate, BTopoGradeObservation> mCommonObservations = new TreeMap<>();
    private Ext mExt;
    private final BTopoControlPoint mP1;
    private final BTopoControlPoint mP2;

    public BTopoGrade(BAxis axis, BTopoControlPoint p1, BTopoControlPoint p2) {
        mAxis = axis;
        mP1 = p1;
        mP2 = p2;

        setName("%s → %s".formatted(p1.getName(), p2.getName()));

        var map1 = createObservationMap(p1);
        var map2 = createObservationMap(p2);

        for (var entry : map1.entrySet()) {
            if (map2.containsKey(entry.getKey())) {
                mCommonObservations.put(entry.getKey(), new BTopoGradeObservation(this, map1.get(entry.getKey()), map2.get(entry.getKey())));
            }
        }
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BAxis getAxis() {
        return mAxis;
    }

    public TreeMap<LocalDate, BTopoGradeObservation> getCommonObservations() {
        return mCommonObservations;
    }

    public double getDistanceHeight() {
        return Math.abs(mP2.getZeroZ() - mP1.getZeroZ());
    }

    public double getDistancePlane() {
        var x1 = new Point2D(mP1.getZeroX(), mP1.getZeroY());
        var x2 = new Point2D(mP2.getZeroX(), mP2.getZeroY());

        return x1.distance(x2);
    }

    public LocalDate getFirstDate() {
        return mCommonObservations.firstKey();
    }

    public BTopoGradeObservation getFirstObservation() {
        return mCommonObservations.firstEntry().getValue();
    }

    public LocalDate getLastDate() {
        return mCommonObservations.lastKey();
    }

    public BTopoGradeObservation getLastObservation() {
        return mCommonObservations.lastEntry().getValue();
    }

    public BTopoControlPoint getP1() {
        return mP1;
    }

    public BTopoControlPoint getP2() {
        return mP2;
    }

    public String getPeriod() {
        return "%s → %s".formatted(getFirstDate(), getLastDate());
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

    public class Ext extends BBase.Ext<BTopoGradeObservation> {

        public Ext() {
        }

        public BTopoGradeDiff getDiff() {
            return getDiff(getFirstObservation(), getLastObservation());
        }

        public BTopoGradeDiff getDiff(BTopoGradeObservation referenceObservation, BTopoGradeObservation observation) {
            return new BTopoGradeDiff(BTopoGrade.this, referenceObservation, observation);
        }

        public long getNumOfCommonDays() {
            return ChronoUnit.DAYS.between(getFirstDate(), getLastDate());
        }

        public int getNumOfCommonObservations() {
            return mCommonObservations.size();
        }

        public long getNumOfDaysSinceLast() {
            return ChronoUnit.DAYS.between(getLastDate(), LocalDate.now());
        }

    }

}
