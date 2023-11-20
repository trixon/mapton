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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.acoustic.BAcoBlast;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BlastFilter extends FormFilter<BlastManager> {

    private final BlastManager mManager = BlastManager.getInstance();
    private final BooleanProperty mProperty = new SimpleBooleanProperty();

    public BlastFilter() {
        super(BlastManager.getInstance());

        initListeners();
    }

    public BooleanProperty property() {
        return mProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(o -> StringUtils.isBlank(getFreeText()) || validateFreeText(o))
                //                .filter(o -> validateDimension(o))
                .filter(o -> validateCoordinateArea(o.getLat(), o.getLon()))
                .filter(o -> validateCoordinateRuler(o.getLat(), o.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    private void initListeners() {
        mProperty.addListener(mChangeListenerObject);
    }

    private boolean validateFreeText(BAcoBlast o) {
        return StringHelper.matchesSimpleGlobByWord(getFreeText(), true, false,
                o.getName(),
                o.getGroup()
        );
    }

}
