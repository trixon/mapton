/*
 * Copyright 2025 Patrik Karlström.
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

import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import org.controlsfx.control.action.Action;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class GradeView {

    protected ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    protected GradeFilter mFilter;
    protected GradeFilterPopOver mFilterPopOver;
    protected MFilterPresetPopOver mFilterPresetPopOver;
    protected SingleListForm mListForm;
    protected GradeManagerBase mManager;
    protected Action mRefreshAction;
    protected final TopoManager mTopoManager = TopoManager.getInstance();

    public GradeView() {
        mRefreshAction = new Action(Dict.REFRESH.toString(), actionEvent -> {
            mRefreshAction.setDisabled(true);
//            new Thread(() -> mManager.load()).start();
            mManager.load();
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        initListeners();
    }

    private void initListeners() {
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            mRefreshAction.setDisabled(false);
        });
    }

}
