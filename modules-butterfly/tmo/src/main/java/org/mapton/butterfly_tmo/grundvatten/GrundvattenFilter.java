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
package org.mapton.butterfly_tmo.grundvatten;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenFilter extends FormFilter<GrundvattenManager> {

    IndexedCheckModel mFiltertypCheckModel;
    IndexedCheckModel mGrundvattenmagasinCheckModel;
    IndexedCheckModel mRörtypCheckModel;
    IndexedCheckModel mStatusCheckModel;
    private final GrundvattenManager mManager = GrundvattenManager.getInstance();

    public GrundvattenFilter() {
        super(GrundvattenManager.getInstance());

        initListeners();
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(g -> StringUtils.isBlank(getFreeText()) || validateFreeText(g))
                .filter(g -> validateCheck(mGrundvattenmagasinCheckModel, g.getGrundvattenmagasin()))
                .filter(g -> validateCheck(mFiltertypCheckModel, g.getFiltertyp()))
                .filter(g -> validateCheck(mRörtypCheckModel, g.getRörtyp()))
                .filter(g -> validateCheck(mStatusCheckModel, g.getStatus()))
                .filter(g -> validateCoordinateArea(g.getLat(), g.getLon()))
                .filter(g -> validateCoordinateRuler(g.getLat(), g.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    void initCheckModelListeners() {
        mGrundvattenmagasinCheckModel.getCheckedItems().addListener(mListChangeListener);
        mFiltertypCheckModel.getCheckedItems().addListener(mListChangeListener);
        mRörtypCheckModel.getCheckedItems().addListener(mListChangeListener);
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    private ContainerTag createInfoContent() {
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(Dict.STATUS.toString(), makeInfo(mStatusCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);
    }

    private void initListeners() {
    }

    private boolean validateFreeText(BGrundvatten b) {
        return StringHelper.matchesSimpleGlobByWord(getFreeText(), true, false,
                b.getBenämning(),
                b.getName(),
                b.getGroup(),
                b.getComment()
        );
    }
}
