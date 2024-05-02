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
package org.mapton.butterfly_geo_extensometer;

import org.mapton.api.ui.forms.FormFilter;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoFilter extends FormFilter<ExtensoManager> {

    private final ExtensoManager mManager = ExtensoManager.getInstance();

    public ExtensoFilter() {
        super(ExtensoManager.getInstance());

        initListeners();
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(extenso -> validateFreeText(extenso.getName(), extenso.getName()))
                //                .filter(mon -> validateCheck(mStatusCheckModel, ActHelper.getStatusAsString(mon.getStatus())))
                .filter(mon -> validateCoordinateArea(mon.getLat(), mon.getLon()))
                .filter(mon -> validateCoordinateRuler(mon.getLat(), mon.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    void initCheckModelListeners() {
    }

    private void initListeners() {
    }

}
