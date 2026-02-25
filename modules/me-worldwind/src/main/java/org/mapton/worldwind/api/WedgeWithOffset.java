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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Wedge;

/**
 *
 * @author Patrik Karlström
 */
public class WedgeWithOffset extends Wedge {

    public WedgeWithOffset(Angle angle, Position centerPosition, double heightUp, double heightDown, double radius) {
        super(centerPosition, angle, heightUp + heightDown, radius);
        setCenterPosition(WWHelper.positionFromPosition(centerPosition, centerPosition.getElevation() + (heightUp - heightDown) / 2.0));
    }

}
