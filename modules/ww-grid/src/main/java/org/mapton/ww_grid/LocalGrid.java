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
package org.mapton.ww_grid;

import com.google.gson.annotations.SerializedName;
import javafx.geometry.Point2D;
import org.mapton.api.MCooTrans;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGrid {

    @SerializedName("color")
    private String mColor = "000000";
    @SerializedName("cooTrans")
    private String mCooTrans = "WGS 84";
    @SerializedName("latCount")
    private int mLatCount;
    @SerializedName("latStart")
    private double mLatStart;
    @SerializedName("latStep")
    private double mLatStep;
    @SerializedName("lineWidth")
    private double mLineWidth;
    @SerializedName("lonCount")
    private int mLonCount;
    @SerializedName("lonStart")
    private double mLonStart;
    @SerializedName("lonStep")
    private double mLonStep;
    @SerializedName("name")
    private String mName;
    @SerializedName("visible")
    private boolean mVisible = true;

    public LocalGrid() {
    }

    public String getColor() {
        return mColor;
    }

    public String getCooTrans() {
        return mCooTrans;
    }

    public int getLatCount() {
        return mLatCount;
    }

    public double getLatStart() {
        return mLatStart;
    }

    public double getLatStep() {
        return mLatStep;
    }

    public double getLineWidth() {
        return mLineWidth;
    }

    public int getLonCount() {
        return mLonCount;
    }

    public double getLonStart() {
        return mLonStart;
    }

    public double getLonStep() {
        return mLonStep;
    }

    public String getName() {
        return mName;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public void setCooTrans(String cooTrans) {
        mCooTrans = cooTrans;
    }

    public void setLatCount(int latCount) {
        mLatCount = latCount;
    }

    public void setLatStart(double latStart) {
        mLatStart = latStart;
    }

    public void setLatStep(double latStep) {
        mLatStep = latStep;
    }

    public void setLineWidth(double lineWidth) {
        mLineWidth = lineWidth;
    }

    public void setLonCount(int lonCount) {
        mLonCount = lonCount;
    }

    public void setLonStart(double lonStart) {
        mLonStart = lonStart;
    }

    public void setLonStep(double lonStep) {
        mLonStep = lonStep;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    @Override
    public String toString() {
        return mName;
    }

    void fitToBounds() {
        MCooTrans cooTrans = MCooTrans.getCooTrans(getCooTrans());

        Point2D sw = cooTrans.toWgs84(mLatStart, mLonStart);
        Point2D ne = cooTrans.toWgs84(mLatStart + mLatStep * mLatCount, mLonStart + mLonStep * mLonCount);

        MLatLon southWest = new MLatLon(sw.getY(), sw.getX());
        MLatLon northEast = new MLatLon(ne.getY(), ne.getX());

        MLatLonBox latLonBox = new MLatLonBox(southWest, northEast);
        Mapton.getEngine().fitToBounds(latLonBox);
    }
}
