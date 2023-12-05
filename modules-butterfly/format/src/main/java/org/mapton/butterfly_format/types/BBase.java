/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.mapton.butterfly_format.Butterfly;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBase {

    @JsonIgnore
    private transient Butterfly butterfly;
    private String name;

    public Butterfly getButterfly() {
        return butterfly;
    }

    public String getName() {
        return name;
    }

    public void setButterfly(Butterfly butterfly) {
        this.butterfly = butterfly;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract class Ext<T extends BBasePointObservation> {

        private LocalDateTime mDateFirst;
        private LocalDateTime mDateLatest;
        private transient LinkedHashMap<String, Integer> measuremenCountStats = new LinkedHashMap<>();
        private transient ArrayList<T> observationsAllCalculated;
        private transient ArrayList<T> observationsAllRaw;
        private transient ArrayList<T> observationsTimeFiltered;
        private transient final HashMap<Object, Object> values = new HashMap<>();

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

        public Object getValue(Object key) {
            return getValues().get(key);
        }

        public Object getValue(Object key, Object defaultValue) {
            return getValues().getOrDefault(key, defaultValue);
        }

        public HashMap<Object, Object> getValues() {
            return values;
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

        public Object setValue(Object key, Object value) {
            return values.put(key, value);
        }

    }

}
