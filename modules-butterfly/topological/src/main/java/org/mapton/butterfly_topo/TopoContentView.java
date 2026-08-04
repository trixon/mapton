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
package org.mapton.butterfly_topo;

import javafx.collections.ListChangeListener;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoContentView extends BContentView {

    private final TopoManager mManager = TopoManager.getInstance();

    public TopoContentView(BContentOptions contentOptions) {
        super(contentOptions, new TopoFilter());

        mFilterPopOver = new TopoFilterPopOver(mFilter);
        mLayerOptions = TopoLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "topo");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        mListForm = new SingleListForm<>(mManager, Bundle.CTL_ControlPointAction());
        bindFooterLabel(mListForm);

        var config = new SubToolBarConfig(getClass(), false);
        var subToolBarActions = getDefaultSubToolBarActions(mManager, config);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getDefaultToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.CATEGORY.toString(),
                Dict.GROUP.toString(),
                SDict.ALARM.toString()
        );

        setListCellFactory(TopoContentListCell.class);

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

}
