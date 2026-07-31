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
package org.mapton.butterfly_rock_convergence;

import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_rock_convergence.api.ConvergenceManager;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceContentView extends BContentView {

//    private final ConvergenceFilter mFilter = new ConvergenceFilter();
//    private final ConvergenceFilterPopOver mFilterPopOver = new ConvergenceFilterPopOver(mFilter);
//    private final MPresetPopOver mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "topo.convergence");
    private final SingleListForm<ConvergenceManager, BRockConvergence> mListForm;
    private final ConvergenceManager mManager = ConvergenceManager.getInstance();

    public ConvergenceContentView(BContentOptions contentOptions) {
        super(contentOptions, new ConvergenceFilter());
        mFilterPopOver = new ConvergenceFilterPopOver(mFilter);
        mLayerOptions = ConvergenceLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "topo.convergence");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var config = new SubToolBarConfig(null, false);
        var subToolBarActions = getDefaultSubToolBarActions(mManager, config);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_ConvergenceAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getDefaultToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new ConvergenceContentListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.GROUP.toString(),
                Dict.COMMENT.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
