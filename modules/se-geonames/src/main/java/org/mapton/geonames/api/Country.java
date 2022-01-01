/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.geonames.api;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import org.mapton.api.MLatLonBox;

/**
 *
 * @author Patrik Karlström
 */
public class Country {

    @SerializedName(value = "Code")
    private String mCode;
    private ArrayList<Geoname> mGeonames = new ArrayList<>();
    private transient MLatLonBox mLatLonBox = new MLatLonBox();
    @SerializedName(value = "Name")
    private String mName;

    public Country() {
    }

    public String getCode() {
        return mCode;
    }

    public ArrayList<Geoname> getGeonames() {
        return mGeonames;
    }

    public MLatLonBox getLatLonBox() {
        return mLatLonBox;
    }

    public String getName() {
        return mName;
    }

    public void setCode(String code) {
        mCode = code;
    }

    public void setGeonames(ArrayList<Geoname> geonames) {
        mGeonames = geonames;
    }

    public void setLatLonBox(MLatLonBox latLonBox) {
        mLatLonBox = latLonBox;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
