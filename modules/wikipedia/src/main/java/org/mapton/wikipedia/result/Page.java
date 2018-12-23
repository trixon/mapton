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
package org.mapton.wikipedia.result;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Page {

    @SerializedName("coordinates")
    private ArrayList<Coordinate> mCoordinates;
    @SerializedName("index")
    private int mIndex;
    @SerializedName("ns")
    private int mNs;
    @SerializedName("pageid")
    private int mPageid;
    @SerializedName("terms")
    private Terms mTerms;
    @SerializedName("thumbnail")
    private Thumbnail mThumbnail;
    @SerializedName("title")
    private String mTitle;

    public ArrayList<Coordinate> getCoordinates() {
        return mCoordinates;
    }

    public int getIndex() {
        return mIndex;
    }

    public int getNs() {
        return mNs;
    }

    public int getPageid() {
        return mPageid;
    }

    public Terms getTerms() {
        return mTerms;
    }

    public Thumbnail getThumbnail() {
        return mThumbnail;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
