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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.Butterfly;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBase {

    @JsonIgnore
    private transient Butterfly butterfly;
    private String meta;
    private String name;
    private String origin;

    public Butterfly getButterfly() {
        return butterfly;
    }

    public String getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setButterfly(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public abstract class Ext<T extends BBasePointObservation> {

        private LocalDateTime mDateFirst;
        private LocalDateTime mDateLatest;
        private transient LinkedHashMap<String, Integer> measuremenCountStats = new LinkedHashMap<>();
        private transient ArrayList<T> observationsAllCalculated;
        private transient ArrayList<T> observationsAllRaw;
        private transient ArrayList<T> observationsTimeFiltered;
        private transient final HashMap<Object, Object> values = new HashMap<>();
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

        public Object getValue(Object key) {
            return getValues().get(key);
        }

        public Object getValue(Object key, Object defaultValue) {
            return getValues().getOrDefault(key, defaultValue);
        }

        public HashMap<Object, Object> getValues() {
            return values;
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

        public Object setValue(Object key, Object value) {
            return values.put(key, value);
        }

        public void setZeroUnset(boolean zeroUnset) {
            this.zeroUnset = zeroUnset;
        }

    }

}
