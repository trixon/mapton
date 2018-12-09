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

/**
 *
 * @author Patrik Karlström
 */
public class LocalGrid {

    private boolean mChecked = true;
    private String mCooTrans = "WGS 84";
    private int mLatCount;
    private double mLatStart;
    private double mLatStep;
    private double mLineWidth;
    private int mLonCount;
    private double mLonStart;
    private double mLonStep;
    private String mName;

    public LocalGrid() {
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

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
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

    @Override
    public String toString() {
        return mName;
    }
}
