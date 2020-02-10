/*
 * Copyright 2020 Patrik Karlström.
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

/**
 *
 * @author Patrik Karlström
 */
public class MPoi extends MBookmark {

    private String mGroup;
    private String mProvider;
    private String mTags;
    private String mUrl;
    private String mWkt;

    public MPoi() {
    }

    public String getGroup() {
        return mGroup;
    }

    public String getProvider() {
        return mProvider;
    }

    public String getTags() {
        return mTags;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getWkt() {
        return mWkt;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public void setProvider(String provider) {
        mProvider = provider;
    }

    public void setTags(String tags) {
        mTags = tags;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setWkt(String wkt) {
        mWkt = wkt;
    }
}
