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
package org.mapton.addon.xkcd;

/**
 *
 * @author Patrik Karlström
 */
public class Xkcd {

    private String mAlt;
    private String mFootnote;
    private String mLicense;
    private String mSrc;
    private String mTitle;
    private String mUrl;

    public Xkcd() {
    }

    public String getAlt() {
        return mAlt;
    }

    public String getFootnote() {
        return mFootnote;
    }

    public String getLicense() {
        return mLicense;
    }

    public String getSrc() {
        return mSrc;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setAlt(String alt) {
        mAlt = alt;
    }

    public void setFootnote(String footnote) {
        mFootnote = footnote;
    }

    public void setLicense(String license) {
        mLicense = license;
    }

    public void setSrc(String src) {
        mSrc = src;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

}
