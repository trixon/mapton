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
import java.util.stream.Collectors;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergenceGroup extends BTopoControlPoint {

    private transient Ext mExt;
    private double mLimit;
    private String mRef;

    public Ext ext2() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public double getLimit() {
        return mLimit;
    }

    public String getRef() {
        return mRef;
    }

    public void setLimit(double limit) {
        mLimit = limit;
    }

    public void setRef(String ref) {
        mRef = ref;
    }

    public class Ext extends BXyzPoint.Ext<BTopoControlPointObservation> {

        private BTopoControlPoint mAnchorPoint;
        private ArrayList<BTopoControlPoint> mControlPoints = new ArrayList<>();
        private ArrayList<BTopoConvergencePair> mPairs = new ArrayList<>();

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

        public ArrayList<Point2D> getProjected2dCoordinates() {
            var viewDistance = 50.0;
            var points2d = new ArrayList<Point2D>();
            var points3d = getControlPointsWithoutAnchor().stream()
                    .map(p1 -> new Point3D(p1.getZeroX(), p1.getZeroY(), p1.getZeroZ()))
                    .toList();

            for (var point : points3d) {
                var x2d = (point.getX() * viewDistance) / point.getY();
                var y2d = (point.getZ() * viewDistance) / point.getY();
                points2d.add(new Point2D(x2d, y2d));
            }

            var avgX = points3d.stream().mapToDouble(p -> p.getX()).average().orElse(0.0);
            var avgY = points3d.stream().mapToDouble(p -> p.getY()).average().orElse(0.0);
            points2d = points2d.stream()
                    .map(p -> {
                        return p.subtract(avgX, avgY);
                    }).collect(Collectors.toCollection(ArrayList::new));

            return points2d;
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
