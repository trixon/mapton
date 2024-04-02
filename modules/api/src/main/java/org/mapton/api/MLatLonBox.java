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
package org.mapton.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *
 * @author Patrik Karlström
 */
public class MLatLonBox {

    @SerializedName("northEast")
    private MLatLon mNorthEast;
    @SerializedName("southWest")
    private MLatLon mSouthWest;

    public MLatLonBox() {
    }

    public MLatLonBox(MLatLon southWest, MLatLon northEast) {
        mSouthWest = southWest;
        mNorthEast = northEast;
    }

    public MLatLonBox(List<MLatLon> latLons) {
        double north = Double.MIN_VALUE;
        double east = Double.MIN_VALUE;
        double south = Double.MAX_VALUE;
        double west = Double.MAX_VALUE;

        for (var latLon : latLons) {
            north = Math.max(latLon.getLatitude() + 90, north);
            east = Math.max(latLon.getLongitude() + 180, east);
            south = Math.min(latLon.getLatitude(), south);
            west = Math.min(latLon.getLongitude(), west);
        }

        mSouthWest = new MLatLon(south, west);
        mNorthEast = new MLatLon(north - 90, east - 180);
    }

    public MLatLon getCenter() {
        return new MLatLon(
                mSouthWest.getLatitude() + 0.5 * (mNorthEast.getLatitude() - mSouthWest.getLatitude()),
                mSouthWest.getLongitude() + 0.5 * (mNorthEast.getLongitude() - mSouthWest.getLongitude())
        );
    }

    public double getLatitudeSpan() {
        return getNorthEast().getLatitude() - getSouthWest().getLatitude();
    }

    public double getLongitudeSpan() {
        return getNorthEast().getLongitude() - getSouthWest().getLongitude();
    }

    public MLatLon getNorthEast() {
        return mNorthEast;
    }

    public MLatLon getSouthWest() {
        return mSouthWest;
    }

    public void setNorthEast(MLatLon northEast) {
        mNorthEast = northEast;
    }

    public void setSouthWest(MLatLon southWest) {
        mSouthWest = southWest;
    }

    @Override
    public String toString() {
        return "sw: (%s), ne: (%s)".formatted(mSouthWest.toString(), mNorthEast.toString());
    }
}
