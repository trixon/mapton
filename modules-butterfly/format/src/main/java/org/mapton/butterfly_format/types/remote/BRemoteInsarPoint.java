/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_format.types.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonIgnoreProperties(value = {"visible"})
public class BRemoteInsarPoint extends BXyzPoint {

    private double acceleration;
    private Integer changeDetected;
    private double cumulativeDisplacement;
    private Integer effArea;
    private transient Ext mExt;
    private double seasonAmp;
    private double stDef;
    private double stDevAcceleration;
    private double stDevHeight;
    private double stDevSeasonAmp;
    private double stDevVelocity;
    private double stDevVelocity3m;
    private double stDevVelocity6m;
    private double velocity;
    private double velocity3m;
    private double velocity6m;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public Integer getChangeDetected() {
        return changeDetected;
    }

    public double getCumulativeDisplacement() {
        return cumulativeDisplacement;
    }

    public Integer getEffArea() {
        return effArea;
    }

    public double getSeasonAmp() {
        return seasonAmp;
    }

    public double getStDef() {
        return stDef;
    }

    public double getStDevAcceleration() {
        return stDevAcceleration;
    }

    public double getStDevHeight() {
        return stDevHeight;
    }

    public double getStDevSeasonAmp() {
        return stDevSeasonAmp;
    }

    public double getStDevVelocity() {
        return stDevVelocity;
    }

    public double getStDevVelocity3m() {
        return stDevVelocity3m;
    }

    public double getStDevVelocity6m() {
        return stDevVelocity6m;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getVelocity3m() {
        return velocity3m;
    }

    public double getVelocity6m() {
        return velocity6m;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public void setChangeDetected(Integer changeDetected) {
        this.changeDetected = changeDetected;
    }

    public void setCumulativeDisplacement(double cumulativeDisplacement) {
        this.cumulativeDisplacement = cumulativeDisplacement;
    }

    public void setEffArea(Integer effArea) {
        this.effArea = effArea;
    }

    public void setSeasonAmp(double seasonAmp) {
        this.seasonAmp = seasonAmp;
    }

    public void setStDef(double stDef) {
        this.stDef = stDef;
    }

    public void setStDevAcceleration(double stDevAcceleration) {
        this.stDevAcceleration = stDevAcceleration;
    }

    public void setStDevHeight(double stDevHeight) {
        this.stDevHeight = stDevHeight;
    }

    public void setStDevSeasonAmp(double stDevSeasonAmp) {
        this.stDevSeasonAmp = stDevSeasonAmp;
    }

    public void setStDevVelocity(double stDevVelocity) {
        this.stDevVelocity = stDevVelocity;
    }

    public void setStDevVelocity3m(double stDevVelocity3m) {
        this.stDevVelocity3m = stDevVelocity3m;
    }

    public void setStDevVelocity6m(double stDevVelocity6m) {
        this.stDevVelocity6m = stDevVelocity6m;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public void setVelocity3m(double velocity3m) {
        this.velocity3m = velocity3m;
    }

    public void setVelocity6m(double velocity6m) {
        this.velocity6m = velocity6m;
    }

    public class Ext extends BXyzPoint.Ext<BRemoteInsarPointObservation> {

    }

}
