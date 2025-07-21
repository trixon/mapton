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

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.grade.GradeFilter;
import org.mapton.butterfly_topo.grade.GradeFilterConfig;
import org.mapton.butterfly_topo.grade.GradeFilterPopOver;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GradeHView {

    private final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    private final GradeFilterPopOver mFilterPopOver;
    private final MFilterPresetPopOver mFilterPresetPopOver;
    private final SingleListForm mListForm;
    private final GradeHManager mManager = GradeHManager.getInstance();
    private final GradeFilter mFilter = new GradeFilter(mManager);

    public GradeHView() {
        var config = new GradeFilterConfig();
        config.setKeyPrefix("_1");
        config.setMinGradeHorizontal(10.0);
        config.setMaxDeltaH(10.0);
        config.setMaxDeltaR(GradeHManager.MAX_RADIAL_DISTANCE);
        config.setDimension(BDimension._1d);

        mFilterPopOver = new GradeFilterPopOver(getClass(), mFilter, config);
        mFilterPresetPopOver = new MFilterPresetPopOver(mFilterPopOver, "gradeH");

        var actions = Arrays.asList(
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mFilterPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );
        mListForm = new SingleListForm<>(mManager, mBundle.getString("grade_h"));
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new GradeHListCell());
        mListForm.setFreeTextTooltip(Dict.NAME.toString());
    }

    public Pane getView() {
        return mListForm.getView();
    }
}
