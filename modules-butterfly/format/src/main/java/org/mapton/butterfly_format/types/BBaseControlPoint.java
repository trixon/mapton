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
package org.mapton.butterfly_format.types;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BBaseControlPoint extends BBasePoint {

    private String category;
    private LocalDateTime dateLatest;
    private LocalDate dateRolling;
    private LocalDate dateValidFrom;
    private LocalDate dateValidTo;
    private LocalDate dateZero;
    private Integer frequency;
    private Integer frequencyDefault;
    private Integer frequencyIntense;
    private String frequencyIntenseParam;
    private String operator;
    private String status;
    private String tag;

    public BBaseControlPoint() {
    }

    public String getCategory() {
        return category;
    }

    public LocalDateTime getDateLatest() {
        return dateLatest;
    }

    public LocalDate getDateRolling() {
        return dateRolling;
    }

    public LocalDate getDateValidFrom() {
        return dateValidFrom;
    }

    public LocalDate getDateValidTo() {
        return dateValidTo;
    }

    public LocalDate getDateZero() {
        return dateZero;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Integer getFrequencyDefault() {
        if (frequencyDefault == null) {
            frequencyDefault = -1;
        }

        return frequencyDefault;
    }

    public Integer getFrequencyIntense() {
        if (frequencyIntense == null) {
            frequencyIntense = -1;
        }

        return frequencyIntense;
    }

    public String getFrequencyIntenseParam() {
        return frequencyIntenseParam;
    }

    public String getOperator() {
        return operator;
    }

    public String getStatus() {
        return status;
    }

    public String getTag() {
        return tag;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDateLatest(LocalDateTime dateLatest) {
        this.dateLatest = dateLatest;
    }

    public void setDateRolling(LocalDate dateRolling) {
        this.dateRolling = dateRolling;
    }

    public void setDateValidFrom(LocalDate dateValidFrom) {
        this.dateValidFrom = dateValidFrom;
    }

    public void setDateValidTo(LocalDate dateValidTo) {
        this.dateValidTo = dateValidTo;
    }

    public void setDateZero(LocalDate dateZero) {
        this.dateZero = dateZero;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public void setFrequencyDefault(Integer frequencyDefault) {
        this.frequencyDefault = frequencyDefault;
    }

    public void setFrequencyIntense(Integer frequencyIntense) {
        this.frequencyIntense = frequencyIntense;
    }

    public void setFrequencyIntenseParam(String frequencyIntenseParam) {
        this.frequencyIntenseParam = frequencyIntenseParam;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
