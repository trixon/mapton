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
package org.mapton.butterfly_topo.grade;

import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GradeContentView extends BContentView {

    protected final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    protected final GradeFilterConfig mFilterConfig = new GradeFilterConfig();
    protected GradeManagerBase mManager;
    protected final SubToolBarConfig mSubToolBarConfig = new SubToolBarConfig(null, true);
    private Action mRefreshAction;

    public GradeContentView(BContentOptions contentOptions, ButterflyFormFilter filter) {
        super(contentOptions, filter);

        mRefreshAction = new Action(Dict.REFRESH.toString(), actionEvent -> {
            mRefreshAction.setDisabled(true);
            mManager.load();
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        initListeners();
    }

    public ArrayList<Action> getToolBarActions() {
        var toolBarActions = new ArrayList<>(getDefaultToolBarActions());
        toolBarActions.get(3).setDisabled(true);
        toolBarActions.add(4, mRefreshAction);
        toolBarActions.add(4, ActionUtils.ACTION_SEPARATOR);

        return toolBarActions;
    }

    private void initListeners() {
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            mRefreshAction.setDisabled(false);
        });
    }

}
