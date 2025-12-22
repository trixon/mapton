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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.jackson.CustomSerializerDecimals.CustomSerializer6Decimals;

@JsonPropertyOrder({
    "name",
    "category",
    "description",
    "latitude",
    "longitude",
    "zoom",
    "color",
    "displayMarker",
    "timeCreated",
    "timeModified"
})
@JsonIgnoreProperties({"values"})
/**
 *
 * @author Patrik Karlström
 */
public class MBookmark extends MObject {

    @JsonIgnore
    private transient Object avList;//Hide it
    @JsonIgnore
    private transient Object entries;//Hide it
    @JsonProperty("category")
    private String mCategory = "";
    @JsonProperty("color")
    private String mColor = "";
    @JsonProperty("description")
    private String mDescription = "";
    @JsonProperty("displayMarker")
    private Boolean mDisplayMarker;
    @JsonProperty("latitude")
    @JsonSerialize(using = CustomSerializer6Decimals.class)
    private Double mLatitude;
    @JsonProperty("longitude")
    @JsonSerialize(using = CustomSerializer6Decimals.class)
    private Double mLongitude;
    @JsonProperty("name")
    private String mName = "";
    @JsonProperty("timeCreated")
    private LocalDateTime mTimeCreated;
    @JsonProperty("timeModified")
    private LocalDateTime mTimeModified;
    @JsonProperty("url")
    private String mUrl;
    @JsonSerialize(using = CustomSerializer6Decimals.class)
    @JsonProperty("zoom")
    private Double mZoom;

    public static MLatLon createLatLon(MBookmark bookmark) {
        return new MLatLon(bookmark.getLatitude(), bookmark.getLongitude());
    }

    public MBookmark() {
        setDisplayMarker(true);
    }

    public String getCategory() {
        return mCategory;
    }

    public String getColor() {
        return Objects.toString(mColor, "FFFF00");
    }

    public String getDescription() {
        return mDescription;
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

    public LocalDateTime getTimeCreated() {
        return mTimeCreated;
    }

    public LocalDateTime getTimeModified() {
        return mTimeModified;
    }

    public String getUrl() {
        return mUrl;
    }

    public Double getZoom() {
        return mZoom;
    }

    @JsonIgnore
    public Boolean isCategory() {
        return !ObjectUtils.allNotNull(getLatitude(), getLongitude());
    }

    public Boolean isDisplayMarker() {
        return mDisplayMarker;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDisplayMarker(Boolean displayMarker) {
        mDisplayMarker = displayMarker;
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

    public void setTimeCreated(LocalDateTime timeCreated) {
        mTimeCreated = timeCreated;
    }

    public void setTimeModified(LocalDateTime timeModified) {
        mTimeModified = timeModified;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setZoom(Double zoom) {
        mZoom = zoom;
    }
}
