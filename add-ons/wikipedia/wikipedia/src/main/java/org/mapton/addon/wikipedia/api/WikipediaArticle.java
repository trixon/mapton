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
package org.mapton.addon.wikipedia.api;

import org.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
public class WikipediaArticle {

    private String mDescription;

    private Double mDistance;
    private MLatLon mLatLon;
    private String mThumbnail;
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private String mTitle;

    public WikipediaArticle() {
    }

    public String getDescription() {
        return mDescription;
    }

    public Double getDistance() {
        return mDistance;
    }

    public MLatLon getLatLon() {
        return mLatLon;
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public int getThumbnailHeight() {
        return mThumbnailHeight;
    }

    public int getThumbnailWidth() {
        return mThumbnailWidth;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDistance(Double distance) {
        mDistance = distance;
    }

    public void setLatLon(MLatLon latLon) {
        mLatLon = latLon;
    }

    public void setThumbnail(String thumbnail) {
        mThumbnail = thumbnail;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        mThumbnailHeight = thumbnailHeight;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        mThumbnailWidth = thumbnailWidth;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return getTitle();
    }

}
