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

    private double airPressure;
    private double airTemperature;
    private double humidity;
    private transient Ext mExt;
    private double precipitation;
    private Integer visibility;
    private String weatherCode;
    private Integer windDirection;
    private double windSpeed;
    private double windSpeedMax;

    public BMeteoPointObservation() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public double getAirPressure() {
        return airPressure;
    }

    public double getAirTemperature() {
        return airTemperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPrecipitation() {
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

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindSpeedMax() {
        return windSpeedMax;
    }

    public void setAirPressure(double airPressure) {
        this.airPressure = airPressure;
    }

    public void setAirTemperature(double temperature) {
        this.airTemperature = temperature;
    }

    public void setExt(Ext ext) {
        this.mExt = ext;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setPrecipitation(double rain) {
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

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setWindSpeedMax(double windSpeedMax) {
        this.windSpeedMax = windSpeedMax;
    }

    public class Ext extends BXyzPointObservation.Ext<BMeteoPoint> {

        public Ext() {
        }

    }
}
