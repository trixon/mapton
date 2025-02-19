/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_format.types.acoustic;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "id",
    "name",
    "soilMaterial",
    "lat",
    "lon",
    "address",
    "comment"
})
public class BAcousticMeasuringPoint extends BXyzPoint {

    private String address;
    private String id;
    private transient Ext mExt;
    private String soilMaterial;
    private String url;

    public BAcousticMeasuringPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getAddress() {
        return address;
    }

    public String getId() {
        return id;
    }

    public String getSoilMaterial() {
        return soilMaterial;
    }

    public String getUrl() {
        return url;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSoilMaterial(String soilMaterial) {
        this.soilMaterial = soilMaterial;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public class Ext extends BXyzPoint.Ext<BAcousticMeasuringObservation> {

        private ArrayList<BAcousticMeasuringChannel> mChannels = new ArrayList<>();
        private ArrayList<BAcousticMeasuringLimit> mLimits = new ArrayList<>();

        public ArrayList<BAcousticMeasuringChannel> getChannels() {
            return mChannels;
        }

        public ArrayList<BAcousticMeasuringLimit> getLimits() {
            return mLimits;
        }

        public void setChannels(ArrayList<BAcousticMeasuringChannel> channels) {
            this.mChannels = channels;
        }

        public void setLimits(ArrayList<BAcousticMeasuringLimit> limits) {
            this.mLimits = limits;
        }

    }

}
