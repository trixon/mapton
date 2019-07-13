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
package org.mapton.geonames.api;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström
 */
public class Geoname {

    @SerializedName("alternate_names")
    private String mAlternateNames;
    @SerializedName("ascii_name")
    private String mAsciiName;
    @SerializedName("country_code")
    private String mCountryCode;
    @SerializedName("elevation")
    private Integer mElevation;
    @SerializedName("lat")
    private Double mLatitude;
    @SerializedName("lon")
    private Double mLongitude;
    @SerializedName("name")
    private String mName;
    @SerializedName("population")
    private Integer mPopulation;

    public Geoname() {
    }

    public String getAlternateNames() {
        return mAlternateNames;
    }

    public String getAsciiName() {
        return mAsciiName;
    }

    public String getCountry() {
        return CountryManager.getInstance().getCountries().getOrDefault(getCountryCode(), "");
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public Integer getElevation() {
        return mElevation;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public String getName() {
        return mName;
    }

    public Integer getPopulation() {
        return mPopulation;
    }

    public void setAlternateNames(String alternateNames) {
        mAlternateNames = alternateNames;
    }

    public void setAsciiName(String asciiName) {
        mAsciiName = asciiName;
    }

    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public void setElevation(Integer elevation) {
        mElevation = elevation;
    }

    public void setLatitude(Double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(Double longitude) {
        mLongitude = longitude;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPopulation(Integer population) {
        mPopulation = population;
    }
}
