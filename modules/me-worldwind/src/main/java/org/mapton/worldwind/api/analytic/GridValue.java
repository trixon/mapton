/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.worldwind.api.analytic;

import org.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
public class GridValue {

    private MLatLon mLatLon;
    private Double mValue;

    public GridValue() {
    }

    public GridValue(MLatLon latLon, Double value) {
        mLatLon = latLon;
        mValue = value;
    }

    public GridValue(double lat, double lon, Double value) {
        mLatLon = new MLatLon(lat, lon);
        mValue = value;
    }

    public MLatLon getLatLon() {
        return mLatLon;
    }

    public Double getValue() {
        return mValue;
    }

    public void setLatLon(MLatLon latLon) {
        mLatLon = latLon;
    }

    public void setValue(Double value) {
        mValue = value;
    }
}
