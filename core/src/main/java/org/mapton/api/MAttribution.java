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
package org.mapton.api;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MAttribution {

    @SerializedName("licenseName")
    private String mLicenseName;
    @SerializedName("licenseUrl")
    private String mLicenseUrl;
    @SerializedName("providerName")
    private String mProviderName;
    @SerializedName("providerUrl")
    private String mProviderUrl;
    @SerializedName("rawHtml")
    private String mRawHtml;

    public MAttribution() {
    }

    public String getLicenseName() {
        return StringUtils.defaultString(mLicenseName);
    }

    public String getLicenseUrl() {
        return StringUtils.defaultString(mLicenseUrl);
    }

    public String getProviderName() {
        return StringUtils.defaultString(mProviderName);
    }

    public String getProviderUrl() {
        return StringUtils.defaultString(mProviderUrl);
    }

    public String getRawHtml() {
        return StringUtils.defaultString(mRawHtml);
    }

    public void setLicenseName(String licenseName) {
        mLicenseName = licenseName;
    }

    public void setLicenseUrl(String licenseUrl) {
        mLicenseUrl = licenseUrl;
    }

    public void setProviderName(String providerName) {
        mProviderName = providerName;
    }

    public void setProviderUrl(String providerUrl) {
        mProviderUrl = providerUrl;
    }

    public void setRawHtml(String rawHtml) {
        mRawHtml = rawHtml;
    }
}
