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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.DoubleStream;
import se.trixon.almond.util.EpochHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BBaseControlPoint extends BBasePoint {

    private String category;
    private String classification;
    private long epochSecondsLatest = EpochHelper.NULL_EPOCH_SECONDS;
    private int epochDaysRolling = EpochHelper.NULL_EPOCH_DAY;
    private int epochDaysValidFrom = EpochHelper.NULL_EPOCH_DAY;
    private int epochDaysValidTo = EpochHelper.NULL_EPOCH_DAY;
    private int epochDaysZero = EpochHelper.NULL_EPOCH_DAY;
    private Integer frequency;
    private Integer frequencyDefault;
    private Integer frequencyHigh;
    private String frequencyHighParam;
    private String operator;
    private String status;
    private String tag;

    public static double getMedian(DoubleStream stream) {
        var values = stream.sorted().toArray();
        int length = values.length;

        if (length == 0) {
            throw new IllegalArgumentException("Stream is empty");
        } else if (length % 2 == 1) {
            return values[length / 2];
        } else {
            return (values[length / 2 - 1] + values[length / 2]) / 2.0;
        }
    }

    public BBaseControlPoint() {
    }

    public String getCategory() {
        return category;
    }

    public String getClassification() {
        return classification;
    }

    @JsonProperty("dateLatest")
    public LocalDateTime getDateLatest() {
        return EpochHelper.epochSecondsToLocalDateTime(epochSecondsLatest);
    }

    @JsonIgnore
    public long getEpochSecondsLatest() {
        return epochSecondsLatest;
    }

    @JsonProperty("dateRolling")
    public LocalDate getDateRolling() {
        return EpochHelper.epochDayToLocalDate(epochDaysRolling);
    }

    @JsonProperty("dateValidFrom")
    public LocalDate getDateValidFrom() {
        return EpochHelper.epochDayToLocalDate(epochDaysValidFrom);
    }

    @JsonProperty("dateValidTo")
    public LocalDate getDateValidTo() {
        return EpochHelper.epochDayToLocalDate(epochDaysValidTo);
    }

    @JsonProperty("dateZero")
    public LocalDate getDateZero() {
        return EpochHelper.epochDayToLocalDate(epochDaysZero);
    }

    @JsonIgnore
    public int getEpochDaysRolling() {
        return epochDaysRolling;
    }

    @JsonIgnore
    public int getEpochDaysValidFrom() {
        return epochDaysValidFrom;
    }

    @JsonIgnore
    public int getEpochDaysValidTo() {
        return epochDaysValidTo;
    }

    @JsonIgnore
    public int getEpochDaysZero() {
        return epochDaysZero;
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

    public Integer getFrequencyHigh() {
        if (frequencyHigh == null) {
            frequencyHigh = -1;
        }

        return frequencyHigh;
    }

    public String getFrequencyHighParam() {
        return frequencyHighParam;
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

    public boolean hasEpochDaysRolling() {
        return EpochHelper.hasDate(epochDaysRolling);
    }

    public boolean hasEpochDaysValidFrom() {
        return EpochHelper.hasDate(epochDaysValidFrom);
    }

    public boolean hasEpochDaysValidTo() {
        return EpochHelper.hasDate(epochDaysValidTo);
    }

    public boolean hasEpochDaysZero() {
        return EpochHelper.hasDate(epochDaysZero);
    }

    public boolean hasEpochSecondsLatest() {
        return EpochHelper.hasDate(epochSecondsLatest);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    @JsonProperty("dateLatest")
    public void setDateLatest(LocalDateTime dateLatest) {
        epochSecondsLatest = EpochHelper.localDateTimeToEpochSeconds(dateLatest);
    }

    @JsonProperty("dateRolling")
    public void setDateRolling(LocalDate dateRolling) {
        this.epochDaysRolling = EpochHelper.localDateToEpochDay(dateRolling);
    }

    @JsonProperty("dateValidFrom")
    public void setDateValidFrom(LocalDate dateValidFrom) {
        this.epochDaysValidFrom = EpochHelper.localDateToEpochDay(dateValidFrom);
    }

    @JsonProperty("dateValidTo")
    public void setDateValidTo(LocalDate dateValidTo) {
        this.epochDaysValidTo = EpochHelper.localDateToEpochDay(dateValidTo);
    }

    @JsonProperty("dateZero")
    public void setDateZero(LocalDate dateZero) {
        this.epochDaysZero = EpochHelper.localDateToEpochDay(dateZero);
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public void setFrequencyDefault(Integer frequencyDefault) {
        this.frequencyDefault = frequencyDefault;
    }

    public void setFrequencyHigh(Integer frequencyHigh) {
        this.frequencyHigh = frequencyHigh;
    }

    public void setFrequencyHighParam(String frequencyHighParam) {
        this.frequencyHighParam = frequencyHighParam;
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
