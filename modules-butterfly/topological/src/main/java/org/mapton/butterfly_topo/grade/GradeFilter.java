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
package org.mapton.butterfly_topo.grade;

import j2html.tags.ContainerTag;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDateProvider;
import org.mapton.butterfly_core.api.BFilterSectionMiscProvider;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GradeFilter extends ButterflyFormFilter<GradeManagerBase> implements
        BFilterSectionDateProvider,
        FilterSectionMeasProvider,
        BFilterSectionMiscProvider {

    private FilterSectionMeas mFilterSectionMeas;
    private final GradeManagerBase mManager;

    public GradeFilter(GradeManagerBase manager) {
        super(manager);
        mManager = manager;
        initListeners();
    }

    @Override
    public void setFilterSection(BFilterSectionDate filterSection) {
        mFilterSectionDate = filterSection;
        mFilterSectionDate.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void setFilterSection(FilterSectionMeas filterSection) {
        mFilterSectionMeas = filterSection;
        mFilterSectionMeas.initListeners(mChangeListenerObject, mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> p.isVisible() != mInvisibleProperty.get())
                .filter(p -> validateFreeText(p.getName()))
                //                .filter(p -> mFilterSectionDate.filter(p, p.ext().getDateFirst()))
                .filter(p -> mFilterSectionMeas.filter(p))
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
//        mFilterSectionDate.createInfoContent(map);
        mFilterSectionMeas.createInfoContent(map);

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
        List.of(
                mInvertProperty,
                mInvisibleProperty
        ).forEach(propertyBase -> propertyBase.addListener(mChangeListenerObject));
    }
}
