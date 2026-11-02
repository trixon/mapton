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
    @JsonIgnore
    private long dateLatestEpochSeconds = EpochHelper.NULL_EPOCH_SECONDS;
    @JsonIgnore
    private int dateRollingEpochDays = EpochHelper.NULL_EPOCH_DAY;
    @JsonIgnore
    private int dateValidFromEpochDays = EpochHelper.NULL_EPOCH_DAY;
    @JsonIgnore
    private int dateValidToEpochDays = EpochHelper.NULL_EPOCH_DAY;
    @JsonIgnore
    private int dateZeroEpochDays = EpochHelper.NULL_EPOCH_DAY;
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

    public LocalDateTime getDateLatest() {
        return EpochHelper.epochSecondsToLocalDateTime(dateLatestEpochSeconds);
    }

    public long getDateLatestEpochSeconds() {
        return dateLatestEpochSeconds;
    }

    public LocalDate getDateRolling() {
        return EpochHelper.epochDayToLocalDate(dateRollingEpochDays);
    }

    public int getDateRollingEpochDays() {
        return dateRollingEpochDays;
    }

    public LocalDate getDateValidFrom() {
        return EpochHelper.epochDayToLocalDate(dateValidFromEpochDays);
    }

    public int getDateValidFromEpochDays() {
        return dateValidFromEpochDays;
    }

    public LocalDate getDateValidTo() {
        return EpochHelper.epochDayToLocalDate(dateValidToEpochDays);
    }

    public int getDateValidToEpochDays() {
        return dateValidToEpochDays;
    }

    public LocalDate getDateZero() {
        return EpochHelper.epochDayToLocalDate(dateZeroEpochDays);
    }

    public int getDateZeroEpochDays() {
        return dateZeroEpochDays;
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

    public boolean hasDateLatest() {
        return EpochHelper.hasDate(dateLatestEpochSeconds);
    }

    public boolean hasDateRolling() {
        return EpochHelper.hasDate(dateRollingEpochDays);
    }

    public boolean hasDateValidFrom() {
        return EpochHelper.hasDate(dateValidFromEpochDays);
    }

    public boolean hasDateValidTo() {
        return EpochHelper.hasDate(dateValidToEpochDays);
    }

    public boolean hasDateZero() {
        return EpochHelper.hasDate(dateZeroEpochDays);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public void setDateLatest(LocalDateTime dateLatest) {
        dateLatestEpochSeconds = EpochHelper.localDateTimeToEpochSeconds(dateLatest);
    }

    public void setDateRolling(LocalDate dateRolling) {
        this.dateRollingEpochDays = EpochHelper.localDateToEpochDay(dateRolling);
    }

    public void setDateValidFrom(LocalDate dateValidFrom) {
        this.dateValidFromEpochDays = EpochHelper.localDateToEpochDay(dateValidFrom);
    }

    public void setDateValidTo(LocalDate dateValidTo) {
        this.dateValidToEpochDays = EpochHelper.localDateToEpochDay(dateValidTo);
    }

    public void setDateZero(LocalDate dateZero) {
        this.dateZeroEpochDays = EpochHelper.localDateToEpochDay(dateZero);
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
