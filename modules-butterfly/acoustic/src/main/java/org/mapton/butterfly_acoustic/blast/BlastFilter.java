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
package org.mapton.butterfly_acoustic.blast;

import j2html.tags.ContainerTag;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDateProvider;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BFilterSectionPointProvider;
import org.mapton.butterfly_core.api.FilterSectionMiscProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class BlastFilter extends FormFilter<BlastManager> implements
        FilterSectionMiscProvider,
        BFilterSectionPointProvider,
        BFilterSectionDateProvider {

    private BFilterSectionDate mFilterSectionDate;
    private BFilterSectionPoint mFilterSectionPoint;
    private final SimpleBooleanProperty mInvertProperty = new SimpleBooleanProperty();
    private final BlastManager mManager = BlastManager.getInstance();

    public BlastFilter() {
        super(BlastManager.getInstance());

        initListeners();
    }

    public void initCheckModelListeners() {
    }

    @Override
    public SimpleBooleanProperty invertProperty() {
        return mInvertProperty;
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
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateFreeText(p.getName(), p.getGroup(), p.getComment(), p.getExternalId()))
                .filter(p -> validateCoordinateCircle(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .filter(p -> mFilterSectionPoint.filter(p, p.ext().getMeasurementUntilNext(ChronoUnit.DAYS)))
                .filter(p -> mFilterSectionDate.filter(p, p.ext().getDateFirst()))
                .toList();

        if (mInvertProperty.get()) {
            var toBeExluded = new HashSet<>(filteredItems);
            filteredItems = mManager.getAllItems().stream()
                    .filter(p -> !toBeExluded.contains(p))
                    .toList();
        }

        mManager.setItemsFiltered(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();
        map.put(Dict.TEXT.toString(), getFreeText());
        mFilterSectionPoint.createInfoContent(map);
        mFilterSectionDate.createInfoContent(map);

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
        List.of(
                mInvertProperty
        ).forEach(propertyBase -> propertyBase.addListener(mChangeListenerObject));
    }
}
