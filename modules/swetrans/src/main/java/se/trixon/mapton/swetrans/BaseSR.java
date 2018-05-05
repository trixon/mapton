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
package se.trixon.mapton.swetrans;

import com.github.goober.coordinatetransformation.positions.SWEREF99Position;
import com.github.goober.coordinatetransformation.positions.SWEREF99Position.SWEREFProjection;
import com.github.goober.coordinatetransformation.positions.WGS84Position;
import se.trixon.mapton.core.api.CooTransProvider;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseSR implements CooTransProvider {

    protected String mName;
    protected SWEREFProjection mProjection;

    @Override
    public double getLatitude(double latitude, double longitude) {
        return getPosition(latitude, longitude, mProjection).getLatitude();
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        return String.format("%.1f N", getPosition(latitude, longitude, mProjection).getLatitude());
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return getPosition(latitude, longitude, mProjection).getLongitude();
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        return String.format("%.1f E", getPosition(latitude, longitude, mProjection).getLongitude());
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getString(double latitude, double longitude) {
        return String.format("%s  %s", getLatitudeString(latitude, longitude), getLongitudeString(latitude, longitude));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }

    private SWEREF99Position getPosition(double latitude, double longitude, SWEREFProjection projection) {
        return new SWEREF99Position(new WGS84Position(latitude, longitude), projection);
    }

}
