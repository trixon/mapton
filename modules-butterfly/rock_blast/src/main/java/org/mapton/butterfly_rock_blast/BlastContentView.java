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
package org.mapton.butterfly_rock_blast;

import java.util.Arrays;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_core.api.ExternalSearchAction;
import org.mapton.butterfly_format.types.rock.BRockBlast;
import org.mapton.core.api.ui.ExportAction;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class BlastContentView extends BContentView {

    private final SingleListForm<BlastManager, BRockBlast> mListForm;
    private final BlastManager mManager = BlastManager.getInstance();

    public BlastContentView(BContentOptions contentOptions) {
        super(contentOptions, new BlastFilter());
        mFilterPopOver = new BlastFilterPopOver(mFilter);
        mLayerOptions = BlastLayerOptions.getInstance();

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "blast");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var actions = Arrays.asList(
                mFilter.getListSortPopOver().getAction(),
                new ExternalSearchAction(mManager),
                new ExportAction("Salvor"),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                mPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_BlastAction());
        bindFooterLabel(mListForm);
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new BlastContentListCell());

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
