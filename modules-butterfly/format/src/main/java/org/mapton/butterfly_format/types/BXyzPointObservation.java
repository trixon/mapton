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
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import static java.lang.Double.NaN;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BXyzPointObservation extends BBaseControlPointObservation {

    private transient Ext mExt;
    @JsonSetter(nulls = Nulls.SKIP)
    private double measuredX = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double measuredY = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double measuredZ = Double.NaN;

    public BXyzPointObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public double getMeasuredX() {
        return measuredX;
    }

    public double getMeasuredY() {
        return measuredY;
    }

    public double getMeasuredZ() {
        return measuredZ;
    }

    public void setMeasuredX(double measuredX) {
        this.measuredX = measuredX;
    }

    public void setMeasuredY(double measuredY) {
        this.measuredY = measuredY;
    }

    public void setMeasuredZ(double measuredZ) {
        this.measuredZ = measuredZ;
    }

    public class Ext<T> {

        private double mAccuX = Double.NaN;
        private double mAccuY = Double.NaN;
        private double mAccuZ = Double.NaN;
        private double mDeltaX = Double.NaN;
        private double mDeltaY = Double.NaN;
        private double mDeltaZ = Double.NaN;
        private T mParent;

        public double getAccuX() {
            return mAccuX;
        }

        public double getAccuY() {
            return mAccuY;
        }

        public double getAccuZ() {
            return mAccuZ;
        }

        public double getBearing() {
            if (Butterfly.anyNaN(getDeltaX(), getDeltaY())) {
                return NaN;
            } else {
                return MathHelper.azimuthToDegrees(getDeltaY(), getDeltaX());
            }
        }

        public double getDelta() {
            double d2;
            if (Butterfly.allNotNaN(getDeltaX(), getDeltaY())) {
                d2 = Math.hypot(getDeltaX(), getDeltaY());
            } else {
                return getDeltaZ();
            }

            if (Double.isNaN(getDeltaZ())) {
                return d2;
            } else {
                return Math.hypot(getDeltaZ(), d2);
            }
        }

        public double getDelta1d() {
            return mDeltaZ;
        }

        public double getDelta2d() {
            double deltaX = getDeltaX();
            double deltaY = getDeltaY();

            if (Butterfly.allNotNaN(deltaX, deltaY)) {
                return Math.hypot(deltaX, deltaY);
            } else {
                return NaN;
            }
        }

        public double getDelta3d() {
            double delta2d = getDelta2d();
            double deltaZ = getDeltaZ();

            if (Butterfly.allNotNaN(delta2d, deltaZ)) {
                return Math.hypot(delta2d, deltaZ) * MathHelper.sign(deltaZ);
            } else {
                return NaN;
            }
        }

        public double getDeltaX() {
            return mDeltaX;
        }

        public double getDeltaY() {
            return mDeltaY;
        }

        public double getDeltaZ() {
            return mDeltaZ;
        }

        public T getParent() {
            return mParent;
        }

        public void setAccuX(double accuX) {
            mAccuX = accuX;
        }

        public void setAccuY(double accuY) {
            mAccuY = accuY;
        }

        public void setAccuZ(double accuZ) {
            mAccuZ = accuZ;
        }

        public void setDeltaX(double deltaX) {
            mDeltaX = deltaX;
        }

        public void setDeltaY(double deltaY) {
            mDeltaY = deltaY;
        }

        public void setDeltaZ(double deltaZ) {
            mDeltaZ = deltaZ;
        }

        public void setParent(T parent) {
            mParent = parent;
        }

    }
}
