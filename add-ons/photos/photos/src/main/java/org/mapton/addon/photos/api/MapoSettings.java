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
package org.mapton.addon.photos.api;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSettings {

    @SerializedName("color_gap")
    private String mColorGap = "000000";
    @SerializedName("color_track")
    private String mColorTrack = "FF0000";
    @SerializedName("high_date")
    private LocalDate mHighDate;
    @SerializedName("low_date")
    private LocalDate mLowDate;
    @SerializedName("plot_gaps")
    private boolean mPlotGaps = true;
    @SerializedName("plot_tracks")
    private boolean mPlotTracks = true;
    @SerializedName("split_by")
    private SplitBy mSplitBy = SplitBy.MONTH;
    @SerializedName("width")
    private Double mWidth = 2.0;

    public MapoSettings() {
    }

    public String getColorGap() {
        return mColorGap;
    }

    public String getColorTrack() {
        return mColorTrack;
    }

    public LocalDate getHighDate() {
        return mHighDate;
    }

    public LocalDate getLowDate() {
        return mLowDate;
    }

    public SplitBy getSplitBy() {
        return mSplitBy;
    }

    public Double getWidth() {
        return mWidth;
    }

    public boolean isPlotGaps() {
        return mPlotGaps;
    }

    public boolean isPlotTracks() {
        return mPlotTracks;
    }

    public void setColorGap(String colorGap) {
        mColorGap = colorGap;
    }

    public void setColorTrack(String colorTrack) {
        mColorTrack = colorTrack;
    }

    public void setHighDate(LocalDate highDate) {
        mHighDate = highDate;
    }

    public void setLowDate(LocalDate lowDate) {
        mLowDate = lowDate;
    }

    public void setPlotGaps(boolean plotGaps) {
        mPlotGaps = plotGaps;
    }

    public void setPlotTracks(boolean plotTracks) {
        mPlotTracks = plotTracks;
    }

    public void setSplitBy(SplitBy splitBy) {
        mSplitBy = splitBy;
    }

    public void setWidth(Double width) {
        mWidth = width;
    }

    public enum SplitBy {
        NONE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR;
    }
}
