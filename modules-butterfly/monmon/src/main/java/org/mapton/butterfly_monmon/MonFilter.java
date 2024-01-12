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
package org.mapton.butterfly_monmon;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;

/**
 *
 * @author Patrik Karlström
 */
public class MonFilter extends FormFilter<MonManager> {

    private final SimpleBooleanProperty mLatest14Property = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mLatest14ValueProperty = new SimpleDoubleProperty();

    private final SimpleBooleanProperty mLatest1Property = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mLatest1ValueProperty = new SimpleDoubleProperty();
    private final SimpleBooleanProperty mLatest7Property = new SimpleBooleanProperty();
    private final SimpleDoubleProperty mLatest7ValueProperty = new SimpleDoubleProperty();
    private final MonManager mManager = MonManager.getInstance();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public MonFilter() {
        super(MonManager.getInstance());

        initListeners();
    }

    public SimpleBooleanProperty latest14Property() {
        return mLatest14Property;
    }

    public SimpleDoubleProperty latest14ValueProperty() {
        return mLatest14ValueProperty;
    }

    public SimpleBooleanProperty latest1Property() {
        return mLatest1Property;
    }

    public SimpleDoubleProperty latest1ValueProperty() {
        return mLatest1ValueProperty;
    }

    public SimpleBooleanProperty latest7Property() {
        return mLatest7Property;
    }

    public SimpleDoubleProperty latest7ValueProperty() {
        return mLatest7ValueProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(mon -> validateFreeText(mon.getName(), mon.getStationName()))
                .filter(mon -> mTopoManager.getTimeFilteredItemsMap().containsKey(mon.getName()))
                .filter(mon -> validateQuota(mLatest1Property, mLatest1ValueProperty, mon.getQuota(1)))
                .filter(mon -> validateQuota(mLatest7Property, mLatest7ValueProperty, mon.getQuota(7)))
                .filter(mon -> validateQuota(mLatest14Property, mLatest14ValueProperty, mon.getQuota(14)))
                //                .filter(mon -> validateCheck(mStatusCheckModel, ActHelper.getStatusAsString(mon.getStatus())))
                .filter(mon -> validateCoordinateArea(mon.getLat(), mon.getLon()))
                .filter(mon -> validateCoordinateRuler(mon.getLat(), mon.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    void initCheckModelListeners() {
    }

    private void initListeners() {
        mLatest1Property.addListener(mChangeListenerObject);
        mLatest1ValueProperty.addListener(mChangeListenerObject);
        mLatest7Property.addListener(mChangeListenerObject);
        mLatest7ValueProperty.addListener(mChangeListenerObject);
        mLatest14Property.addListener(mChangeListenerObject);
        mLatest14ValueProperty.addListener(mChangeListenerObject);

        mTopoManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            update();
        });
    }

    private boolean validateQuota(SimpleBooleanProperty enabled, SimpleDoubleProperty valueProperty, double quota) {
        if (enabled.get()) {
            double lim = valueProperty.get();
            double value = Math.abs(quota);

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {
                return value <= Math.abs(lim);
            } else {
                return value >= lim;
            }
        } else {
            return true;
        }
    }
}
