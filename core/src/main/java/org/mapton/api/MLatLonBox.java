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
package org.mapton.api;

import com.google.gson.annotations.SerializedName;

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

    public MLatLon getCenter() {
        return new MLatLon(
                mSouthWest.getLatitude() + 0.5 * (mNorthEast.getLatitude() - mSouthWest.getLatitude()),
                mSouthWest.getLongitude() + 0.5 * (mNorthEast.getLongitude() - mSouthWest.getLongitude())
        );
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
}
