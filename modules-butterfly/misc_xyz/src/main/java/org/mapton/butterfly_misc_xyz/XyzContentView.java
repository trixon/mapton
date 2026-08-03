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
package org.mapton.butterfly_misc_xyz;

import java.util.List;
import javafx.scene.layout.Pane;
import org.apache.commons.collections.ListUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.BContentOptions;
import org.mapton.butterfly_core.api.BContentView;
import org.mapton.butterfly_core.api.base.XyzManager;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class XyzContentView extends BContentView {

    private final Action mClearAction;

    private final SingleListForm<XyzManager, BXyzPoint> mListForm;
    private final XyzManager mManager = XyzManager.getInstance();

    public XyzContentView(BContentOptions contentOptions) {
        super(contentOptions, new XyzFilter());
        mFilterPopOver = new XyzFilterPopOver(mFilter);
        mLayerOptions = XyzLayerOptions.getInstance();
        mClearAction = new Action(Dict.CLEAR.toString(), actionEvent -> {
            mManager.clear();
        });
        mClearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));
        var customToolBarActions = List.of(
                ActionUtils.ACTION_SEPARATOR,
                mClearAction
        );

        mPresetPopOver = new MPresetPopOver(mFilterPopOver, MPresetPopOver.PARENT_NODE_FILTER, "xyz");
        mFilterPopOver.setFilterPresetPopOver(mPresetPopOver);
        var config = new SubToolBarConfig(null, true);
        var defaultSubToolBarActions = getDefaultSubToolBarActions(mManager, config);
        var subToolBarActions = ListUtils.sum(defaultSubToolBarActions, customToolBarActions);
        mToolBarPopOver.setToolBar(ActionUtils.createToolBar(subToolBarActions, ActionUtils.ActionTextBehavior.SHOW));

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_XyzAction());
        bindFooterLabel(mListForm);
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(getDefaultToolBarActions());

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new XyzContentListCell());

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
