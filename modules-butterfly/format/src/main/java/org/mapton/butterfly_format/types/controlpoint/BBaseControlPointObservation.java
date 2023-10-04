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
package org.mapton.butterfly_format.types.controlpoint;

import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BBaseControlPointObservation {

    private String comment;
    private LocalDateTime date;
    private String instrument;
    private String name;
    private String operator;
    private boolean replacementMeasurement;
    private Integer status;
    private boolean zeroMeasurement;

    public BBaseControlPointObservation() {
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getName() {
        return name;
    }

    public String getOperator() {
        return operator;
    }

    public Integer getStatus() {
        return status;
    }

    public boolean isReplacementMeasurement() {
        return replacementMeasurement;
    }

    public boolean isZeroMeasurement() {
        return zeroMeasurement;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setReplacementMeasurement(boolean replacementMeasurement) {
        this.replacementMeasurement = replacementMeasurement;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setZeroMeasurement(boolean zeroMeasurement) {
        this.zeroMeasurement = zeroMeasurement;
    }

}
