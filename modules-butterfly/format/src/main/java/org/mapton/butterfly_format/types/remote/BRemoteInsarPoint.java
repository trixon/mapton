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

import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BRemoteInsarPoint extends BXyzPoint {

    private Double acceleration;
    private Integer changeDetected;
    private Double cumulativeDisplacement;
    private Double effArea;
    private transient Ext mExt;
    private Double seasonAmp;
    private Double stDef;
    private Double stDevAcceleration;
    private Double stDevHor;
    private Double stDevSeasonAmp;
    private Double stDevVelocity;
    private Double stDevVelocity3m;
    private Double stDevVelocity6m;
    private Double stDevVer;
    private Double velocity;
    private Double velocity3m;
    private Double velocity6m;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getAcceleration() {
        return acceleration;
    }

    public Integer getChangeDetected() {
        return changeDetected;
    }

    public Double getCumulativeDisplacement() {
        return cumulativeDisplacement;
    }

    public Double getEffArea() {
        return effArea;
    }

    public Double getSeasonAmp() {
        return seasonAmp;
    }

    public Double getStDef() {
        return stDef;
    }

    public Double getStDevAcceleration() {
        return stDevAcceleration;
    }

    public Double getStDevHor() {
        return stDevHor;
    }

    public Double getStDevSeasonAmp() {
        return stDevSeasonAmp;
    }

    public Double getStDevVelocity() {
        return stDevVelocity;
    }

    public Double getStDevVelocity3m() {
        return stDevVelocity3m;
    }

    public Double getStDevVelocity6m() {
        return stDevVelocity6m;
    }

    public Double getStDevVer() {
        return stDevVer;
    }

    public Double getVelocity() {
        return velocity;
    }

    public Double getVelocity3m() {
        return velocity3m;
    }

    public Double getVelocity6m() {
        return velocity6m;
    }

    public void setAcceleration(Double acceleration) {
        this.acceleration = acceleration;
    }

    public void setChangeDetected(Integer changeDetected) {
        this.changeDetected = changeDetected;
    }

    public void setCumulativeDisplacement(Double cumulativeDisplacement) {
        this.cumulativeDisplacement = cumulativeDisplacement;
    }

    public void setEffArea(Double effArea) {
        this.effArea = effArea;
    }

    public void setSeasonAmp(Double seasonAmp) {
        this.seasonAmp = seasonAmp;
    }

    public void setStDef(Double stDef) {
        this.stDef = stDef;
    }

    public void setStDevAcceleration(Double stDevAcceleration) {
        this.stDevAcceleration = stDevAcceleration;
    }

    public void setStDevHor(Double stDevHor) {
        this.stDevHor = stDevHor;
    }

    public void setStDevSeasonAmp(Double stDevSeasonAmp) {
        this.stDevSeasonAmp = stDevSeasonAmp;
    }

    public void setStDevVelocity(Double stDevVelocity) {
        this.stDevVelocity = stDevVelocity;
    }

    public void setStDevVelocity3m(Double stDevVelocity3m) {
        this.stDevVelocity3m = stDevVelocity3m;
    }

    public void setStDevVelocity6m(Double stDevVelocity6m) {
        this.stDevVelocity6m = stDevVelocity6m;
    }

    public void setStDevVer(Double stDevVer) {
        this.stDevVer = stDevVer;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    public void setVelocity3m(Double velocity3m) {
        this.velocity3m = velocity3m;
    }

    public void setVelocity6m(Double velocity6m) {
        this.velocity6m = velocity6m;
    }

    public class Ext extends BXyzPoint.Ext<BRemoteInsarPointObservation> {

    }

}
