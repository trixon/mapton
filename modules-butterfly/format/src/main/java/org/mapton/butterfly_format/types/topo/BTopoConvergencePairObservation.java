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

import java.time.LocalDateTime;
import javafx.geometry.Point3D;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergencePairObservation {

    private final LocalDateTime mDate;
    private final Point3D mDelta;
    private final Point3D mO1;
    private final Point3D mO2;
    private final BTopoConvergencePair mPair;

    public BTopoConvergencePairObservation(BTopoConvergencePair pair, LocalDateTime date, Point3D o1, Point3D o2) {
        mPair = pair;
        mDate = date;
        mO1 = o1;
        mO2 = o2;
        mDelta = o2.subtract(o1);
    }

    public LocalDateTime getDate() {
        return mDate;
    }

    public Point3D getDelta() {
        return mDelta;
    }

    public double getDeltaDeltaDistanceComparedToFirst() {
        return getDeltaDistanceInPairForSameDate() - getFirstObservation().getDeltaDistanceInPairForSameDate();
    }

    public double getDeltaDistanceInPairForSameDate() {
//        return mDelta.distance(Point3D.ZERO);
        return mO1.distance(mO2);
    }

    public double getDeltaHInPairForSameDate() {
        return mDelta.getZ();
    }

    public double getDeltaRInPairForSameDate() {
        return Math.hypot(mDelta.getX(), mDelta.getY());
    }

    public Point3D getO1() {
        return mO1;
    }

    public Point3D getO2() {
        return mO2;
    }

    private BTopoConvergencePairObservation getFirstObservation() {
        return mPair.getObservations().getFirst();
    }

}
