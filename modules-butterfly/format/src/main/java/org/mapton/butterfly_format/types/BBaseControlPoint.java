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
    private Integer numOfDecXY;
    private Integer numOfDecZ;
    private String operator;
    private Double rollingX;
    private Double rollingY;
    private Double rollingZ;
    private String status;
    private String tag;
    private Double zeroX;
    private Double zeroY;
    private Double zeroZ;

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

    public Integer getNumOfDecXY() {
        return numOfDecXY;
    }

    public Integer getNumOfDecZ() {
        return numOfDecZ;
    }

    public String getOperator() {
        return operator;
    }

    public Double getRollingX() {
        return rollingX;
    }

    public Double getRollingY() {
        return rollingY;
    }

    public Double getRollingZ() {
        return rollingZ;
    }

    public String getStatus() {
        return status;
    }

    public String getTag() {
        return tag;
    }

    public Double getZeroX() {
        return zeroX;
    }

    public Double getZeroY() {
        return zeroY;
    }

    public Double getZeroZ() {
        return zeroZ;
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

    public void setNumOfDecXY(Integer numOfDecXY) {
        this.numOfDecXY = numOfDecXY;
    }

    public void setNumOfDecZ(Integer numOfDecZ) {
        this.numOfDecZ = numOfDecZ;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setRollingX(Double rollingX) {
        this.rollingX = rollingX;
    }

    public void setRollingY(Double rollingY) {
        this.rollingY = rollingY;
    }

    public void setRollingZ(Double rollingZ) {
        this.rollingZ = rollingZ;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setZeroX(Double zeroX) {
        this.zeroX = zeroX;
    }

    public void setZeroY(Double zeroY) {
        this.zeroY = zeroY;
    }

    public void setZeroZ(Double zeroZ) {
        this.zeroZ = zeroZ;
    }

}
