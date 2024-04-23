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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.mapton.butterfly_format.types.BBaseControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergencePair extends BBaseControlPoint {

    private BTopoConvergenceGroup mConvergenceGroup;
    private final Point3D mDelta;
    private final double mDistance;
    private Ext mExt;
    private ArrayList<BTopoConvergencePairObservation> mObservations = new ArrayList<>();
    private BTopoControlPoint mP1;
    private BTopoControlPoint mP2;

    public BTopoConvergencePair(BTopoConvergenceGroup group, BTopoControlPoint p1, BTopoControlPoint p2) {
        mConvergenceGroup = group;
        mP1 = p1;
        mP2 = p2;
        setName("%s → %s".formatted(p1.getName(), p2.getName()));

        var pp1 = new Point3D(p1.getZeroX(), p1.getZeroY(), p1.getZeroZ());
        var pp2 = new Point3D(p2.getZeroX(), p2.getZeroY(), p2.getZeroZ());
        mDelta = pp2.subtract(pp1);
        mDistance = pp1.distance(pp2);
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BTopoConvergenceGroup getConvergenceGroup() {
        return mConvergenceGroup;
    }

    public Point3D getDelta() {
        return mDelta;
    }

    public double getDeltaDistanceOverTime() {
        if (mObservations.isEmpty()) {
            return 0;
        } else {
            return mObservations.getLast().getDeltaDistanceInPairForSameDate()
                    - mObservations.getFirst().getDeltaDistanceInPairForSameDate();
        }
    }

    public double getDeltaR() {
        return Math.hypot(mDelta.getX(), mDelta.getY());
    }

    public double getDeltaROverTime() {
        if (mObservations.isEmpty()) {
            return 0;
        } else {
            mObservations.getLast();
            return mObservations.getLast().getDeltaRInPairForSameDate()
                    - mObservations.getFirst().getDeltaRInPairForSameDate();
        }
    }

    public double getDeltaZ() {
        return Math.abs(mDelta.getZ());
    }

    public double getDeltaZOverTime() {
        if (mObservations.isEmpty()) {
            return 0;
        } else {
            mObservations.getLast();
            return mObservations.getLast().getDeltaHInPairForSameDate()
                    - mObservations.getFirst().getDeltaHInPairForSameDate();
        }
    }

    public double getDistance() {
        return mDistance;
    }

    public ArrayList<BTopoConvergencePairObservation> getObservations() {
        return mObservations;
    }

    public BTopoControlPoint getP1() {
        return mP1;
    }

    public BTopoControlPoint getP2() {
        return mP2;
    }

    public void setConvergenceGroup(BTopoConvergenceGroup convergenceGroup) {
        this.mConvergenceGroup = convergenceGroup;
    }

    public void setObservations(ArrayList<BTopoConvergencePairObservation> observations) {
        this.mObservations = observations;
    }

    public void setP1(BTopoControlPoint p1) {
        this.mP1 = p1;
    }

    public void setP2(BTopoControlPoint p2) {
        this.mP2 = p2;
    }

    public class Ext {

        public long getAge(ChronoUnit chronoUnit) {
            if (mObservations.isEmpty()) {
                return -1L;
            } else {
                return chronoUnit.between(mObservations.getLast().getDate().toLocalDate(), LocalDate.now());
            }
        }

        public LocalDateTime getFirstDate() {
            if (mObservations != null && !mObservations.isEmpty()) {
                return mObservations.getFirst().getDate();
            } else {
                return null;
            }
        }

        public LocalDateTime getLastDate() {
            if (mObservations != null && !mObservations.isEmpty()) {
                return mObservations.getLast().getDate();
            } else {
                return null;
            }
        }

    }
}
