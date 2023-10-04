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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class BBaseControlPoint {

    private String category;
    private String comment;
    private LocalDateTime dateLatest;
    private LocalDate dateRolling;
    private LocalDate dateValidFrom;
    private LocalDate dateValidTo;
    private LocalDate dateZero;
    private Integer frequency;
    private String group;
    private Double lat;
    private Double lon;
    private String meta;
    private String name;
    private Integer numOfDecXY;
    private Integer numOfDecZ;
    private String operator;
    private String origin;
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

    public String getComment() {
        return comment;
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

    public String getGroup() {
        return group;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public String getMeta() {
        return meta;
    }

    public String getName() {
        return name;
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

    public String getOrigin() {
        return origin;
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

    public void setComment(String comment) {
        this.comment = comment;
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

    public void setGroup(String group) {
        this.group = group;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setOrigin(String origin) {
        this.origin = origin;
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

    @Override
    public String toString() {
        return getName();
    }

    public abstract class Ext {

        private transient final HashMap<Object, Object> values = new HashMap<>();

        public Object getValue(Object key) {
            return getValues().get(key);
        }

        public Object getValue(Object key, Object defaultValue) {
            return getValues().getOrDefault(key, defaultValue);
        }

        public HashMap<Object, Object> getValues() {
            return values;
        }

        public Object setValue(Object key, Object value) {
            return values.put(key, value);
        }

    }
}
