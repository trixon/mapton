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
package org.mapton.butterfly_remote.insar;

import java.util.List;
import javafx.collections.ListChangeListener;
import org.apache.commons.collections.ListUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class InsarContentView extends BContentView {

    private final InsarManager mManager = InsarManager.getInstance();
    private Action mRefreshAction;
    private final Action mClearAction;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();

    public InsarContentView(BContentOptions contentOptions) {
        super(contentOptions, new InsarFilter());
        mFilterPopOver = new InsarFilterPopOver(mFilter);
        mLayerOptions = InsarLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "insar");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        mRefreshAction = new Action(Dict.REFRESH.toString(), actionEvent -> {
            mRefreshAction.setDisabled(true);
            mManager.load2(mButterflyManager.getButterfly());
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));
        mClearAction = new Action(Dict.CLEAR.toString(), actionEvent -> {
            mManager.clear();
            SystemHelper.runGcDelayed(500);
            mRefreshAction.setDisabled(false);
        });
        mClearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));
        mButterflyManager.butterflyProperty().addListener((p, o, n) -> {
            mRefreshAction.setDisabled(false);
        });

        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var config = new SubToolBarConfig(null, false);

        var defaultSubToolBarActions = getDefaultSubToolBarActions(mManager, config);
        var customToolBarActions = List.of(
                ActionUtils.ACTION_SEPARATOR,
                mRefreshAction,
                mClearAction
        );
        var subToolBarActions = ListUtils.sum(defaultSubToolBarActions, customToolBarActions);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_InsarAction());
        bindFooterLabel(mListForm);
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getDefaultToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );

        setListCellFactory(InsarContentListCell::new);

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BRemoteInsarPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

}
