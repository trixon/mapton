/* 
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.core;

import javafx.geometry.Point2D;
import org.openide.util.lookup.ServiceProvider;
import org.mapton.api.MDecDegDMS;
import org.mapton.api.MBounds;
import org.mapton.api.MCooTrans;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCooTrans.class)
public class Wgs84DMS implements MCooTrans {

    private final MBounds mBoundsProjected = new MBounds(-180, -90, 180, 90);
    private final MBounds mBoundsWgs84 = new MBounds(-180, -90, 180, 90);

    @Override
    public Point2D fromWgs84(double latitude, double longitude) {
        return new Point2D(longitude, latitude);
    }

    @Override
    public MBounds getBoundsProjected() {
        return mBoundsProjected;
    }

    @Override
    public MBounds getBoundsWgs84() {
        return mBoundsWgs84;
    }

    @Override
    public double getLatitude(double latitude, double longitude) {
        return latitude;
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        MDecDegDMS dddms = new MDecDegDMS(latitude, true);

        return dddms.format("%2d°%2d'%4.1f\"%s", "N", "S");
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return longitude;
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        MDecDegDMS dddms = new MDecDegDMS(longitude, true);

        return dddms.format("%3d°%2d'%4.1f\"%s", "E", "W");
    }

    @Override
    public String getName() {
        return "WGS 84 DMS";
    }

    @Override
    public String getString(double latitude, double longitude) {
        return String.format("%s %s", getLatitudeString(latitude, longitude), getLongitudeString(latitude, longitude));
    }

    @Override
    public boolean isWithinProjectedBounds(double latitude, double longitude) {
        return mBoundsProjected.contains(longitude, latitude);
    }

    @Override
    public boolean isWithinWgs84Bounds(double latitude, double longitude) {
        return mBoundsWgs84.contains(longitude, latitude);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Point2D toWgs84(double latitude, double longitude) {
        return new Point2D(longitude, latitude);
    }
}
