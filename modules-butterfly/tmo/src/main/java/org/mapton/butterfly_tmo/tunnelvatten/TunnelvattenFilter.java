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
package org.mapton.butterfly_tmo.tunnelvatten;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.tmo.BTunnelvatten;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TunnelvattenFilter extends FormFilter<TunnelvattenManager> {

    private IndexedCheckModel mGroupCheckModel;
    private final TunnelvattenManager mManager = TunnelvattenManager.getInstance();

    public TunnelvattenFilter() {
        super(TunnelvattenManager.getInstance());

        initListeners();
    }

    public void setCheckModelGroup(IndexedCheckModel checkModel) {
        mGroupCheckModel = checkModel;
        checkModel.getCheckedItems().addListener(mListChangeListener);
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(b -> StringUtils.isBlank(getFreeText()) || validateFreeText(b))
                //                .filter(b -> validateGroup(b.getGroup()))
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

    private boolean validateFreeText(BTunnelvatten b) {
        return StringHelper.matchesSimpleGlobByWord(getFreeText(), true, false,
                b.getBenämning(),
                b.getName(),
                b.getGroup(),
                b.getComment()
        );
    }

    private boolean validateGroup(String s) {
        return validateCheck(mGroupCheckModel, s);
    }

}
