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
public class MPoiStyle {

    private String mImageColor;
    private ImageLocation mImageLocation = ImageLocation.BOTTOM_LEFT;
    private double mImageScale = 1.0;
    private String mImageUrl;
    private boolean mImageVisible = true;
    private String mLabelColor;
    private double mLabelScale = 1.0;
    private String mLabelText;
    private boolean mLabelVisible = true;

    public MPoiStyle() {
    }

    public String getImageColor() {
        return mImageColor;
    }

    public ImageLocation getImageLocation() {
        return mImageLocation;
    }

    public double getImageScale() {
        return mImageScale;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getLabelColor() {
        return mLabelColor;
    }

    public double getLabelScale() {
        return mLabelScale;
    }

    public String getLabelText() {
        return mLabelText;
    }

    public boolean isImageVisible() {
        return mImageVisible;
    }

    public boolean isLabelVisible() {
        return mLabelVisible;
    }

    public void setImageColor(String imageColor) {
        mImageColor = imageColor;
    }

    public void setImageLocation(ImageLocation imageLocation) {
        mImageLocation = imageLocation;
    }

    public void setImageScale(double imageScale) {
        mImageScale = imageScale;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public void setImageVisible(boolean imageVisible) {
        mImageVisible = imageVisible;
    }

    public void setLabelColor(String labelColor) {
        mLabelColor = labelColor;
    }

    public void setLabelScale(double labelScale) {
        mLabelScale = labelScale;
    }

    public void setLabelText(String labelText) {
        mLabelText = labelText;
    }

    public void setLabelVisible(boolean labelVisible) {
        mLabelVisible = labelVisible;
    }

    public enum ImageLocation {
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT;
    }
}
