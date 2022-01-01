/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.addon.wikipedia.result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author Patrik Karlström
 */
public class ApiResult {

    private static final Gson gson = new GsonBuilder()
            .setVersion(1.0)
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @SerializedName("batchcomplete")
    private Boolean mBatchComplete;
    @SerializedName("continue")
    private Continue mContinue;
    @SerializedName("curtimestamp")
    private Timestamp mCurTimestamp;
    @SerializedName("errorlang")
    private String mErrorLang;
    @SerializedName("query")
    private Query mQuery;
    @SerializedName("servedby")
    private String mServedBy;
    @SerializedName("uselang")
    private String mUseLang;

    public static ApiResult load(String json) {
        ApiResult apiResult = gson.fromJson(json, ApiResult.class);

        return apiResult;
    }

    public ApiResult() {
    }

    public Boolean getBatchComplete() {
        return mBatchComplete;
    }

    public Continue getContinue() {
        return mContinue;
    }

    public Timestamp getCurTimestamp() {
        return mCurTimestamp;
    }

    public String getErrorLang() {
        return mErrorLang;
    }

    public Query getQuery() {
        return mQuery;
    }

    public String getServedBy() {
        return mServedBy;
    }

    public String getUseLang() {
        return mUseLang;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
