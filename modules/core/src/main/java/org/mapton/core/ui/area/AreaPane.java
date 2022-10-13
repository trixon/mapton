/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.core.ui.area;

import java.util.Arrays;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MArea;
import org.mapton.api.MAreaManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AreaPane extends BorderPane {

    private final MAreaManager mAreaManager = MAreaManager.getInstance();
    private CheckModel<TreeItem<MArea>> mCheckModel;
    private CheckBoxTreeItem<MArea> mRootItem;
    private CheckTreeView<MArea> mTreeView;

    public AreaPane() {
        createUI();
    }

    private void createUI() {
        var browseAction = new Action(Dict.BROWSE.toString(), actionEvent -> {
        });
        browseAction.setGraphic(MaterialIcon._Social.PUBL.getImageView(getIconSizeToolBarInt()));
        browseAction.setDisabled(true);

        var actions = Arrays.asList(
                browseAction,
                ActionUtils.ACTION_SPAN
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        var rootArea = new MArea("");
        mRootItem = new CheckBoxTreeItem<>(rootArea);
        mTreeView = new CheckTreeView<>(mRootItem);
        mCheckModel = mTreeView.getCheckModel();
        mTreeView.setShowRoot(false);

        setTop(toolBar);
        setCenter(mTreeView);
    }

}
