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
package org.mapton.butterfly_format.types.rock;

import java.util.function.Function;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BRockConvergenceObservation extends BXyzPointObservation {

    public static final Function<BRockConvergenceObservation, Double> FUNCTION_1D = o -> o.ext().getDeltaX();
    public static final Function<BRockConvergenceObservation, Double> FUNCTION_2D = o -> o.ext().getDeltaY();
    public static final Function<BRockConvergenceObservation, Double> FUNCTION_3D = o -> o.ext().getDeltaZ();

    private Double calculatedConvergence1d;
    private Double calculatedConvergence2d;
    private Double calculatedConvergence3d;
    private transient Ext mExt;
    private Double offset1d;
    private Double offset2d;
    private Double offset3d;
    private String p1Name;
    private String p2Name;
    private Double zero1d;
    private Double zero2d;
    private Double zero3d;

    public BRockConvergenceObservation() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getCalculatedConvergence1d() {
        return calculatedConvergence1d;
    }

    public Double getCalculatedConvergence2d() {
        return calculatedConvergence2d;
    }

    public Double getCalculatedConvergence3d() {
        return calculatedConvergence3d;
    }

    public Double getOffset1d() {
        return offset1d;
    }

    public Double getOffset2d() {
        return offset2d;
    }

    public Double getOffset3d() {
        return offset3d;
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

    public void setCalculatedConvergence1d(Double calculatedConvergence1d) {
        this.calculatedConvergence1d = calculatedConvergence1d;
    }

    public void setCalculatedConvergence2d(Double calculatedConvergence2d) {
        this.calculatedConvergence2d = calculatedConvergence2d;
    }

    public void setCalculatedConvergence3d(Double calculatedConvergence3d) {
        this.calculatedConvergence3d = calculatedConvergence3d;
    }

    public void setOffset1d(Double offset1d) {
        this.offset1d = offset1d;
    }

    public void setOffset2d(Double offset2d) {
        this.offset2d = offset2d;
    }

    public void setOffset3d(Double offset3d) {
        this.offset3d = offset3d;
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

    public class Ext extends BXyzPointObservation.Ext<BRockConvergence> {

        private BRockConvergencePair mPair;

        public Ext() {
        }

        @Override
        public Double getDelta1d() {
            return getMeasuredZ();
        }

        @Override
        public Double getDelta2d() {
            return getMeasuredY();
        }

        @Override
        public Double getDelta3d() {
            return getMeasuredX();
        }

        public BRockConvergencePair getPair() {
            return mPair;
        }

        public void setPair(BRockConvergencePair pair) {
            this.mPair = pair;
        }
    }

}
