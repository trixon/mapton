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
package org.mapton.mapollage.api;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 *
 * @author Patrik Karlström
 */
public class MapoPhoto {

    @SerializedName("altitude")
    private Double mAltitude;
    @SerializedName("bearing")
    private Double mBearing;
    @SerializedName("checksum")
    private String mChecksum;
    @SerializedName("date")
    private Date mDate;
    @SerializedName("height")
    private int mHeight;
    @SerializedName("lat")
    private double mLat;
    @SerializedName("lon")
    private double mLon;
    @SerializedName("orientation")
    private int mOrientation;
    @SerializedName("path")
    private String mPath;
    @SerializedName("width")
    private int mWidth;

    public MapoPhoto() {
    }

    public Double getAltitude() {
        return mAltitude;
    }

    public Double getBearing() {
        return mBearing;
    }

    public String getChecksum() {
        return mChecksum;
    }

    public Date getDate() {
        return mDate;
    }

    public int getHeight() {
        return mHeight;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public String getPath() {
        return mPath;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setAltitude(Double altitude) {
        mAltitude = altitude;
    }

    public void setBearing(Double bearing) {
        mBearing = bearing;
    }

    public void setChecksum(String checksum) {
        mChecksum = checksum;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public void setLon(double lon) {
        mLon = lon;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public void setWidth(int width) {
        mWidth = width;
    }
}
