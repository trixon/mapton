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
package org.mapton.butterfly_format.types.hydro;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "date",
    "measuredX",
    "measuredY",
    "measuredZ",
    "operator",
    "instrument",
    "comment",
    "status",
    "replacementMeasurement",
    "zeroMeasurement"
})
public class BHydroControlPointObservation extends BBaseControlPointObservation {

    /*
    zm: down measurement from top-edge
m1: (temperature if present)
m2: sensor[m] as distance from top-edge, negative!
m4: calculated groundwater level - height
m5: top-edge of pipe = RÖK
m6: pressure from sensor in mVp

     */
    private Double temperature;
    private Double downMeasurement;
    private Double sensorOffset;
    private Double groundWaterLevel;
    private Double topEdge;
    private Double preasureMvp;

    public BHydroControlPointObservation() {
    }

    public Double getGroundWaterLevel() {
        return groundWaterLevel;
    }

    public Double getPreasureMvp() {
        return preasureMvp;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getDownMeasurement() {
        return downMeasurement;
    }

    public Double getSensorOffset() {
        return sensorOffset;
    }

    public Double getTopEdge() {
        return topEdge;
    }

    public void setGroundWaterLevel(Double groundWaterLevel) {
        this.groundWaterLevel = groundWaterLevel;
    }

    public void setPreasureMvp(Double preasureMvp) {
        this.preasureMvp = preasureMvp;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setDownMeasurement(Double downMeasurement) {
        this.downMeasurement = downMeasurement;
    }

    public void setSensorOffset(Double sensorOffset) {
        this.sensorOffset = sensorOffset;
    }

    public void setTopEdge(Double topEdge) {
        this.topEdge = topEdge;
    }

}
