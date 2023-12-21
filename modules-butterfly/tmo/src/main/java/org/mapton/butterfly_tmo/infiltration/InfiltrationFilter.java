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
package org.mapton.butterfly_tmo.infiltration;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class InfiltrationFilter extends FormFilter<InfiltrationManager> {

    private IndexedCheckModel mGroupCheckModel;
    private final InfiltrationManager mManager = InfiltrationManager.getInstance();

    public InfiltrationFilter() {
        super(InfiltrationManager.getInstance());

        initListeners();
    }

    public void setCheckModelGroup(IndexedCheckModel checkModel) {
        mGroupCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(b -> validateFreeText(b.getName(), b.getGroup(), b.getComment()))
                .filter(b -> validateCoordinateArea(b.getLat(), b.getLon()))
                .filter(b -> validateCoordinateRuler(b.getLat(), b.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);

    }

    private void initListeners() {
    }
}
