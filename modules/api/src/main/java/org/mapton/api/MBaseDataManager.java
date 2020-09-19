/*
 * Copyright 2020 Patrik Karlström.
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

import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class MBaseDataManager<T> {

    protected final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    protected MTemporalRange mTemporalRange;
    private final String TEMPORAL_PREFIX;
    private final ObjectProperty<ObservableList<T>> mAllItemsProperty = new SimpleObjectProperty<>();
    private final DelayedResetRunner mDelayedResetRunner;
    private final ObjectProperty<ObservableList<T>> mFilteredItemsProperty = new SimpleObjectProperty<>();
    private T mOldSelectedValue;
    private final ObjectProperty<T> mSelectedItemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<T>> mTimeFilteredItemsProperty = new SimpleObjectProperty<>();

    public MBaseDataManager(String temporalPrefix) {
        TEMPORAL_PREFIX = temporalPrefix;

        mAllItemsProperty.setValue(FXCollections.observableArrayList());
        mFilteredItemsProperty.setValue(FXCollections.observableArrayList());
        mTimeFilteredItemsProperty.setValue(FXCollections.observableArrayList());

        mDelayedResetRunner = new DelayedResetRunner(50, () -> {
            try {
                applyTemporalFilter();
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
        return mAllItemsProperty == null ? null : mAllItemsProperty.get();
    }

    public ObservableList<T> getFilteredItems() {
        return mFilteredItemsProperty == null ? null : mFilteredItemsProperty.get();
    }

    public T getSelectedItem() {
        return mSelectedItemProperty.get();
    }

    public ObservableList<T> getTimeFilteredItems() {
        return mTimeFilteredItemsProperty == null ? null : mTimeFilteredItemsProperty.get();
    }

    public void restoreSelection() {
        mSelectedItemProperty.set(mOldSelectedValue);
    }

    public ObjectProperty<T> selectedItemProperty() {
        return mSelectedItemProperty;
    }

    public void setSelectedItem(T item) {
        mSelectedItemProperty.set(item);
    }

    public void setTemporalVisibility(boolean visible) {
        if (mTemporalRange != null) {
            if (visible) {
                mTemporalManager.put(TEMPORAL_PREFIX, mTemporalRange);
            } else {
                mTemporalManager.remove(TEMPORAL_PREFIX);
            }

            mTemporalManager.refresh();
        }
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
