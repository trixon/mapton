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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.controlsfx.control.action.Action;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.GeometryFactory;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.nbp.core.SelectionLockManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class MBaseDataManager<T> {

    private static final Logger LOGGER = Logger.getLogger(MBaseDataManager.class.getName());
    protected final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
    protected final GeometryFactory mGeometryFactory = JTSFactoryFinder.getGeometryFactory();
    protected final SelectionLockManager mSelectionLockManager = SelectionLockManager.getInstance();

    private final String TEMPORAL_PREFIX;
    private final LinkedHashMap<Object, T> mAllItemsMap = new LinkedHashMap<>();
    private final ObjectProperty<ObservableList<T>> mAllItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mAllItemsSet = new HashSet<>();
    private final DelayedResetRunner mDelayedResetRunner;
    private final LinkedHashMap<Object, T> mFilteredItemsMap = new LinkedHashMap<>();
    private final ObjectProperty<ObservableList<T>> mFilteredItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mFilteredItemsSet = new HashSet<>();
    private Boolean mInitialTemporalState = null;
    private boolean mLayerBundleEnabled;
    private T mOldSelectedValue;
    private final ObjectProperty<T> mSelectedItemProperty = new SimpleObjectProperty<>();
    private MTemporalRange mStoredTemporalRange;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private final ObjectProperty<MTemporalRange> mTemporalRangeProperty = new SimpleObjectProperty<>();
    private final LinkedHashMap<Object, T> mTimeFilteredItemsMap = new LinkedHashMap<>();
    private final ObjectProperty<ObservableList<T>> mTimeFilteredItemsProperty = new SimpleObjectProperty<>();
    private final HashSet<T> mTimeFilteredItemsSet = new HashSet<>();
    private final Class<T> mTypeParameterClass;
    private final DelayedResetRunner mUnlockDelayedResetRunner;
    private transient final HashMap<Object, Object> mValues = new HashMap<>();

    private final Action mZoomExtentsAction;

    public MBaseDataManager(Class<T> typeParameterClass) {
        mTypeParameterClass = typeParameterClass;
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

        mZoomExtentsAction = new Action(Dict.ZOOM_EXTENTS.toString(), actionEvent -> {
            var extents = getTimeFilteredExtents();
            if (extents != null) {
                Mapton.getEngine().fitToBounds(extents);
            }
        });
        mZoomExtentsAction.setGraphic(MaterialIcon._Action.ALL_OUT.getImageView(getIconSizeToolBarInt()));
    }

    public ObjectProperty<ObservableList<T>> allItemsProperty() {
        return mAllItemsProperty;
    }

    public ObjectProperty<ObservableList<T>> filteredItemsProperty() {
        return mFilteredItemsProperty;
    }

    public Action geZoomExtentstAction() {
        return mZoomExtentsAction;
    }

    public synchronized final ObservableList<T> getAllItems() {
        return mAllItemsProperty.get();
    }

    public synchronized LinkedHashMap<Object, T> getAllItemsMap() {
        return mAllItemsMap;
    }

    public synchronized HashSet<T> getAllItemsSet() {
        return mAllItemsSet;
    }

    public T getFilteredItemForKey(Object key) {
        return mFilteredItemsMap.get(key);
    }

    public synchronized ObservableList<T> getFilteredItems() {
        return mFilteredItemsProperty.get();
    }

    public synchronized LinkedHashMap<Object, T> getFilteredItemsMap() {
        return mFilteredItemsMap;
    }

    public synchronized HashSet<T> getFilteredItemsSet() {
        return mFilteredItemsSet;
    }

    public T getItemForKey(Object key) {
        return mAllItemsMap.get(key);
    }

    public MLatLonBox getLatLonBoxForItem(T t) {
        return null;
    }

    public MLatLon getLatLonForItem(T t) {
        return null;
    }

    public Object getMapIndicator(T t) {
        return null;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public Object getObjectChart(T t) {
        return Boolean.FALSE;
    }

    public Object getObjectProperties(T t) {
        return Boolean.FALSE;
    }

    public Object getObjectTrends(T t) {
        return Boolean.FALSE;
    }

    public T getSelectedItem() {
        return mSelectedItemProperty.get();
    }

    public MTemporalManager getTemporalManager() {
        return mTemporalManager;
    }

    public MTemporalRange getTemporalRange() {
        return mTemporalRangeProperty.get();
    }

    public T getTimeFilteredItemForKey(Object key) {
        return mTimeFilteredItemsMap.get(key);
    }

    public synchronized ObservableList<T> getTimeFilteredItems() {
        return mTimeFilteredItemsProperty.get();
    }

    public synchronized LinkedHashMap<Object, T> getTimeFilteredItemsMap() {
        return mTimeFilteredItemsMap;
    }

    public synchronized HashSet<T> getTimeFilteredItemsSet() {
        return mTimeFilteredItemsSet;
    }

    public Class<T> getTypeParameterClass() {
        return mTypeParameterClass;
    }

    public Object getValue(Object key) {
        return getValues().get(key);
    }

    public Object getValue(Object key, Object defaultValue) {
        return getValues().getOrDefault(key, defaultValue);
    }

    public <T> T getValue(String key, Class<T> type) {
        return type.cast(mValues.get(key));
    }

    public <T> T getValue(String key) {
        return (T) (mValues.get(key));
    }

    public HashMap<Object, Object> getValues() {
        return mValues;
    }

    public void initAllItems(ArrayList<T> items) {
        setItemsAll(items);
        setItemsFiltered(items);
        setItemsTimeFiltered(items);
    }

    public boolean isLayerBundleEnabled() {
        return mLayerBundleEnabled;
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

    public void setInitialTemporalState(boolean initialTemporalState) {
        mInitialTemporalState = initialTemporalState;
        updateTemporal(initialTemporalState);
    }

    public void setItemsAll(List<T> items) {
        synchronized (getAllItems()) {
            getAllItems().setAll(items);
        }
    }

    public void setItemsFiltered(List<T> items) {
        synchronized (getFilteredItems()) {
            getFilteredItems().setAll(items);
        }
    }

    public void setItemsTimeFiltered(List<T> items) {
        synchronized (getTimeFilteredItems()) {
            getTimeFilteredItems().setAll(items);
        }
    }

    public void setSelectedItem(T item) {
        FxHelper.runLater(() -> {
            mSelectedItemProperty.set(item);
        });
    }

    public void setSelectedItemAfterReset(T item) {
        FxHelper.runLater(() -> {
            mSelectedItemProperty.set(null);
            mSelectedItemProperty.set(item);
        });
    }

    public void setTemporalRange(LocalDate first, LocalDate last) {
        setTemporalRange(new MTemporalRange(first, last));
    }

    public void setTemporalRange(MTemporalRange temporalRange) {
        mStoredTemporalRange = temporalRange;
        mTemporalManager.put(TEMPORAL_PREFIX, temporalRange);
        mTemporalRangeProperty.set(temporalRange);

        if (mInitialTemporalState != null) {
            updateTemporal(mInitialTemporalState);
            mInitialTemporalState = null;
        }
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

    public Object setValue(Object key, Object value) {
        return mValues.put(key, value);
    }

    public ObjectProperty<MTemporalRange> temporalRangeProperty() {
        return mTemporalRangeProperty;
    }

    public ObjectProperty<ObservableList<T>> timeFilteredItemsProperty() {
        return mTimeFilteredItemsProperty;
    }

    public void updateTemporal(boolean layerBundleEnabled) {
        if (layerBundleEnabled) {
            if (mStoredTemporalRange != null) {
                mTemporalManager.put(TEMPORAL_PREFIX, mStoredTemporalRange);
            }
        } else {
            mTemporalManager.remove(TEMPORAL_PREFIX);
        }
        mLayerBundleEnabled = layerBundleEnabled;
        mTemporalManager.refresh();
    }

    protected abstract void applyTemporalFilter();

    protected MLatLonBox getTimeFilteredExtents() {
        return null;
    }

    protected abstract void load(ArrayList<T> items);

    private void init() {
    }

    private void initListeners() {
        mTemporalManager.lowDateProperty().addListener((p, o, n) -> {
            mDelayedResetRunner.reset();
        });

        mTemporalManager.highDateProperty().addListener((p, o, n) -> {
            mDelayedResetRunner.reset();
        });

        getFilteredItems().addListener((ListChangeListener.Change<? extends T> c) -> {
            mDelayedResetRunner.reset();
        });

        mSelectedItemProperty.addListener((p, o, n) -> {
            if (n != null) {
                mOldSelectedValue = n;
            }

            var objectProperties = getObjectProperties(n);
            if (objectProperties != Boolean.FALSE) {
                Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, objectProperties);
            }

            var objectTrends = getObjectTrends(n);
            if (objectTrends != Boolean.FALSE) {
                Mapton.getGlobalState().put(MKey.OBJECT_TRENDS, objectTrends);
            }

            var objectChart = getObjectChart(n);
            if (objectChart != Boolean.FALSE) {
                Mapton.getGlobalState().put(MKey.CHART, objectChart);
            }

            var mapIndicator = getMapIndicator(n);
            Mapton.getGlobalState().put(MKey.INDICATOR_LAYER_LOAD, mapIndicator);
        });
    }
}
