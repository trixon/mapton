/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.tilt;

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.scene.control.Tab;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHView {

    private final TiltHFilter mFilter = new TiltHFilter();
    private final TiltHFilterPopOver mFilterPopOver = new TiltHFilterPopOver(mFilter);
    private final TiltHFavoritePopOver mFilterFavoritePopOver = new TiltHFavoritePopOver(mFilterPopOver);
    private final SingleListForm mListForm;
    private final TiltHManager mManager = TiltHManager.getInstance();
    private final ResourceBundle mBundle = NbBundle.getBundle(TiltHManager.class);
    private Tab mTab;

    public TiltHView() {
        var actions = Arrays.asList(
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mFilterFavoritePopOver.getAction(),
                mFilterPopOver.getAction()
        );
        mListForm = new SingleListForm<>(mManager, mBundle.getString("tilt_h"));
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new TiltHListCell());
        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );

    }

    public Tab getView() {
        if (mTab == null) {
            mTab = new Tab(mBundle.getString("tilt_h"), mListForm.getView());
            mManager.selectedItemProperty().addListener((p, o, n) -> {
                mTab.getTabPane().getSelectionModel().select(mTab);
            });
        }

        return mTab;
    }
}
