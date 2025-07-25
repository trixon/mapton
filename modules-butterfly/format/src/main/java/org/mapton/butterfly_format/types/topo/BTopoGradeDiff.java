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

import javafx.geometry.Point3D;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoGradeDiff {

    private final Point3D mDeltaPairPoint3d;
    private final BTopoGrade mGrade;
    private final BTopoGradeObservation mObservation;
    private final BTopoGradeObservation mReferenceObservation;

    public BTopoGradeDiff(BTopoGrade grade, BTopoGradeObservation referenceObservation, BTopoGradeObservation observation) {
        mGrade = grade;
        mReferenceObservation = referenceObservation;
        mObservation = observation;

        var referencePoint1 = referenceObservation.getPoint3d1();
        var referencePoint2 = referenceObservation.getPoint3d2();
        var point1 = observation.getPoint3d1();
        var point2 = observation.getPoint3d2();

        var deltaPoint1 = point1.subtract(referencePoint1);
        var deltaPoint2 = point2.subtract(referencePoint2);

        mDeltaPairPoint3d = deltaPoint2.subtract(deltaPoint1);
    }

    public Double getBearing() {
        var b = MathHelper.azimuthToDegrees(mDeltaPairPoint3d.getY(), mDeltaPairPoint3d.getX()) - 180;
        if (b < 0) {
            return b + 360;
        } else if (b > 360) {
            return b - 360;
        } else {
            return b;
        }
    }

    public double getPartialDiffR() {
        return Math.hypot(mDeltaPairPoint3d.getX(), mDeltaPairPoint3d.getY());
    }

    public double getPartialDiffZ() {
        return mDeltaPairPoint3d.getZ();
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
        return getPartialDiffR() / mGrade.getDistanceHeight();
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
        return getPartialDiffZ() / mGrade.getDistancePlane();
    }

}
