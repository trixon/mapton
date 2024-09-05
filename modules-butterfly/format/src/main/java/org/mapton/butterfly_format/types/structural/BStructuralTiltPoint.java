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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BStructuralTiltPoint extends BBaseControlPoint {

    private Double zeroX2;
    private Double zeroY2;
    private Double zeroZ2;

    @JsonIgnore
    private Ext mExt;

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getZeroX2() {
        return zeroX2;
    }

    public Double getZeroY2() {
        return zeroY2;
    }

    public Double getZeroZ2() {
        return zeroZ2;
    }

    public void setZeroX2(Double zeroX2) {
        this.zeroX2 = zeroX2;
    }

    public void setZeroY2(Double zeroY2) {
        this.zeroY2 = zeroY2;
    }

    public void setZeroZ2(Double zeroZ2) {
        this.zeroZ2 = zeroZ2;
    }

    public class Ext extends BBase.Ext<BBaseControlPointObservation> {

    }

}