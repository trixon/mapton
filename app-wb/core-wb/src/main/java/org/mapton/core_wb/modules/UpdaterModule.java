/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.core_wb.modules;

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.mapton.base.ui.updater.UpdaterView;
import org.mapton.core_wb.api.MWorkbenchModule;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterModule extends MWorkbenchModule {

    private BorderPane mRoot;

    public UpdaterModule() {
        super(Dict.UPDATER.toString(), MaterialDesignIcon.DOWNLOAD);

        createUI();
    }

    @Override
    public Node activate() {
        return mRoot;
    }

    private void createUI() {
        UpdaterView updaterView = new UpdaterView();

        var refreshToolbarItem = new ToolbarItem(Dict.REFRESH.toString(), new MaterialDesignIconView(MaterialDesignIcon.SYNC), event -> {
            updaterView.refreshUpdaters();
        });

        var updateToolbarItem = new ToolbarItem(Dict.UPDATE.toString(), new MaterialDesignIconView(MaterialDesignIcon.DOWNLOAD), event -> {
            updaterView.update();
        });

        getToolbarControlsLeft().addAll(refreshToolbarItem, updateToolbarItem);

        refreshToolbarItem.disableProperty().bind(updaterView.runningProperty());
        updateToolbarItem.disableProperty().bind(updaterView.runningProperty());

        mRoot = new BorderPane(updaterView.getLogPanel());
        mRoot.setLeft(updaterView.getListNode());
    }
}
