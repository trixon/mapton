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
import java.sql.Timestamp;
import org.apache.commons.lang3.ObjectUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MBookmark {

    @SerializedName("category")
    private String mCategory = "";
    @SerializedName("description")
    private String mDescription = "";
    @SerializedName("display_marker")
    private Boolean mDisplayMarker;
    private transient Long mId;
    @SerializedName("lanLonBox")
    private MLatLonBox mLatLonBox;
    @SerializedName("latitude")
    private Double mLatitude;
    @SerializedName("longitude")
    private Double mLongitude;
    @SerializedName("name")
    private String mName = "";
    @SerializedName("time_created")
    private transient Timestamp mTimeCreated;
    @SerializedName("time_modified")
    private transient Timestamp mTimeModified;
    @SerializedName("zoom")
    private Double mZoom;

    public MBookmark() {
        setDisplayMarker(true);
    }

    public String getCategory() {
        return mCategory;
    }

    public String getDescription() {
        return mDescription;
    }

    public Long getId() {
        return mId;
    }

    public MLatLonBox getLatLonBox() {
        return mLatLonBox;
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

    public Timestamp getTimeCreated() {
        return mTimeCreated;
    }

    public Timestamp getTimeModified() {
        return mTimeModified;
    }

    public Double getZoom() {
        return mZoom;
    }

    public Boolean isCategory() {
        return !ObjectUtils.allNotNull(getLatitude(), getLongitude());
    }

    public Boolean isDisplayMarker() {
        return mDisplayMarker;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDisplayMarker(Boolean displayMarker) {
        mDisplayMarker = displayMarker;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setLatLonBox(MLatLonBox latLonBox) {
        mLatLonBox = latLonBox;
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

    public void setTimeCreated(Timestamp timeCreated) {
        mTimeCreated = timeCreated;
    }

    public void setTimeModified(Timestamp timeModified) {
        mTimeModified = timeModified;
    }

    public void setZoom(Double zoom) {
        mZoom = zoom;
    }
}
