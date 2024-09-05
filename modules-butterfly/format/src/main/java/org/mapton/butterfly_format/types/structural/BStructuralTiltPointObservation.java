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
package org.mapton.butterfly_format.types.structural;

import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BStructuralTiltPointObservation extends BTopoControlPointObservation {

    private Double m1;
    private Double m2;
    private Double m3;
    private Double m4;
    private Double m5;
    private Double m6;
    private Ext mExt;

    public Ext ext2() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getM1() {
        return m1;
    }

    public Double getM2() {
        return m2;
    }

    public Double getM3() {
        return m3;
    }

    public Double getM4() {
        return m4;
    }

    public Double getM5() {
        return m5;
    }

    public Double getM6() {
        return m6;
    }

    public void setM1(Double m1) {
        this.m1 = m1;
    }

    public void setM2(Double m2) {
        this.m2 = m2;
    }

    public void setM3(Double m3) {
        this.m3 = m3;
    }

    public void setM4(Double m4) {
        this.m4 = m4;
    }

    public void setM5(Double m5) {
        this.m5 = m5;
    }

    public void setM6(Double m6) {
        this.m6 = m6;
    }

    public class Ext {

        private BStructuralTiltPoint mParent;

        public BStructuralTiltPoint getParent() {
            return mParent;
        }

        public void setParent(BStructuralTiltPoint p) {
            mParent = p;
        }

    }

}
