/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.api;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.control.DateSelectionMode;

/**
 *
 * @author Patrik Karlström
 */
public class MTemporalManager {

    private final SimpleObjectProperty<DateSelectionMode> mDateSelectionModeProperty = new SimpleObjectProperty<>();
    private final DelayedResetRunner mDelayedResetRunner;
    private final SimpleObjectProperty<LocalDate> mHighDateProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LocalDate> mLowDateProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LocalDate> mMaxDateProperty = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<LocalDate> mMinDateProperty = new SimpleObjectProperty<>();
    private final ConcurrentHashMap<String, MTemporalRange> mRanges = new ConcurrentHashMap<>();

    public static MTemporalManager getInstance() {
        return Holder.INSTANCE;
    }

    private MTemporalManager() {
        mDelayedResetRunner = new DelayedResetRunner(100, () -> {
            var fromRanges = new TreeSet<>(Comparator.comparing(MTemporalRange::getFromLocalDate));
            var toRanges = new TreeSet<>(Comparator.comparing(MTemporalRange::getToLocalDate));

            for (var range : mRanges.values()) {
                fromRanges.add(range);
                toRanges.add(range);
            }

            if (!fromRanges.isEmpty() && !toRanges.isEmpty()) {
                setMinDate(fromRanges.first().getFromLocalDate());
                setMaxDate(toRanges.last().getToLocalDate());
            } else {
                setMinDate(LocalDate.of(1900, 1, 1));
                setMaxDate(LocalDate.of(2099, 12, 31));
            }
        });
    }

    public void clear() {
        mRanges.clear();
        refresh();
    }

    public boolean contains(String key) {
        return mRanges.containsKey(key);
    }

    public SimpleObjectProperty<DateSelectionMode> dateSelectionModeProperty() {
        return mDateSelectionModeProperty;
    }

    public synchronized ConcurrentHashMap<String, MTemporalRange> getAndRemoveSubSet(String prefix) {
        var subSet = getSubSet(prefix);
        removeAll(prefix);

        return subSet;
    }

    public DateSelectionMode getDateSelectionMode() {
        return mDateSelectionModeProperty.getValue();
    }

    public LocalDate getHighDate() {
        return mHighDateProperty.getValue();
    }

    public LocalDate getLowDate() {
        return mLowDateProperty.getValue();
    }

    public LocalDate getMaxDate() {
        return mMaxDateProperty.getValue();
    }

    public LocalDate getMinDate() {
        return mMinDateProperty.getValue();
    }

    public synchronized ConcurrentHashMap<String, MTemporalRange> getSubSet(String prefix) {
        var subSet = new ConcurrentHashMap<String, MTemporalRange>();

        for (var key : mRanges.keySet()) {
            if (StringUtils.startsWith(key, prefix)) {
                subSet.put(key, mRanges.get(key));
            }
        }

        return subSet;
    }

    public SimpleObjectProperty<LocalDate> highDateProperty() {
        return mHighDateProperty;
    }

    public synchronized boolean isValid(String string) {
        return isValid(LocalDate.parse(string));
    }

    public synchronized boolean isValid(LocalDate localDate) {
        return getLowDate().compareTo(localDate) * localDate.compareTo(getHighDate()) >= 0;
    }

    public synchronized boolean isValid(LocalDateTime localDateTime) {
        return isValid(localDateTime.toLocalDate());
    }

    public synchronized boolean isValid(Timestamp timestamp) {
        return isValid(timestamp.toLocalDateTime().toLocalDate());
    }

    public synchronized boolean isValid(java.sql.Date date) {
        return isValid(date.toLocalDate());
    }

    public synchronized boolean isValid(java.util.Date date) {
        return isValid(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    public SimpleObjectProperty<LocalDate> lowDateProperty() {
        return mLowDateProperty;
    }

    public SimpleObjectProperty<LocalDate> maxDateProperty() {
        return mMaxDateProperty;
    }

    public SimpleObjectProperty<LocalDate> minDateProperty() {
        return mMinDateProperty;
    }

    public synchronized void put(String key, MTemporalRange range) {
        mRanges.put(key, range);
    }

    public synchronized void putAll(ConcurrentHashMap<String, MTemporalRange> subSet) {
        mRanges.putAll(subSet);

        refresh();
    }

    public void refresh() {
        mDelayedResetRunner.reset();
    }

    public synchronized MTemporalRange remove(String key) {
        return mRanges.remove(key);
    }

    public synchronized void removeAll(String prefix) {
        var keys = new ArrayList<String>();
        for (var key : mRanges.keySet()) {
            if (StringUtils.startsWith(key, prefix)) {
                keys.add(key);
            }
        }

        for (var key : keys) {
            mRanges.remove(key);
        }

        refresh();
    }

    public void setDateSelectionMode(DateSelectionMode dateSelectionMode) {
        mDateSelectionModeProperty.setValue(dateSelectionMode);
    }

    public void setHighDate(LocalDate localDate) {
        mHighDateProperty.setValue(localDate);
    }

    public void setLowDate(LocalDate localDate) {
        mLowDateProperty.setValue(localDate);
    }

    public void setMaxDate(LocalDate localDate) {
        mMaxDateProperty.setValue(localDate);
    }

    public void setMinDate(LocalDate localDate) {
        mMinDateProperty.setValue(localDate);
    }

    private static class Holder {

        private static final MTemporalManager INSTANCE = new MTemporalManager();
    }
}
