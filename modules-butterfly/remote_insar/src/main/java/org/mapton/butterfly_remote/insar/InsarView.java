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

import java.util.Arrays;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_core.api.CopyNamesAction;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class InsarView {

    private final InsarFilter mFilter = new InsarFilter();
    private final InsarFilterPopOver mFilterPopOver = new InsarFilterPopOver(mFilter);
    private final MPresetPopOver MPresetPopOver = new MPresetPopOver(mFilterPopOver, "insar");
    private final SingleListForm<InsarManager, BRemoteInsarPoint> mListForm;
    private final InsarManager mManager = InsarManager.getInstance();
    private Action mRefreshAction;
    private Action mClearAction;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();

    public InsarView() {
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

        mFilterPopOver.setFilterPresetPopOver(MPresetPopOver);
        var actions = Arrays.asList(
                mRefreshAction,
                mClearAction,
                //                new ExternalSearchAction(mManager),
                new CopyNamesAction(mManager),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                MPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_InsarAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new InsarListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BRemoteInsarPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
