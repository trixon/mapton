/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.geom.Position;
import java.util.HashMap;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BCoordinatrix {

    private static final HashMap<BXyzPoint, MLatLon> sPointToLatLon = new HashMap();
    private static final HashMap<BXyzPoint, Position> sPositionWW2d = new HashMap();
    private static final HashMap<BXyzPoint, Position> sPositionWW3d = new HashMap();

    public static void clear() {
        sPointToLatLon.clear();
        sPositionWW2d.clear();
        sPositionWW3d.clear();
    }

    public static MLatLon toLatLon(BXyzPoint p) {
        return sPointToLatLon.computeIfAbsent(p, k -> new MLatLon(k.getLat(), k.getLon()));
    }

    public static Position toPositionWW2d(BXyzPoint p) {
        return sPositionWW2d.computeIfAbsent(p, k -> WWHelper.positionFromLatLon(toLatLon(p)));
    }

    public static Position toPositionWW3d(BXyzPoint p) {
        return sPositionWW3d.computeIfAbsent(p, k -> WWHelper.positionFromLatLon(toLatLon(p), p.getZeroZ()));
    }

    private BCoordinatrix() {
    }
}
