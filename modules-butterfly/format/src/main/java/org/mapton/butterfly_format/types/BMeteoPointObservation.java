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
package org.mapton.butterfly_format.types;

/**
 *
 * @author Patrik Karlström
 */
public class BMeteoPointObservation extends BXyzPointObservation {

    private Double temperature;
    private Double rh;
    private Double globalRad;
    private Double airPressure;
    private Double windSpeed;
    private Double windDirection;
    private Double rain;
    private Double no2;
    private Double o3;
    private Double pm10;
    private Double nox;
    private Double pm25;
    private transient Ext mExt;

    public BMeteoPointObservation() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getAirPressure() {
        return airPressure;
    }

    public Double getGlobalRad() {
        return globalRad;
    }

    public Double getNo2() {
        return no2;
    }

    public Double getNox() {
        return nox;
    }

    public Double getO3() {
        return o3;
    }

    public Double getPm10() {
        return pm10;
    }

    public Double getPm25() {
        return pm25;
    }

    public Double getRain() {
        return rain;
    }

    public Double getRh() {
        return rh;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getWindDirection() {
        return windDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setAirPressure(Double airPressure) {
        this.airPressure = airPressure;
    }

    public void setExt(Ext ext) {
        this.mExt = ext;
    }

    public void setGlobalRad(Double globalRad) {
        this.globalRad = globalRad;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public void setNox(Double nox) {
        this.nox = nox;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public void setRain(Double rain) {
        this.rain = rain;
    }

    public void setRh(Double rh) {
        this.rh = rh;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public class Ext extends BXyzPointObservation.Ext<BMeteoPoint> {

        public Ext() {
        }

    }
}
