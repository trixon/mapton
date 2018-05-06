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
package se.trixon.mapton.core.map;

import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.core.api.CooTransProvider;
import se.trixon.mapton.core.api.DecDegDMS;
import se.trixon.mapton.core.api.MapBounds;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CooTransProvider.class)
public class Wgs84DMS implements CooTransProvider {

    private MapBounds mBoundsProjected = new MapBounds(-180, -90, 180, 90);
    private MapBounds mBoundsWgs84 = new MapBounds(-180, -90, 180, 90);

    @Override
    public MapBounds getBoundsProjected() {
        return mBoundsProjected;
    }

    @Override
    public MapBounds getBoundsWgs84() {
        return mBoundsWgs84;
    }

    @Override
    public double getLatitude(double latitude, double longitude) {
        return latitude;
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        DecDegDMS dddms = new DecDegDMS(latitude, true);

        return dddms.format("%2d°%2d'%4.1f\"%s", "N", "S");
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return longitude;
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        DecDegDMS dddms = new DecDegDMS(longitude, true);

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
    public boolean isValid(double latitude, double longitude) {
        return mBoundsWgs84.contains(longitude, latitude);
    }

    @Override
    public String toString() {
        return getName();
    }

}
