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

    private Double airPressure;
    private Double airTemperature;
    private Double humidity;
    private transient Ext mExt;
    private Double precipitation;
    private Integer visibility;
    private String weatherCode;
    private Integer windDirection;
    private Double windSpeed;
    private Double windSpeedMax;

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

    public Double getAirTemperature() {
        return airTemperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public String getWeatherCode() {
        return weatherCode;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public Double getWindSpeedMax() {
        return windSpeedMax;
    }

    public void setAirPressure(Double airPressure) {
        this.airPressure = airPressure;
    }

    public void setAirTemperature(Double temperature) {
        this.airTemperature = temperature;
    }

    public void setExt(Ext ext) {
        this.mExt = ext;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public void setPrecipitation(Double rain) {
        this.precipitation = rain;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public void setWeatherCode(String weatherCode) {
        this.weatherCode = weatherCode;
    }

    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindSpeedMax(Double windSpeedMax) {
        this.windSpeedMax = windSpeedMax;
    }

    public class Ext extends BXyzPointObservation.Ext<BMeteoPoint> {

        public Ext() {
        }

    }
}
