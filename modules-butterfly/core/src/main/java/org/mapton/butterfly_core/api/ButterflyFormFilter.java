/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.util.List;
import java.util.stream.Stream;
import javafx.beans.property.SimpleBooleanProperty;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ButterflyFormFilter<ManagerType extends MBaseDataManager> extends FormFilter {

    protected BContentOptions mContentOptions;
    protected BFilterSectionAlarm mFilterSectionAlarm;
    protected BFilterSectionAvg mFilterSectionAvg;
    protected BFilterSectionDate mFilterSectionDate;
    protected BFilterSectionDisruptor mFilterSectionDisruptor;
    protected BFilterSectionMeas mFilterSectionMeas;
    protected BFilterSectionMisc mFilterSectionMisc;
    protected BFilterSectionPoint mFilterSectionPoint;
    protected BFilterSectionTrend mFilterSectionTrend;
    protected final SimpleBooleanProperty mInvalidCoordinateProperty = new SimpleBooleanProperty();
    protected final SimpleBooleanProperty mInvertProperty = new SimpleBooleanProperty();
    protected final SimpleBooleanProperty mInvisibleProperty = new SimpleBooleanProperty();

    public ButterflyFormFilter(MBaseDataManager manager) {
        super(manager);
    }

    public SimpleBooleanProperty invalidCoordinateProperty() {
        return mInvalidCoordinateProperty;
    }

    public SimpleBooleanProperty invertProperty() {
        return mInvertProperty;
    }

    public SimpleBooleanProperty invisibleProperty() {
        return mInvisibleProperty;
    }

    protected <T extends BXyzPoint> Stream<T> filterCommon1(List<T> items) {
        var stream = items.stream()
                .filter(p -> BCoordinatrix.hasValidLatLon(p) != mInvalidCoordinateProperty.get())
                .filter(p -> p.isVisible() != mInvisibleProperty.get())
                .filter(p -> validateFreeText(p.getName(), p.getGroup(), p.getComment(), p.getExternalId()));

        if (!mInvalidCoordinateProperty.get()) {
            stream = stream
                    .filter(p -> validateCoordinateCircle(p.getLat(), p.getLon()))
                    .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                    .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                    .filter(p -> {
                        if (mFilterSectionDisruptor == null) {
                            return true;
                        } else {
                            return mFilterSectionDisruptor.filter(p);
                        }
                    });
        }

        return stream;
    }

    protected <T extends BXyzPoint> List<T> sortAndLimit(List<T> filteredItems) {
        return filteredItems.stream()
                .sorted(mContentOptions.getListSortOrder().getComparator().thenComparing(BListSortOrder.STANDARD.getComparator()))
                .limit(mContentOptions.getListLimit())
                .toList();
    }
}
