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
package org.mapton.butterfly_topo.grade.horizontal;

import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_topo.grade.GradeContentView;
import org.mapton.butterfly_topo.grade.GradeFilter;
import org.mapton.butterfly_topo.grade.GradeFilterPopOver;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GradeHContentView extends GradeContentView {

    public GradeHContentView(BContentOptions contentOptions) {
        super(contentOptions, new GradeFilter(GradeHManager.getInstance(), HorizontalContentOptions.getInstance()));
        mManager = GradeHManager.getInstance();
        mFilterConfig.setKeyPrefix("_1");
        mFilterConfig.setMinGradeHorizontal(10.0);
        mFilterConfig.setMaxDeltaH(10.0);
        mFilterConfig.setMaxDeltaR(GradeHManager.MAX_RADIAL_DISTANCE);
        mFilterConfig.setAxis(BAxis.HORIZONTAL);

        mFilterPopOver = new GradeFilterPopOver(getClass(), mFilter, mFilterConfig);
        mLayerOptions = GradeHLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "gradeH");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var subToolBarActions = getDefaultSubToolBarActions(mManager, mSubToolBarConfig);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, mBundle.getString("grade_h"));
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.setFreeTextTooltip(Dict.NAME.toString());
        setListCellFactory(GradeHContentListCell::new);
    }

}
