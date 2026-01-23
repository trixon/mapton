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
package org.mapton.butterfly_geo_predrilling;

import java.util.Arrays;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.ExternalSearchAction;
import org.mapton.butterfly_format.types.geo.BGeoPreDrillPoint;
import org.mapton.core.api.ui.ExportAction;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class PreDrillView {

    private final PreDrillFilter mFilter = new PreDrillFilter();
    private final PreDrillFilterPopOver mFilterPopOver = new PreDrillFilterPopOver(mFilter);
    private final SingleListForm<PreDrillManager, BGeoPreDrillPoint> mListForm;
    private final PreDrillManager mManager = PreDrillManager.getInstance();
    private final MPresetPopOver MPresetPopOver = new MPresetPopOver(mFilterPopOver, "preDrill");

    public PreDrillView() {
        mFilterPopOver.setFilterPresetPopOver(MPresetPopOver);
        var actions = Arrays.asList(
                new ExternalSearchAction(mManager),
                new ExportAction("Salvor"),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                MPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_PreDrillAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new PreDrillListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.GROUP.toString(),
                Dict.COMMENT.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
