/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade;

/**
 *
 * @author Patrik Karlström
 */
public class GradeFilterConfig {

    private String mKeyPrefix;
    private double mMaxDabbaH = 500.0;
    private double mMaxDabbaR = 500.0;
    private double mMaxDeltaH = 50.0;
    private double mMaxDeltaR = 100.0;
    private double mMaxGradeHorizontal = 20.0;
    private double mMaxGradeVertical = 20.0;
    private double mMinDabbaH;
    private double mMinDabbaR;
    private double mMinDeltaH;
    private double mMinDeltaR;
    private double mMinGradeHorizontal = 0.0;
    private double mMinGradeVertical = 0.0;

    public String getKeyPrefix() {
        return mKeyPrefix;
    }

    public double getMaxDabbaH() {
        return mMaxDabbaH;
    }

    public double getMaxDabbaR() {
        return mMaxDabbaR;
    }

    public double getMaxDeltaH() {
        return mMaxDeltaH;
    }

    public double getMaxDeltaR() {
        return mMaxDeltaR;
    }

    public double getMaxGradeHorizontal() {
        return mMaxGradeHorizontal;
    }

    public double getMaxGradeVertical() {
        return mMaxGradeVertical;
    }

    public double getMinDabbaH() {
        return mMinDabbaH;
    }

    public double getMinDabbaR() {
        return mMinDabbaR;
    }

    public double getMinDeltaH() {
        return mMinDeltaH;
    }

    public double getMinDeltaR() {
        return mMinDeltaR;
    }

    public double getMinGradeHorizontal() {
        return mMinGradeHorizontal;
    }

    public double getMinGradeVertical() {
        return mMinGradeVertical;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.mKeyPrefix = keyPrefix;
    }

    public void setMaxDabbaH(double maxDabbaH) {
        this.mMaxDabbaH = maxDabbaH;
    }

    public void setMaxDabbaR(double maxDabbaR) {
        this.mMaxDabbaR = maxDabbaR;
    }

    public void setMaxDeltaH(double maxDeltaH) {
        this.mMaxDeltaH = maxDeltaH;
    }

    public void setMaxDeltaR(double maxDeltaR) {
        this.mMaxDeltaR = maxDeltaR;
    }

    public void setMaxGradeHorizontal(double maxGradeHorizontal) {
        this.mMaxGradeHorizontal = maxGradeHorizontal;
    }

    public void setMaxGradeVertical(double maxGradeVertical) {
        this.mMaxGradeVertical = maxGradeVertical;
    }

    public void setMinDabbaH(double minDabbaH) {
        this.mMinDabbaH = minDabbaH;
    }

    public void setMinDabbaR(double minDabbaR) {
        this.mMinDabbaR = minDabbaR;
    }

    public void setMinDeltaH(double minDeltaH) {
        this.mMinDeltaH = minDeltaH;
    }

    public void setMinDeltaR(double minDeltaR) {
        this.mMinDeltaR = minDeltaR;
    }

    public void setMinGradeHorizontal(double minGradeHorizontal) {
        this.mMinGradeHorizontal = minGradeHorizontal;
    }

    public void setMinGradeVertical(double minGradeVertical) {
        this.mMinGradeVertical = minGradeVertical;
    }
}
