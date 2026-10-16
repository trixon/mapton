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
import org.mapton.butterfly_format.types.BBaseHydroPointObservation;

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
public class BHydroGroundwaterPointObservation extends BBaseHydroPointObservation {

    /*
    zm: down measurement from top-edge
m1: (temperature if present)
m2: sensor[m] as distance from top-edge, negative!
m4: calculated groundwater level - height
m5: top-edge of pipe = RÖK
m6: pressure from sensor in mVp

     */
    private double temperature = Double.NaN;
    private double downMeasurement = Double.NaN;
    private double sensorOffset = Double.NaN;
    private double groundwaterLevel = Double.NaN;
    private double topEdge = Double.NaN;
    private double preasureMvp = Double.NaN;

    public BHydroGroundwaterPointObservation() {
    }

    public double getGroundwaterLevel() {
        return groundwaterLevel;
    }

    public double getPreasureMvp() {
        return preasureMvp;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getDownMeasurement() {
        return downMeasurement;
    }

    public double getSensorOffset() {
        return sensorOffset;
    }

    public double getTopEdge() {
        return topEdge;
    }

    public void setGroundwaterLevel(double groundwaterLevel) {
        this.groundwaterLevel = groundwaterLevel;
    }

    public void setPreasureMvp(double preasureMvp) {
        this.preasureMvp = preasureMvp;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setDownMeasurement(double downMeasurement) {
        this.downMeasurement = downMeasurement;
    }

    public void setSensorOffset(double sensorOffset) {
        this.sensorOffset = sensorOffset;
    }

    public void setTopEdge(double topEdge) {
        this.topEdge = topEdge;
    }

    public class Ext extends BBaseHydroPointObservation.Ext<BHydroGroundwaterPointObservation> {

        public Ext() {
        }

    }

}
