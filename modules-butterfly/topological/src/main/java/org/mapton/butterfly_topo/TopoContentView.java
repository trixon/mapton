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

import org.mapton.butterfly_core.api.BContentView;
import java.util.Arrays;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.CopyNamesAction;
import org.mapton.butterfly_core.api.ExternalSearchAction;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.core.api.ui.ExportAction;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoContentView extends BContentView {

    private final SingleListForm<TopoManager, BTopoControlPoint> mListForm;
    private final TopoManager mManager = TopoManager.getInstance();

    public TopoContentView() {
        mFilter = new TopoFilter();
        mFilterPopOver = new TopoFilterPopOver(mFilter);
        mLayerOptions = TopoLayerOptions.getInstance();
        mListSortOrderScb.valueProperty().bindBidirectional(mLayerOptions.listSortOrderProperty());
        var cb = new ComboBox<Integer>();
        cb.getItems().setAll(-1, 1, 5, 10, 25, 50, 100, 1000);
        var box = new VBox(8, mListSortOrderScb, cb);
        cb.prefWidthProperty().bind(mListSortOrderScb.widthProperty());
//        BindingHelper.bindWidthForChildrens(box);
        box.setPadding(FxHelper.getUIScaledInsets(16));
        mFilter.getListSortPopOver().setNode(box);

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "topo");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        mListForm = new SingleListForm<>(mManager, Bundle.CTL_ControlPointAction());
        mListForm.bindSortLabel(mLayerOptions.listSortOrderProperty());
        var actions = Arrays.asList(
                mFilter.getListSortPopOver().getAction(),
                new ExternalSearchAction(mManager),
                new ExportAction(getClass()),
                new CopyNamesAction(mManager),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                mPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new TopoContentListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.CATEGORY.toString(),
                Dict.GROUP.toString(),
                SDict.ALARM.toString()
        );

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
