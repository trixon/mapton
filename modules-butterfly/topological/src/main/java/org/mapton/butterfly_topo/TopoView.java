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

import java.util.Arrays;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListForm;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.ManagedList;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.tilt.TiltHManager;
import org.mapton.butterfly_topo.tilt.TiltHView;
import org.mapton.core.api.ui.ExportAction;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public class TopoView {

    private final TopoFilter mFilter = new TopoFilter();
    private final TopoFilterPopOver mFilterPopOver = new TopoFilterPopOver(mFilter);
    private final TopoFilterFavoritePopOver mFilterFavoritePopOver = new TopoFilterFavoritePopOver(mFilterPopOver);
    private final ListForm mListForm;
    private final TopoManager mManager = TopoManager.getInstance();

    public TopoView() {
        mListForm = new ListForm(Bundle.CTL_ControlPointAction());
        var pointManagedList = new ManagedList<TopoManager, BTopoControlPoint>(mManager);
        var pointTab = new Tab(SDict.POINTS.toString(), pointManagedList.getView());
        var tiltHTab = new Tab(NbBundle.getMessage(TiltHManager.class, "tilt_h"), new TiltHView().getView());
        var tabPane = new TabPane(pointTab, tiltHTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mListForm.setContent(tabPane);

        var actions = Arrays.asList(
                new ExportAction(getClass()),
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mFilterFavoritePopOver.getAction(),
                mFilterPopOver.getAction()
        );

        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        pointManagedList.getListView().setCellFactory(listView -> new TopoListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.CATEGORY.toString(),
                Dict.GROUP.toString(),
                SDict.ALARM.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
