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
package org.mapton.butterfly_format.types.topo;

import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergenceObservation extends BXyzPointObservation {

    private Double distance1d;
    private Double distance2d;
    private Double distance3d;
    private String p1Name;
    private String p2Name;
    private Double zero1d;
    private Double zero2d;
    private Double zero3d;

    public BTopoConvergenceObservation() {
    }

    public Double getDistance1d() {
        return distance1d;
    }

    public Double getDistance2d() {
        return distance2d;
    }

    public Double getDistance3d() {
        return distance3d;
    }

    public String getP1Name() {
        return p1Name;
    }

    public String getP2Name() {
        return p2Name;
    }

    public Double getZero1d() {
        return zero1d;
    }

    public Double getZero2d() {
        return zero2d;
    }

    public Double getZero3d() {
        return zero3d;
    }

    public void setDistance1d(Double distance1d) {
        this.distance1d = distance1d;
    }

    public void setDistance2d(Double distance2d) {
        this.distance2d = distance2d;
    }

    public void setDistance3d(Double distance3d) {
        this.distance3d = distance3d;
    }

    public void setP1Name(String p1Name) {
        this.p1Name = p1Name;
    }

    public void setP2Name(String p2Name) {
        this.p2Name = p2Name;
    }

    public void setZero1d(Double zero1d) {
        this.zero1d = zero1d;
    }

    public void setZero2d(Double zero2d) {
        this.zero2d = zero2d;
    }

    public void setZero3d(Double zero3d) {
        this.zero3d = zero3d;
    }

}
