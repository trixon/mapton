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

import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BStructuralLoadCellPoint extends BXyzPoint {

    private Double directionX;
    private transient Ext mExt;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getDirectionX() {
        return directionX;
    }

    public void setDirectionX(Double directionX) {
        this.directionX = directionX;
    }

    public class Ext extends BXyzPoint.Ext<BStructuralLoadCellPointObservation> {

        public String getDeltaRolling() {
            return getDelta(deltaRolling());
        }

        public String getDeltaZero() {
            return getDelta(deltaZero());
        }

        private String getDelta(Delta delta) {
            var s = "kN=%.2f".formatted(delta.getDeltaZ());

            return s;
        }
    }

}
