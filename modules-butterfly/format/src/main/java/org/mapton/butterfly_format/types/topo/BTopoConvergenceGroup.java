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

        private ArrayList<BTopoControlPoint> mControlPoints = new ArrayList<>();
        private ArrayList<BTopoConvergencePair> mPairs = new ArrayList<>();

        public ArrayList<BTopoControlPoint> getControlPoints() {
            return mControlPoints;
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

//        @Override
//        public int getNumOfObservations() {
        ////            return getObservationsAllRaw().stream()
////                    .map(o -> o.getDate().toString())
////                    .collect(Collectors.toSet()).size();
//            return 1 + getObservationsAllRaw().size();
//        }
//
//        @Override
//        public int getNumOfObservationsFiltered() {
//            return getObservationsTimeFiltered().stream()
//                    .map(o -> o.getDate().toString())
//                    .collect(Collectors.toSet()).size();
//        }

        public ArrayList<BTopoConvergencePair> getPairs() {
            return mPairs;
        }

        public void setControlPoints(ArrayList<BTopoControlPoint> controlPoints) {
            mControlPoints = controlPoints;
        }

    }
}
