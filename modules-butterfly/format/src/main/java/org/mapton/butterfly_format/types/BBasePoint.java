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

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasePoint extends BBase {

    private String comment;
    private String group;
    private Double lat;
    private Double lon;

    public String getComment() {
        return comment;
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

    public void setComment(String comment) {
        this.comment = comment;
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

    public abstract class Ext<T extends BBasePointObservation> {

        private LocalDateTime mDateFirst;
        private LocalDateTime mDateLatest;
        private transient LinkedHashMap<String, Integer> measuremenCountStats = new LinkedHashMap<>();
        private transient ArrayList<T> observationsAllCalculated;
        private transient ArrayList<T> observationsAllRaw;
        private transient ArrayList<T> observationsTimeFiltered;
        private transient LocalDateTime storedZeroDateTime;
        private transient boolean zeroUnset = true;

        public LocalDateTime getDateFirst() {
            return mDateFirst;
        }

        public String getDateFirstAsString() {
            return mDateFirst != null ? mDateFirst.toString() : "-";
        }

        public LocalDateTime getDateLatest() {
            return mDateLatest;
        }

        public String getDateLatestAsString() {
            return mDateLatest != null ? mDateLatest.toString() : "-";
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
            return storedZeroDateTime;
        }

        public boolean isZeroUnset() {
            return zeroUnset;
        }

        public void setDateFirst(LocalDateTime dateFirst) {
            mDateFirst = dateFirst;
        }

        public void setDateLatest(LocalDateTime dateTime) {
            mDateLatest = dateTime;
        }

        public void setMeasurementCountStats(LinkedHashMap<String, Integer> measuremenCountStats) {
            this.measuremenCountStats = measuremenCountStats;
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

        public void setStoredZeroDateTime(LocalDateTime storedZeroDateTime) {
            this.storedZeroDateTime = storedZeroDateTime;
        }

        public void setZeroUnset(boolean zeroUnset) {
            this.zeroUnset = zeroUnset;
        }

    }

}
