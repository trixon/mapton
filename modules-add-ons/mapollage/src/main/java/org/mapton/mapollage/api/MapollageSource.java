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
package org.mapton.mapollage.api;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström
 */
public class MapollageSource {

    @SerializedName("name")
    private String mName;
    @SerializedName("visible")
    private boolean mVisible = true;

    public MapollageSource() {
    }

    public void fitToBounds() {
//        MLatLonBox latLonBox = new MLatLonBox(southWest, northEast);
//        Mapton.getEngine().fitToBounds(latLonBox);
    }

    public String getName() {
        return mName;
    }

    public boolean isVisible() {
        return mVisible;
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
}
