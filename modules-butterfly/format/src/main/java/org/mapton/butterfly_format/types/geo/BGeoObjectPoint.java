/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_format.types.geo;

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
public abstract class BGeoObjectPoint extends BXyzPoint {

    private Double mDepth;
    private Double mDiameter;
    private boolean mHasDepth;
    private boolean mHasDiameter;
    private boolean mHasZ;

    public BGeoObjectPoint() {
    }

    public Double getDepth() {
        return mDepth;
    }

    public Double getDiameter() {
        return mDiameter;
    }

    public boolean hasDepth() {
        return mHasDepth;
    }

    public boolean hasDiameter() {
        return mHasDiameter;
    }

    public boolean hasZ() {
        return mHasZ;
    }

    public void setDepth(Double depth) {
        this.mDepth = depth;
    }

    public void setDiameter(Double diameter) {
        this.mDiameter = diameter;
    }

    public void setHasDepth(boolean hasDepth) {
        this.mHasDepth = hasDepth;
    }

    public void setHasDiameter(boolean hasDiameter) {
        this.mHasDiameter = hasDiameter;
    }

    public void setHasZ(boolean hasZ) {
        this.mHasZ = hasZ;
    }
}
