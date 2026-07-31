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
package org.mapton.butterfly_structural.load;

import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class LoadContentView extends BContentView {

    private final SingleListForm<LoadManager, BStructuralLoadCellPoint> mListForm;
    private final LoadManager mManager = LoadManager.getInstance();

    public LoadContentView(BContentOptions contentOptions) {
        super(contentOptions, new LoadFilter());
        mFilterPopOver = new LoadFilterPopOver(mFilter);
        mLayerOptions = LoadLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "load");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var config = new SubToolBarConfig(null, false);
        var subToolBarActions = getDefaultSubToolBarActions(mManager, config);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_LoadAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getDefaultToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new LoadContentListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BStructuralLoadCellPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
