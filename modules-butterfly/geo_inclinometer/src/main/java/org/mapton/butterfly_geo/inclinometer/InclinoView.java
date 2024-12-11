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
package org.mapton.butterfly_geo.inclinometer;

import java.util.Arrays;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.CopyNamesAction;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPoint;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class InclinoView {

    private final InclinoFilter mFilter = new InclinoFilter();
    private final InclinoFilterPopOver mFilterPopOver = new InclinoFilterPopOver(mFilter);
    private final MFilterPresetPopOver mFilterPresetPopOver = new MFilterPresetPopOver(mFilterPopOver, "crack");
    private final SingleListForm<InclinoManager, BStructuralCrackPoint> mListForm;
    private final InclinoManager mManager = InclinoManager.getInstance();

    public InclinoView() {
        var actions = Arrays.asList(
                new CopyNamesAction(mManager),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                mFilterPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_InclinometerAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new InclinoListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BStructuralCrackPoint> c) -> {
            mFilterPopOver.setNames(mManager.getTimeFilteredItems().stream().map(p -> p.getName()).toList());
        });
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
