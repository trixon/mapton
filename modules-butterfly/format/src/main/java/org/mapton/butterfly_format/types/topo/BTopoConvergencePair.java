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
import org.mapton.butterfly_format.types.BBaseControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergencePair extends BBaseControlPoint {

    private BTopoConvergenceGroup mConvergenceGroup;
    private double mDistance;
    private BTopoControlPoint mP1;
    private BTopoControlPoint mP2;
    private Point3D mDelta;

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

    public double getDeltaR() {
        return Math.hypot(mDelta.getX(), mDelta.getY());
    }

    public double getDeltaZ() {
        return Math.abs(mDelta.getZ());
    }

    public BTopoConvergenceGroup getConvergenceGroup() {
        return mConvergenceGroup;
    }

    public Point3D getDelta() {
        return mDelta;
    }

    public double getDistance() {
        return mDistance;
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

    public void setP1(BTopoControlPoint p1) {
        this.mP1 = p1;
    }

    public void setP2(BTopoControlPoint p2) {
        this.mP2 = p2;
    }
}
