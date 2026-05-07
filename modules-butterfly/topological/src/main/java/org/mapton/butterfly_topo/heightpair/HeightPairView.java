/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_topo.heightpair;

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.core.api.ui.MPresetPopOver;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class HeightPairView {

    private final ResourceBundle mBundle = NbBundle.getBundle(HeightPairManager.class);
    private final HeightPairFilter mFilter = new HeightPairFilter();
    private final HeightPairFilterPopOver mFilterPopOver = new HeightPairFilterPopOver(mFilter);
    private final SingleListForm mListForm;
    private final HeightPairManager mManager = HeightPairManager.getInstance();
    private final MPresetPopOver mPresetPopOver;
    private Action mRefreshAction;
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public HeightPairView() {
        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "heightpair");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        mListForm = new SingleListForm<>(mManager, mBundle.getString("heightPairs"));
        mRefreshAction = new Action(Dict.REFRESH.toString(), actionEvent -> {
            mRefreshAction.setDisabled(true);
//            new Thread(() -> mManager.load()).start();
            mManager.load();
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));
        var actions = Arrays.asList(
                mRefreshAction,
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new HeightPairListCell());
        mListForm.setFreeTextTooltip(Dict.NAME.toString());

        initListeners();
    }

    public Pane getView() {
        return mListForm.getView();
    }

    private void initListeners() {
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            mRefreshAction.setDisabled(false);
        });
    }
}
