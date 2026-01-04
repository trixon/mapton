/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_format.types.rock;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "name",
    "group",
    "comment",
    "lat",
    "lon"
})
public class BRockEarthquake extends BXyzPoint {

    private transient Ext mExt;
    private Integer mSig;
    private Double mag;
    private String magType;

    public BRockEarthquake() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getMag() {
        return mag;
    }

    public String getMagType() {
        return magType;
    }

    public Integer getSig() {
        return mSig;
    }

    public void setMag(Double mag) {
        this.mag = mag;
    }

    public void setMagType(String magType) {
        this.magType = magType;
    }

    public void setSig(Integer sig) {
        this.mSig = sig;
    }

    public class Ext extends BXyzPoint.Ext<BRockBlastObservation> {

    }

}
