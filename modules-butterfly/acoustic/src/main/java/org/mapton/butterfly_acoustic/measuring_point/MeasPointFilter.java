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
package org.mapton.butterfly_acoustic.measuring_point;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointFilter extends FormFilter<MeasPointManager> {

    IndexedCheckModel mCategoryCheckModel;
    IndexedCheckModel mGroupCheckModel;
    IndexedCheckModel mSoilCheckModel;
    IndexedCheckModel mStatusCheckModel;
    private final ResourceBundle mBundle = NbBundle.getBundle(MeasPointFilter.class);
    private final MeasPointManager mManager = MeasPointManager.getInstance();

    public MeasPointFilter() {
        super(MeasPointManager.getInstance());

        initListeners();
    }

    public void initCheckModelListeners() {
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
        mGroupCheckModel.getCheckedItems().addListener(mListChangeListener);
        mCategoryCheckModel.getCheckedItems().addListener(mListChangeListener);
        mSoilCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(b -> validateFreeText(b.getName(), b.getGroup(), b.getComment(), b.getAddress()))
                .filter(b -> validateCheck(mStatusCheckModel, b.getStatus()))
                .filter(b -> validateCheck(mGroupCheckModel, b.getGroup()))
                .filter(b -> validateCheck(mCategoryCheckModel, b.getCategory()))
                .filter(b -> validateCheck(mSoilCheckModel, b.getSoilMaterial()))
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
        map.put(Dict.STATUS.toString(), makeInfo(mStatusCheckModel.getCheckedItems()));
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
        map.put(Dict.CATEGORY.toString(), makeInfo(mCategoryCheckModel.getCheckedItems()));
        map.put(mBundle.getString("soilMaterial"), makeInfo(mSoilCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);

    }

    private void initListeners() {
    }
}
