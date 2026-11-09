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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.EpochHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasePoint extends BBase {

    private String comment;
    private String group;
    private double lat = Double.NaN;
    private double lon = Double.NaN;

    public String getComment() {
        return comment;
    }

    public String getGroup() {
        return group;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public abstract class Ext<T extends BBasePointObservation> {

        private long mEpochSecondsFirst = EpochHelper.NULL_EPOCH_SECONDS;
        private long mEpochSecondsLatest = EpochHelper.NULL_EPOCH_SECONDS;
        private long mEpochSecondsStoredZero = EpochHelper.NULL_EPOCH_SECONDS;

        private transient boolean mMultipleObservationsPerDay = true;
        private transient LinkedHashMap<String, Integer> measuremenCountStats = new LinkedHashMap<>();
        private transient ArrayList<T> observationsAllCalculated;
        private transient ArrayList<T> observationsAllRaw;
        private transient ArrayList<T> observationsTimeFiltered;

        private transient boolean zeroUnset = true;

        public LocalDateTime getDateFirst() {
            return EpochHelper.epochSecondsToLocalDateTime(mEpochSecondsFirst);
        }

        public String getDateFirstAsString() {
            return DateHelper.toDateTimeString(getDateFirst(), "-");
        }

        public LocalDateTime getDateLatest() {
            return EpochHelper.epochSecondsToLocalDateTime(mEpochSecondsLatest);
        }

        public String getDateLatestAsString() {
            return DateHelper.toDateTimeString(getDateLatest(), "-");
        }

        public long getEpochSecondsFirst() {
            return mEpochSecondsFirst;
        }

        public long getEpochSecondsLatest() {
            return mEpochSecondsLatest;
        }

        public long getEpochSecondsStoredZero() {
            return mEpochSecondsStoredZero;
        }

        public long getMeasurementAge(ChronoUnit chronoUnit) {
            if (getDateLatest() != null) {
                return chronoUnit.between(getDateLatest().toLocalDate(), LocalDate.now());
            } else {
                return -1L;
            }
        }

        public LinkedHashMap<String, Integer> getMeasurementCountStats() {
            return measuremenCountStats;
        }

        public HashMap<String, String> getMetaAsMap() {
            var map = new HashMap<String, String>();

            for (var s : StringUtils.split(getMeta(), "\\n")) {
                var item = StringUtils.split(s, "=", 2);
                try {
                    map.put(item[0], item[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                }

            }

            return map;
        }

        public Properties getMetaAsProperties() {
            var properties = new Properties();
            properties.putAll(getMetaAsMap());

            return properties;
        }

        public String getMetaAsString() {
            return getMeta();
        }

        public int getNumOfObservations() {
            return getObservationsAllRaw().size();
        }

        public int getNumOfObservationsFiltered() {
            return getObservationsTimeFiltered().size();
        }

        public T getObservationFilteredFirst() {
            try {
                return getObservationsTimeFiltered().getFirst();
            } catch (Exception e) {
                return null;
            }
        }

        public LocalDate getObservationFilteredFirstDate() {
            try {
                return getObservationFilteredFirst().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        public T getObservationFilteredLast() {
            try {
                return getObservationsTimeFiltered().getLast();
            } catch (Exception e) {
                return null;
            }
        }

        public LocalDate getObservationFilteredLastDate() {
            try {
                return getObservationFilteredLast().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        public LocalDateTime getObservationFilteredLastDateWithTime() {
            try {
                return getObservationFilteredLast().getDate();
            } catch (Exception e) {
                return null;
            }
        }

        public T getObservationRawFirst() {
            try {
                return getObservationsAllRaw().getFirst();
            } catch (Exception e) {
                return null;
            }
        }

        public LocalDate getObservationRawFirstDate() {
            try {
                return getObservationRawFirst().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        public T getObservationRawLast() {
            try {
                return getObservationsAllRaw().getLast();
            } catch (Exception e) {
                return null;
            }
        }

        public LocalDate getObservationRawLastDate() {
            try {
                return getObservationRawLast().getDate().toLocalDate();
            } catch (Exception e) {
                return null;
            }
        }

        public ArrayList<T> getObservationsAllCalculated() {
            if (observationsAllCalculated == null) {
                observationsAllCalculated = new ArrayList<>();
            }

            return observationsAllCalculated;
        }

        public ArrayList<T> getObservationsAllRaw() {
            if (observationsAllRaw == null) {
                observationsAllRaw = new ArrayList<>();
            }

            return observationsAllRaw;
        }

        public ArrayList<T> getObservationsTimeFiltered() {
            if (observationsTimeFiltered == null) {
                observationsTimeFiltered = new ArrayList<>();
            }

            return observationsTimeFiltered;
        }

        public LocalDateTime getStoredZeroDateTime() {
            return EpochHelper.epochSecondsToLocalDateTime(mEpochSecondsStoredZero);
        }

        public boolean hasEpochSecondsFirst() {
            return EpochHelper.hasDate(mEpochSecondsFirst);
        }

        public boolean hasEpochSecondsLatest() {
            return EpochHelper.hasDate(mEpochSecondsLatest);
        }

        public boolean hasEpochSecondsStordZero() {
            return EpochHelper.hasDate(mEpochSecondsStoredZero);
        }

        public boolean isMultipleObservationsPerDay() {
            return mMultipleObservationsPerDay;
        }

        public boolean isZeroUnset() {
            return zeroUnset;
        }

        public void setDateFirst(LocalDateTime dateTime) {
            mEpochSecondsFirst = EpochHelper.localDateTimeToEpochSeconds(dateTime);
        }

        public void setDateLatest(LocalDateTime dateTime) {
            mEpochSecondsLatest = EpochHelper.localDateTimeToEpochSeconds(dateTime);
        }

        public void setEpochSecondsFirst(long epochSecondsFirst) {
            mEpochSecondsFirst = epochSecondsFirst;
        }

        public void setEpochSecondsLatest(long epochSecondsLatest) {
            mEpochSecondsLatest = epochSecondsLatest;
        }

        public void setEpochSecondsStoredZero(long epochSecondsStoredZero) {
            mEpochSecondsStoredZero = epochSecondsStoredZero;
        }

        public void setMeasurementCountStats(LinkedHashMap<String, Integer> measuremenCountStats) {
            this.measuremenCountStats = measuremenCountStats;
        }

        public void setMultipleObservationsPerDay(boolean multipleObservationsPerDay) {
            this.mMultipleObservationsPerDay = multipleObservationsPerDay;
        }

        public void setObservationsAllCalculated(ArrayList<T> observationsCalculated) {
            this.observationsAllCalculated = observationsCalculated;
        }

        public void setObservationsAllRaw(ArrayList<T> observationsAllRaw) {
            this.observationsAllRaw = observationsAllRaw;
        }

        public void setObservationsTimeFiltered(ArrayList<T> observationsTimeFiltered) {
            this.observationsTimeFiltered = observationsTimeFiltered;
        }

        public void setStoredZeroDateTime(LocalDateTime dateTime) {
            this.mEpochSecondsStoredZero = EpochHelper.localDateTimeToEpochSeconds(dateTime);
        }

        public void setZeroUnset(boolean zeroUnset) {
            this.zeroUnset = zeroUnset;
        }

    }

}
