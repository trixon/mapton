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
package org.mapton.addon.mapollage.api;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Patrik Karlström
 */
public class MapoCollection {

    @SerializedName("dateMax")
    private Date mDateMax;
    @SerializedName("dateMin")
    private Date mDateMin;
    @SerializedName("id")
    private Long mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("photos")
    private ArrayList<MapoPhoto> mPhotos = new ArrayList<>();

    public MapoCollection() {
    }

    public Date getDateMax() {
        return mDateMax;
    }

    public Date getDateMin() {
        return mDateMin;
    }

    public Long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public ArrayList<MapoPhoto> getPhotos() {
        return mPhotos;
    }

    public void setDateMax(Date dateMax) {
        mDateMax = dateMax;
    }

    public void setDateMin(Date dateMin) {
        mDateMin = dateMin;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPhotos(ArrayList<MapoPhoto> photos) {
        mPhotos = photos;
    }

}
