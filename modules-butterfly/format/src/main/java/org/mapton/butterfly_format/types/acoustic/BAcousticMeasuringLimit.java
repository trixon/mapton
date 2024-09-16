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

import java.util.ArrayList;

/**
 *
 * @author Patrik Karlström
 */
public class BAcousticMeasuringLimit {

    private boolean fixed;
    private String pointId;
    private String unit;
    private Double value;
    private ArrayList<String> types;

    public BAcousticMeasuringLimit() {
    }

    public String getPointId() {
        return pointId;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public String getUnit() {
        return unit;
    }

    public Double getValue() {
        return value;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setValue(Double value) {
        this.value = value;
    }

}
