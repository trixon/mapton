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
package org.mapton.butterfly_topo.grade.vertical;

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
public class GradeVContentView extends GradeContentView {

    public GradeVContentView(BContentOptions contentOptions) {
        super(contentOptions, new GradeFilter(GradeVManager.getInstance(), VerticalContentOptions.getInstance()));
        mManager = GradeVManager.getInstance();
        mFilterConfig.setKeyPrefix("_3");
        mFilterConfig.setMaxDeltaR(GradeVManager.MAX_HORIZONTAL_DISTANCE);
        mFilterConfig.setMinDeltaH(GradeVManager.MIN_VERTICAL_DISTANCE);
        mFilterConfig.setMinGradeVertical(5.0);
        mFilterConfig.setMinGradeHorizontal(10.0);
        mFilterConfig.setAxis(BAxis.VERTICAL);

        mFilterPopOver = new GradeFilterPopOver(getClass(), mFilter, mFilterConfig);
        mLayerOptions = GradeVLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "gradeV");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var subToolBarActions = getDefaultSubToolBarActions(mManager, mSubToolBarConfig);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, mBundle.getString("grade_v"));
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.setFreeTextTooltip(Dict.NAME.toString());
        setListCellFactory(GradeVContentListCell::new);
    }
}
