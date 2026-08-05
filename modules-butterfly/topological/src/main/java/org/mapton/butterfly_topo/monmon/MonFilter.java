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
package org.mapton.butterfly_topo.monmon;

import j2html.tags.ContainerTag;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDateProvider;
import org.mapton.butterfly_core.api.BFilterSectionMiscProvider;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BFilterSectionPointProvider;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MonFilter extends ButterflyFormFilter<MonManager> implements
        BFilterSectionMiscProvider,
        BFilterSectionPointProvider,
        BFilterSectionDateProvider {

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
        mContentOptions = MonContentOptions.getInstance();

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
    public void setFilterSection(BFilterSectionDate filterSectionDate) {
        mFilterSectionDate = filterSectionDate;
        mFilterSectionDate.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(BFilterSectionPoint filterSection) {
        mFilterSectionPoint = filterSection;
        mFilterSectionPoint.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void update() {
//        var filteredItems = mManager.getAllItems().stream()
//                .filter(mon -> mTopoManager.getTimeFilteredItemsMap().containsKey(mon.getName()))
//                .filter(mon -> validateQuota(mLatest1Property, mLatest1ValueProperty, mon.getQuota(1)))
//                .filter(mon -> validateQuota(mLatest7Property, mLatest7ValueProperty, mon.getQuota(7)))
//                .filter(mon -> validateQuota(mLatest14Property, mLatest14ValueProperty, mon.getQuota(14)))
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> p.isVisible() != mInvisibleProperty.get())
                .filter(p -> validateFreeText(p.getName(), p.getGroup(), p.getComment(), p.getStationName()))
                .filter(p -> validateCoordinateCircle(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .filter(p -> mFilterSectionPoint.filter(p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS)))
                .filter(p -> mFilterSectionDate.filter(p.getControlPoint(), p.getControlPoint().ext().getDateFirst()))
                .toList();

        if (mInvertProperty.get()) {
            var toBeExluded = new HashSet<>(filteredItems);
            filteredItems = mManager.getAllItems().stream()
                    .filter(p -> !toBeExluded.contains(p))
                    .toList();
        }

        filteredItems = sortAndLimit(filteredItems);

        mManager.setItemsFiltered(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    void initCheckModelListeners() {
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();
        map.put(Dict.TEXT.toString(), getFreeText());
        mFilterSectionPoint.createInfoContent(map);
        mFilterSectionDate.createInfoContent(map);

        return createHtmlFilterInfo(map);
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

        List.of(
                mInvertProperty,
                mInvisibleProperty,
                mContentOptions.listSortOrderProperty(),
                mContentOptions.listLimitProperty()
        ).forEach(propertyBase -> propertyBase.addListener(mChangeListenerObject));
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
