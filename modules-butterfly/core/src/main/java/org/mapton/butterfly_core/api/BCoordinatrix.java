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
import java.util.concurrent.ConcurrentHashMap;
import javafx.geometry.Point3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.mapton.api.MCooTrans;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BCoordinatrix {

    private static final ConcurrentHashMap<BBasePoint, MLatLon> sPointToLatLon = new ConcurrentHashMap();
    private static final ConcurrentHashMap<BXyzPoint, Position> sPositionWW2d = new ConcurrentHashMap();
    private static final ConcurrentHashMap<BXyzPoint, Position> sPositionWW3d = new ConcurrentHashMap();

    public static void clear() {
        sPointToLatLon.clear();
        sPositionWW2d.clear();
        sPositionWW3d.clear();
    }

    public static MCooTrans getCooTrans() {
        return MCooTrans.getCooTrans(MOptions.getInstance().getMapCooTransName());
    }

    public static MLatLon toLatLon(BBasePoint p) {
        return sPointToLatLon.computeIfAbsent(p, k -> new MLatLon(k.getLat(), k.getLon()));
    }

    public static Point3D toLocalFromPosition(Position p) {
        var local = getCooTrans().fromWgs84(p.getLatitude().degrees, p.getLongitude().degrees);
        return new Point3D(local.getX(), local.getY(), p.elevation);
    }

    public static Vector3D toVector(Point3D p) {
        return Vector3D.of(p.getX(), p.getY(), p.getZ());
    }

    public static Position toPositionWW2d(BXyzPoint p) {
        return sPositionWW2d.computeIfAbsent(p, k -> WWHelper.positionFromLatLon(toLatLon(p)));
    }

    public static Position toPositionWW3d(Vector3D p) {
        var wgs = getCooTrans().toWgs84(p.getY(), p.getX());
        var position = Position.fromDegrees(wgs.getY(), wgs.getX(), p.getZ());

        return position;
    }

    public static Position toPositionWW3d(BXyzPoint p) {
        return sPositionWW3d.computeIfAbsent(p, k -> WWHelper.positionFromLatLon(toLatLon(p), p.getZeroZ()));
    }

    private BCoordinatrix() {
    }
}
