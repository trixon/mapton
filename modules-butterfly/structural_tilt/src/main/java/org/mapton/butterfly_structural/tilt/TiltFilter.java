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
package org.mapton.butterfly_structural.tilt;

import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TiltFilter extends FormFilter<TiltManager> {

    IndexedCheckModel mCategoryCheckModel;
    IndexedCheckModel mGroupCheckModel;
    IndexedCheckModel mOperatorCheckModel;
    IndexedCheckModel mOriginCheckModel;
    IndexedCheckModel mStatusCheckModel;
    private final ResourceBundle mBundle = NbBundle.getBundle(TiltFilter.class);
    private final TiltManager mManager = TiltManager.getInstance();

    public TiltFilter() {
        super(TiltManager.getInstance());

        initListeners();
    }

    public void initCheckModelListeners() {
        mGroupCheckModel.getCheckedItems().addListener(mListChangeListener);
        List.of(
                //                mAlarmLevelCheckModel,
                //                mAlarmNameCheckModel,
                mCategoryCheckModel,
                //                mDateFromToCheckModel,
                //                mFrequencyCheckModel,
                mGroupCheckModel,
                //                mMeasCodeCheckModel,
                //                mMeasOperatorsCheckModel,
                //                mMeasNextCheckModel,
                mOperatorCheckModel,
                mOriginCheckModel,
                mStatusCheckModel
        //                mDisruptorCheckModel
        ).forEach(cm -> cm.getCheckedItems().addListener(mListChangeListener));
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateFreeText(p.getName(), p.getGroup(), p.getComment()))
                .filter(p -> validateCheck(mStatusCheckModel, p.getStatus()))
                .filter(p -> validateCheck(mGroupCheckModel, p.getGroup()))
                .filter(p -> validateCheck(mCategoryCheckModel, p.getCategory()))
                .filter(p -> validateCheck(mOperatorCheckModel, p.getOperator()))
                .filter(p -> validateCheck(mOriginCheckModel, p.getOrigin()))
                .filter(p -> validateCoordinateArea(p.getLat(), p.getLon()))
                .filter(p -> validateCoordinateRuler(p.getLat(), p.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);

        getInfoPopOver().loadContent(createInfoContent().renderFormatted());
    }

    private ContainerTag createInfoContent() {
        //TODO Add measOperator+latest
        var map = new LinkedHashMap<String, String>();

        map.put(Dict.TEXT.toString(), getFreeText());
        map.put(Dict.GROUP.toString(), makeInfo(mGroupCheckModel.getCheckedItems()));
//        map.put(Dict.TYPE.toString(), makeInfo(mTypeCheckModel.getCheckedItems()));
//        map.put(mBundle.getString("soilMaterial"), makeInfo(mSoilCheckModel.getCheckedItems()));

        return createHtmlFilterInfo(map);

    }

    private void initListeners() {

    }
}
