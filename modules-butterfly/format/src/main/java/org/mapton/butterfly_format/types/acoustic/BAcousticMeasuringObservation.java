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
package org.mapton.butterfly_format.types.acoustic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BXyzPointObservation;

@JsonPropertyOrder({
    "name",
    "channelId",
    "date",
    "value",
    "unit",
    "limit",
    "distance",
    "frequency"
})
@JsonIgnoreProperties(value = {"id", "blastId", "replacementMeasurement", "zeroMeasurement", "operator"})
/**
 *
 * @author Patrik Karlström
 */
public class BAcousticMeasuringObservation extends BXyzPointObservation {

    private transient String blastId;
    private String channelIdX;
    private String channelIdY;
    private String channelIdZ;
    private Integer distance;
    private Double frequencyX;
    private Double frequencyY;
    private Double frequencyZ;
    private transient String id;
    private Double limit;
    private Ext mExt;
    private String unit;

    public BAcousticMeasuringObservation() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getBlastId() {
        return blastId;
    }

    public String getChannelIdX() {
        return channelIdX;
    }

    public String getChannelIdY() {
        return channelIdY;
    }

    public String getChannelIdZ() {
        return channelIdZ;
    }

    public Integer getDistance() {
        return distance;
    }

    public Double getFrequencyX() {
        return frequencyX;
    }

    public Double getFrequencyY() {
        return frequencyY;
    }

    public Double getFrequencyZ() {
        return frequencyZ;
    }

    public String getId() {
        return id;
    }

    public Double getLimit() {
        return limit;
    }

    public String getUnit() {
        return unit;
    }

    public void setBlastId(String blastId) {
        this.blastId = blastId;
    }

    public void setChannelIdX(String channelIdX) {
        this.channelIdX = channelIdX;
    }

    public void setChannelIdY(String channelIdY) {
        this.channelIdY = channelIdY;
    }

    public void setChannelIdZ(String channelIdZ) {
        this.channelIdZ = channelIdZ;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void setFrequencyX(Double frequencyX) {
        this.frequencyX = frequencyX;
    }

    public void setFrequencyY(Double frequencyY) {
        this.frequencyY = frequencyY;
    }

    public void setFrequencyZ(Double frequencyZ) {
        this.frequencyZ = frequencyZ;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public class Ext extends BXyzPointObservation.Ext<BAcousticMeasuringPoint> {

        public Ext() {
        }

    }

}
