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
import java.time.LocalDateTime;

@JsonPropertyOrder({
    "pointId",
    "channelId",
    "date",
    "value",
    "unit",
    "limit",
    "distance",
    "frequency"
})
@JsonIgnoreProperties(value = {"id", "blastId"})
/**
 *
 * @author Patrik Karlström
 */
public class BAcousticMeasuringObservation {

    private transient String blastId;
    private String channelId;
    private Integer distance;
    private Double frequency;
    private transient String id;
    private Double limit;
    private String pointId;
    private LocalDateTime date;
    private String unit;
    private Double value;

    public BAcousticMeasuringObservation() {
    }

    public String getBlastId() {
        return blastId;
    }

    public String getChannelId() {
        return channelId;
    }

    public Integer getDistance() {
        return distance;
    }

    public String getPointId() {
        return pointId;
    }

    public Double getFrequency() {
        return frequency;
    }

    public String getId() {
        return id;
    }

    public Double getLimit() {
        return limit;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getUnit() {
        return unit;
    }

    public Double getValue() {
        return value;
    }

    public void setBlastId(String blastId) {
        this.blastId = blastId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public void setPointId(String measuringPointId) {
        this.pointId = measuringPointId;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit(Double limit) {
        this.limit = limit;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setValue(Double value) {
        this.value = value;
    }

}
