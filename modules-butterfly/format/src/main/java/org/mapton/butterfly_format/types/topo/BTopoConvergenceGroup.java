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

import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergenceGroup extends BXyzPoint {

    private transient Ext mExt;
    private String ref;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public class Ext extends BXyzPoint.Ext<BTopoConvergenceObservation> {

        private BTopoControlPoint mAnchorPoint;
        private ArrayList<BTopoControlPoint> mControlPoints = new ArrayList<>();
        private final ArrayList<BTopoConvergencePair> mPairs = new ArrayList<>();

        @Override
        public int getAlarmLevel(BXyzPointObservation o) {
            return getAlarmLevel(BComponent.HEIGHT, o.getMeasuredX() / 1000d);
        }

        public BTopoControlPoint getAnchorPoint() {
            return mAnchorPoint;
        }

        public ArrayList<BTopoControlPoint> getControlPoints() {
            return mControlPoints;
        }

        public ArrayList<BTopoControlPoint> getControlPointsWithoutAnchor() {
            return new ArrayList<>(mControlPoints.stream().filter(p -> p != getAnchorPoint()).toList());
        }

        public BTopoConvergencePair getMaxDeltaDistanceOverTime() {
            BTopoConvergencePair storedPair = null;
            for (var pair : getPairs()) {
                if (storedPair == null || Math.abs(pair.getDeltaDistanceOverTime()) > Math.abs(storedPair.getDeltaDistanceOverTime())) {
                    storedPair = pair;
                }
            }

            return storedPair;
        }

        public BTopoConvergencePair getMaxDeltaROverTime() {
            BTopoConvergencePair storedPair = null;
            for (var pair : getPairs()) {
                if (storedPair == null || Math.abs(pair.getDeltaROverTime()) > Math.abs(storedPair.getDeltaROverTime())) {
                    storedPair = pair;
                }
            }

            return storedPair;
        }

        public BTopoConvergencePair getMaxDeltaZOverTime() {
            BTopoConvergencePair storedPair = null;
            for (var pair : getPairs()) {
                if (storedPair == null || Math.abs(pair.getDeltaZOverTime()) > Math.abs(storedPair.getDeltaZOverTime())) {
                    storedPair = pair;
                }
            }

            return storedPair;
        }

        public ArrayList<BTopoConvergencePair> getPairs() {
            return mPairs;
        }

        public boolean hasAnchorPoint() {
            return mAnchorPoint != null;
        }

        public void setControlPoints(ArrayList<BTopoControlPoint> controlPoints) {
            mControlPoints = controlPoints;
            if (controlPoints.isEmpty()) {
                return;
            }
            var maxDistance = 0.0;
            var totalDistance = 0.0;
            mAnchorPoint = null;

            for (var p1 : controlPoints) {
                var pp1 = new Point3D(p1.getZeroX(), p1.getZeroY(), p1.getZeroZ());
                var pointDistance = 0.0;
                for (var p2 : controlPoints) {
                    var pp2 = new Point3D(p2.getZeroX(), p2.getZeroY(), p2.getZeroZ());
                    pointDistance += pp1.distance(pp2);
                }
                totalDistance += pointDistance;
                if (pointDistance > maxDistance) {
                    maxDistance = pointDistance;
                    mAnchorPoint = p1;
                }
            }

            var avgDistance = (totalDistance - maxDistance) / controlPoints.size();
            if (maxDistance < avgDistance * 2) {
                mAnchorPoint = null;
            }
        }

    }

}
