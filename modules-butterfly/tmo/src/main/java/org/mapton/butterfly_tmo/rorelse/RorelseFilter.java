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
package org.mapton.butterfly_tmo.rorelse;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_tmo.api.RorelseManager;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RorelseFilter extends FormFilter<RorelseManager> {

    IndexedCheckModel<String> mFixpunktCheckModel;
    IndexedCheckModel<String> mInformationskallorCheckModel;
    IndexedCheckModel<String> mPlaceringCheckModel;
    IndexedCheckModel<String> mStatusCheckModel;

    private final RorelseManager mManager = RorelseManager.getInstance();

    public RorelseFilter() {
        super(RorelseManager.getInstance());

        initListeners();
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(r -> validateFreeText(r.getBenämning(), r.getLägesbeskrivning(), r.getPlacering(), r.getPlacering_kommentar(), r.getGroup(), r.getComment()))
                .filter(r -> validateCheck(mPlaceringCheckModel, r.getPlacering()))
                .filter(r -> validateCheck(mFixpunktCheckModel, r.getFixpunkt()))
                .filter(r -> validateCheck(mStatusCheckModel, r.getStatus()))
                .filter(r -> validateCheck(mInformationskallorCheckModel, r.getInformationskällor()))
                //                .filter(r -> !r.ext().getObservationsAllRaw().isEmpty())
                .filter(r -> validateCoordinateCircle(r.getLat(), r.getLon()))
                .filter(r -> validateCoordinateArea(r.getLat(), r.getLon()))
                .filter(r -> validateCoordinateRuler(r.getLat(), r.getLon()))
                .toList();

        mManager.setItemsFiltered(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    void initCheckModelListeners() {
        mFixpunktCheckModel.getCheckedItems().addListener(mListChangeListener);
        mPlaceringCheckModel.getCheckedItems().addListener(mListChangeListener);
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
        mInformationskallorCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put("Placering", makeInfo(mPlaceringCheckModel.getCheckedItems()));
        map.put("Status", makeInfo(mStatusCheckModel.getCheckedItems()));
        map.put("Fixpunkt", makeInfo(mFixpunktCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);

    }

    private void initListeners() {
    }
}
