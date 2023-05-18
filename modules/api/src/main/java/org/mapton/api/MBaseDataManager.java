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
package org.mapton.api;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class MBaseDataManager<T> {

    private static final Logger LOGGER = Logger.getLogger(MBaseDataManager.class.getName());
    protected final MSelectionLockManager mSelectionLockManager = MSelectionLockManager.getInstance();

    private final String TEMPORAL_PREFIX;
    private final ObjectProperty<ObservableList<T>> mAllItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mAllItemsSet = new HashSet<>();
    private final DelayedResetRunner mDelayedResetRunner;
    private final ObjectProperty<ObservableList<T>> mFilteredItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mFilteredItemsSet = new HashSet<>();
    private T mOldSelectedValue;
    private final ObjectProperty<T> mSelectedItemProperty = new SimpleObjectProperty<>();
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final ObjectProperty<MTemporalRange> mTemporalRangeProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<T>> mTimeFilteredItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mTimeFilteredItemsSet = new HashSet<>();
    private final DelayedResetRunner mUnlockDelayedResetRunner;

    public MBaseDataManager(Class<T> typeParameterClass) {
        TEMPORAL_PREFIX = typeParameterClass.getName();

        mAllItemsProperty.setValue(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
        mFilteredItemsProperty.setValue(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
        mTimeFilteredItemsProperty.setValue(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));

        mUnlockDelayedResetRunner = new DelayedResetRunner(100, () -> {
            mSelectionLockManager.removeLock(this);
        });

        mDelayedResetRunner = new DelayedResetRunner(50, () -> {
            try {
                selectionLock();
                applyTemporalFilter();
                selectionUnlock();
            } catch (Exception e) {
                //
            }
        });

        init();
        initListeners();
    }

    public ObjectProperty<ObservableList<T>> allItemsProperty() {
        return mAllItemsProperty;
    }

    public ObjectProperty<ObservableList<T>> filteredItemsProperty() {
        return mFilteredItemsProperty;
    }

    public final ObservableList<T> getAllItems() {
        return mAllItemsProperty.get();
    }

    public HashSet<T> getAllItemsSet() {
        return mAllItemsSet;
    }

    public ObservableList<T> getFilteredItems() {
        return mFilteredItemsProperty.get();
    }

    public HashSet<T> getFilteredItemsSet() {
        return mFilteredItemsSet;
    }

    public T getSelectedItem() {
        return mSelectedItemProperty.get();
    }

    public MTemporalRange getTemporalRange() {
        return mTemporalRangeProperty.get();
    }

    public ObservableList<T> getTimeFilteredItems() {
        return mTimeFilteredItemsProperty.get();
    }

    public HashSet<T> getTimeFilteredItemsSet() {
        return mTimeFilteredItemsSet;
    }

    public boolean isSelectionLocked() {
        return mSelectionLockManager.isLocked();
    }

    public boolean isSelectionUnlocked() {
        return !mSelectionLockManager.isLocked();
    }

    public boolean isValid(String string) {
        return string == null ? false : mTemporalManager.isValid(string);
    }

    public boolean isValid(LocalDate localDate) {
        return localDate == null ? false : mTemporalManager.isValid(localDate);
    }

    public boolean isValid(LocalDateTime localDateTime) {
        return localDateTime == null ? false : mTemporalManager.isValid(localDateTime);
    }

    public boolean isValid(Timestamp timestamp) {
        return timestamp == null ? false : mTemporalManager.isValid(timestamp);
    }

    public boolean isValid(java.sql.Date date) {
        return date == null ? false : mTemporalManager.isValid(date);
    }

    public boolean isValid(java.util.Date date) {
        return date == null ? false : mTemporalManager.isValid(date);
    }

    public void restoreSelection() {
        mSelectedItemProperty.set(mOldSelectedValue);
    }

    public ObjectProperty<T> selectedItemProperty() {
        return mSelectedItemProperty;
    }

    public void selectionLock() {
        mSelectionLockManager.addLock(this);
    }

    public void selectionUnlock() {
        mUnlockDelayedResetRunner.reset();
    }

    public void setSelectedItem(T item) {
        FxHelper.runLater(() -> {
            mSelectedItemProperty.set(item);
        });
    }

    public void setSelectedItemAfterReset(T item) {
        FxHelper.runLater(() -> {
            mSelectedItemProperty.set(null);
            FxHelper.runLaterDelayed(10, () -> {
                mSelectedItemProperty.set(item);
            });
        });
    }

    public void setTemporalRange(LocalDate first, LocalDate last) {
        setTemporalRange(new MTemporalRange(first, last));
    }

    public void setTemporalRange(MTemporalRange temporalRange) {
        mTemporalManager.put(TEMPORAL_PREFIX, temporalRange);
        mTemporalRangeProperty.set(temporalRange);
    }

    public void setTemporalVisibility(boolean visible) {
        if (mTemporalRangeProperty != null && getTemporalRange() != null) {
            if (visible && !mTemporalManager.contains(TEMPORAL_PREFIX)) {
                mTemporalManager.put(TEMPORAL_PREFIX, getTemporalRange());
                mTemporalManager.refresh();
            } else if (!visible && mTemporalManager.contains(TEMPORAL_PREFIX)) {
                mTemporalManager.remove(TEMPORAL_PREFIX);
                mTemporalManager.refresh();
            }
        }
    }

    public ObjectProperty<MTemporalRange> temporalRangeProperty() {
        return mTemporalRangeProperty;
    }

    public ObjectProperty<ObservableList<T>> timeFilteredItemsProperty() {
        return mTimeFilteredItemsProperty;
    }

    protected abstract void applyTemporalFilter();

    protected abstract void load(ArrayList<T> items);

    private void init() {
    }

    private void initListeners() {
        mTemporalManager.lowDateProperty().addListener((observable, oldValue, newValue) -> {
            mDelayedResetRunner.reset();
        });

        mTemporalManager.highDateProperty().addListener((observable, oldValue, newValue) -> {
            mDelayedResetRunner.reset();
        });

        getFilteredItems().addListener((ListChangeListener.Change<? extends T> c) -> {
            mDelayedResetRunner.reset();
        });

        mSelectedItemProperty.addListener((ObservableValue<? extends T> observable, T oldValue, T newValue) -> {
            if (newValue != null) {
                mOldSelectedValue = newValue;
            }
        });
    }
}
