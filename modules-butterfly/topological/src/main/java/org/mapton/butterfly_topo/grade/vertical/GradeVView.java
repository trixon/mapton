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

import java.util.Arrays;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_topo.grade.GradeFilter;
import org.mapton.butterfly_topo.grade.GradeFilterConfig;
import org.mapton.butterfly_topo.grade.GradeFilterPopOver;
import org.mapton.butterfly_topo.grade.GradeView;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVView extends GradeView {

    public GradeVView() {
        mManager = GradeVManager.getInstance();
        mFilter = new GradeFilter(mManager);
        var config = new GradeFilterConfig();
        config.setKeyPrefix("_3");
        config.setMaxDeltaR(GradeVManager.MAX_HORIZONTAL_DISTANCE);
        config.setMinDeltaH(GradeVManager.MIN_VERTICAL_DISTANCE);
        config.setMinGradeVertical(5.0);
        config.setMinGradeHorizontal(10.0);
        config.setAxis(BAxis.VERTICAL);

        mFilterPopOver = new GradeFilterPopOver(getClass(), mFilter, config);
        mFilterPresetPopOver = new MFilterPresetPopOver(mFilterPopOver, "gradeV");

        var actions = Arrays.asList(
                mRefreshAction,
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mFilterPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );
        mListForm = new SingleListForm<>(mManager, mBundle.getString("grade_v"));
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new GradeVListCell());
        mListForm.setFreeTextTooltip(Dict.NAME.toString());
    }

    public Pane getView() {
        return mListForm.getView();
    }
}
