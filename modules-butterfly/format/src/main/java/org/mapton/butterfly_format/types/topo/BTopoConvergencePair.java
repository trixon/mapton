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

import java.util.function.Function;
import javafx.geometry.Point3D;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergencePair extends BXyzPoint {

    private BTopoConvergenceGroup mConvergenceGroup;
    private final Point3D mDelta;
    private transient Ext mExt;
    private BTopoControlPoint mP1;
    private BTopoControlPoint mP2;

    public BTopoConvergencePair(BTopoConvergenceGroup group, BTopoControlPoint p1, BTopoControlPoint p2, double offset) {
        mConvergenceGroup = group;
        mP1 = p1;
        mP2 = p2;
        setName("%s → %s".formatted(p1.getName(), p2.getName()));

        var p3d1 = new Point3D(p1.getZeroX(), p1.getZeroY(), p1.getZeroZ());
        var p3d2 = new Point3D(p2.getZeroX(), p2.getZeroY(), p2.getZeroZ());
        mDelta = p3d2.subtract(p3d1);

        var midPoint = p3d1.midpoint(p3d2);
        setZeroX(midPoint.getX());
        setZeroY(midPoint.getY());
        setZeroZ(midPoint.getZ() + offset);
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    @Override
    public String getAlarm1Id() {
        return mConvergenceGroup.getAlarm1Id();
    }

    @Override
    public String getAlarm2Id() {
        return mConvergenceGroup.getAlarm2Id();
    }

    @Override
    public Butterfly getButterfly() {
        return mConvergenceGroup.getButterfly();
    }

    public BTopoConvergenceGroup getConvergenceGroup() {
        return mConvergenceGroup;
    }

    public Point3D getDelta() {
        return mDelta;
    }

    public BTopoControlPoint getP1() {
        return mP1;
    }

    public BTopoControlPoint getP2() {
        return mP2;
    }

    public String getSimpleName() {
        return Strings.CI.remove(getName(), mConvergenceGroup.getName());
    }

    public void setConvergenceGroup(BTopoConvergenceGroup convergenceGroup) {
        mConvergenceGroup = convergenceGroup;
    }

    public void setP1(BTopoControlPoint p1) {
        mP1 = p1;
    }

    public void setP2(BTopoControlPoint p2) {
        mP2 = p2;
    }

    public class Ext extends BXyzPoint.Ext<BTopoConvergenceObservation> {

        public int getAlarmLevel(Function<BTopoConvergenceObservation, Double> function) {
            try {
                return getAlarmLevel(BComponent.HEIGHT, getDelta(function) / 1000d);
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public int getAlarmLevel(BXyzPointObservation o) {
            try {
                return getAlarmLevel(BComponent.HEIGHT, o.ext().getDelta1d() / 1000d);
            } catch (Exception e) {
                return -1;
            }
        }

        public Double getDelta(Function<BTopoConvergenceObservation, Double> function) {
            try {
                return function.apply(getObservationsTimeFiltered().getLast());
            } catch (Exception e) {
                return null;
            }
        }

        public String getDeltaString(String prefix, Function<BTopoConvergenceObservation, Double> function) {
            var diff = "";
            try {
                var o = ext().getObservationFilteredLast();
                diff = "%s: %s   %+.1f".formatted(
                        prefix,
                        getSimpleName(),
                        function.apply(o)
                );
            } catch (Exception e) {
            }

            return diff;
        }

        public String getShortName() {
            return Strings.CI.remove(getName(), getConvergenceGroup().getName());
        }

    }
}
