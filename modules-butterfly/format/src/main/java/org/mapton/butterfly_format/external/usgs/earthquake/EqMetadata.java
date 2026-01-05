/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_format.external.usgs.earthquake;

/**
 *
 * @author Patrik Karlström
 */
public class EqMetadata {

    private String mApi;
    private int mCount;
    private long mGenerated;
    private int mStatus;
    private String mTitle;
    private String mUrl;

    public String getApi() {
        return mApi;
    }

    public int getCount() {
        return mCount;
    }

    public long getGenerated() {
        return mGenerated;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setApi(String api) {
        mApi = api;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public void setGenerated(long generated) {
        mGenerated = generated;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

}
