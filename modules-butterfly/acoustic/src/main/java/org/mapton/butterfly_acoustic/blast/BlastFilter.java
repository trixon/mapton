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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import javafx.beans.property.SimpleObjectProperty;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class BlastFilter extends FormFilter<BlastManager> {

    private final SimpleObjectProperty<LocalDate> mDateHighProperty = new SimpleObjectProperty();
    private final SimpleObjectProperty<LocalDate> mDateLowProperty = new SimpleObjectProperty();
    IndexedCheckModel mGroupCheckModel;
    private final BlastManager mManager = BlastManager.getInstance();

    public BlastFilter() {
        super(BlastManager.getInstance());

        initListeners();
    }

    public SimpleObjectProperty<LocalDate> dateHighProperty() {
        return mDateHighProperty;
    }

    public SimpleObjectProperty<LocalDate> dateLowProperty() {
        return mDateLowProperty;
    }

    public void initCheckModelListeners() {
        mGroupCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(b -> validateFreeText(b.getName(), b.getGroup(), b.getComment()))
                .filter(b -> validateCheck(mGroupCheckModel, b.getGroup()))
                .filter(b -> validateDate(b.getDateTime()))
                .filter(b -> validateCoordinateArea(b.getLat(), b.getLon()))
                .filter(b -> validateCoordinateRuler(b.getLat(), b.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
        map.put(Dict.FROM.toString(), mDateLowProperty.get().toString());
        map.put(Dict.TO.toString(), mDateHighProperty.get().toString());

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
        mDateLowProperty.addListener(mChangeListenerObject);
        mDateHighProperty.addListener(mChangeListenerObject);
    }

    private boolean validateDate(LocalDateTime lastMeasurementDateTime) {
        if (null != lastMeasurementDateTime) {
            var lowDate = mDateLowProperty.get();
            var highDate = mDateHighProperty.get();
            var valid = DateHelper.isBetween(lowDate, highDate, lastMeasurementDateTime.toLocalDate());

            return valid;
        } else {
            return false;
        }
    }
}
