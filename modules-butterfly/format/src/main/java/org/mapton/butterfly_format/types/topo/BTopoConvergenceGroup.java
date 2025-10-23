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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;
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
            try {
                var delta = o.ext().getDeltaZ();
                return getAlarmLevel(BComponent.HEIGHT, delta / 1000d);
            } catch (Exception e) {
                return -1;
            }
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

        public String getDeltaString(String prefix, Function<BTopoConvergenceObservation, Double> function) {
            var pair = getPairWithLargestDiff(function);
            if (pair == null) {
                return "";
            } else {
                return pair.ext().getDeltaString(prefix, function);
            }
        }

        public Double getDeltaValue(Function<BTopoConvergenceObservation, Double> function) {
            var pair = getPairWithLargestDiff(function);
            if (pair == null) {
                return null;
            } else {
                var o = pair.ext().getObservationFilteredLast();
                return function.apply(o);
            }
        }

        public BTopoConvergencePair getPairWithLargestDiff(Function<BTopoConvergenceObservation, Double> function) {
            var observations = ext().getPairs().stream()
                    .flatMap(pair -> pair.ext().getObservationsTimeFiltered().stream()).toList();

            var maxDateOpt = observations.stream()
                    .map(BTopoConvergenceObservation::getDate)
                    .max(LocalDateTime::compareTo);

            if (maxDateOpt.isPresent()) {
                var maxDate = maxDateOpt.get();
                var observation = observations.stream()
                        .filter(value -> value.getDate().equals(maxDate))
                        .max(Comparator.comparingDouble(o -> Math.abs(function.apply(o)))).orElse(null);
                return observation.ext().getPair();
            }

            return null;
        }

        public ArrayList<BTopoConvergencePair> getPairs() {
            return mPairs;
        }

        public Stream<BTopoConvergencePair> getPairsOrderedByDeltaDesc(Function<BTopoConvergenceObservation, Double> function, Integer... limits) {
            int limit;
            if (limits == null || limits[0] == null) {
                limit = Integer.MAX_VALUE;
            } else {
                limit = limits[0];
            }

            return getPairs().stream()
                    .sorted((p1, p2) -> Double.compare(
                    Math.abs(p2.ext().getDelta(function)),
                    Math.abs(p1.ext().getDelta(function))))
                    .limit(limit);

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
