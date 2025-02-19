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

import org.apache.commons.lang3.ObjectUtils;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BXyzPointObservation extends BBaseControlPointObservation {

    private transient Ext mExt;
    private Double measuredX;
    private Double measuredY;
    private Double measuredZ;

    public BXyzPointObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getMeasuredX() {
        return measuredX;
    }

    public Double getMeasuredY() {
        return measuredY;
    }

    public Double getMeasuredZ() {
        return measuredZ;
    }

    public void setMeasuredX(Double measuredX) {
        this.measuredX = measuredX;
    }

    public void setMeasuredY(Double measuredY) {
        this.measuredY = measuredY;
    }

    public void setMeasuredZ(Double measuredZ) {
        this.measuredZ = measuredZ;
    }

    public class Ext<T> {

        private Double mDeltaX;
        private Double mDeltaY;
        private Double mDeltaZ;
        private T mParent;

        public T getParent() {
            return mParent;
        }

        public void setParent(T parent) {
            mParent = parent;
        }

        public Double getBearing() {
            if (ObjectUtils.anyNull(getDeltaX(), getDeltaY())) {
                return null;
            } else {
                return MathHelper.azimuthToDegrees(getDeltaY(), getDeltaX());
            }
        }

        public Double getDelta() {
            Double d2;
            if (ObjectUtils.allNotNull(getDeltaX(), getDeltaY())) {
                d2 = Math.hypot(getDeltaX(), getDeltaY());
            } else {
                return getDeltaZ();
            }

            if (getDeltaZ() == null) {
                return d2;
            } else {
                return Math.hypot(getDeltaZ(), d2);
            }
        }

        public Double getDelta2d() {
            Double deltaX = getDeltaX();
            Double deltaY = getDeltaY();

            if (ObjectUtils.allNotNull(deltaX, deltaY)) {
                return Math.hypot(deltaX, deltaY);
            } else {
                return null;
            }
        }

        public Double getDelta3d() {
            Double delta2d = getDelta2d();
            Double deltaZ = getDeltaZ();

            if (ObjectUtils.allNotNull(delta2d, deltaZ)) {
                return Math.hypot(delta2d, deltaZ) * MathHelper.sign(deltaZ);
            } else {
                return null;
            }
        }

        public Double getDeltaX() {
            return mDeltaX;
        }

        public Double getDeltaY() {
            return mDeltaY;
        }

        public Double getDeltaZ() {
            return mDeltaZ;
        }

        public void setDeltaX(Double deltaX) {
            this.mDeltaX = deltaX;
        }

        public void setDeltaY(Double deltaY) {
            this.mDeltaY = deltaY;
        }

        public void setDeltaZ(Double deltaZ) {
            this.mDeltaZ = deltaZ;
        }

    }
}
