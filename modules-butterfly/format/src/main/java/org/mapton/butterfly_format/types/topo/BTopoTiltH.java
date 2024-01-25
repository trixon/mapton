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

/**
 *
 * @author Patrik Karlström
 */
public class BTopoTiltH extends BTopoTiltBase {

    public BTopoTiltH(BTopoControlPoint p1, BTopoControlPoint p2) {
        super(p1, p2);

    }

    public Double getAngleDeg() {
        return Math.toDegrees(getAngleRad());
    }

    public Double getAngleGon() {
        return getAngleDeg() * 200.0 / 180.0;
    }

    public Double getAngleRad() {
        return Math.tanh(getQuota());
    }

    public Double getPerMille() {
        return getQuota() * 1000;
    }

    public Double getPercentage() {
        return getQuota() * 100;
    }

    public Double getQuota() {
        return getDeltaPair3D().getZ() / getDistancePlane();
    }

}
