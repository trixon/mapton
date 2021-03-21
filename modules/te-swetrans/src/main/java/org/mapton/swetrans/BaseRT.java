/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.swetrans;

import com.github.goober.coordinatetransformation.positions.RT90Position;
import com.github.goober.coordinatetransformation.positions.RT90Position.RT90Projection;
import com.github.goober.coordinatetransformation.positions.WGS84Position;
import javafx.geometry.Point2D;
import se.trixon.almond.util.Dict;
import org.mapton.api.MBounds;
import org.mapton.api.MCooTrans;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseRT implements MCooTrans {

    protected MBounds mBoundsProjected;
    protected MBounds mBoundsWgs84;
    protected String mName;
    protected RT90Projection mProjection;

    @Override
    public Point2D fromWgs84(double latitude, double longitude) {
        RT90Position position = getPosition(latitude, longitude);

        return new Point2D(position.getLongitude(), position.getLatitude());
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
        return getPosition(latitude, longitude).getLatitude();
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        return String.format("%.1f X", getPosition(latitude, longitude).getLatitude());
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return getPosition(latitude, longitude).getLongitude();
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        return String.format("%.1f Y", getPosition(latitude, longitude).getLongitude());
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getString(double latitude, double longitude) {
        if (isWithinWgs84Bounds(latitude, longitude)) {
            return String.format("%s  %s", getLatitudeString(latitude, longitude), getLongitudeString(latitude, longitude));
        } else {
            return Dict.OUT_OF_BOUNDS.toString();
        }
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
        RT90Position position = new RT90Position(latitude, longitude, mProjection);

        return new Point2D(position.toWGS84().getLongitude(), position.toWGS84().getLatitude());
    }

    private RT90Position getPosition(double latitude, double longitude) {
        return new RT90Position(new WGS84Position(latitude, longitude), mProjection);
    }
}
