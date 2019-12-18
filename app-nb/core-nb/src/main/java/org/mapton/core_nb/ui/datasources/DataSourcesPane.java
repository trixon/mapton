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
package org.mapton.core_nb.ui.datasources;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDataSourceInitializer;
import org.mapton.api.MKey;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.DataSourceTab;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class DataSourcesPane extends BorderPane {

    private DataSourceTab mFileTab;
    private TabPane mTabPane;
    private ToolBar mToolBar;
    private DataSourceTab mWmsSourceTab;
    private DataSourceTab mWmsStyleTab;

    public DataSourcesPane() {
        createUI();
        init();
    }

    void save() {
        Platform.runLater(() -> {
            mFileTab.save();
            mWmsSourceTab.save();
            mWmsStyleTab.save();
        });
    }

    private void apply() {
        save();
    }

    private void createUI() {
        initToolBar();
        String[] wmsExts = new String[]{"json"};

        mFileTab = new DataSourceTab(Dict.FILE.toString(), MKey.DATA_SOURCES_FILES, null);
        mWmsSourceTab = new DataSourceTab("WMS " + Dict.SOURCE.toString(), MKey.DATA_SOURCES_WMS_SOURCES, wmsExts);
        mWmsStyleTab = new DataSourceTab("WMS " + Dict.STYLE.toString(), MKey.DATA_SOURCES_WMS_STYLES, wmsExts);
        mTabPane = new TabPane(mWmsSourceTab, mWmsStyleTab, mFileTab);

        for (Tab tab : mTabPane.getTabs()) {
            tab.setClosable(false);
        }

        setCenter(mTabPane);
    }

    private void init() {
        mFileTab.load("");
        mWmsSourceTab.load(MDataSourceInitializer.getDefaultSources());
        mWmsStyleTab.load(MDataSourceInitializer.getDefaultStyles());

        apply();
    }

    private void initToolBar() {
        Action applyAction = new Action(Dict.APPLY.toString(), (event) -> {
            apply();
        });
        applyAction.setGraphic(MaterialIcon._Navigation.CHECK.getImageView(getIconSizeToolBarInt()));

        Action restoreDefaultsAction = new Action(Dict.RESTORE_DEFAULTS.toString(), (event) -> {
            DataSourceTab t = (DataSourceTab) mTabPane.getSelectionModel().getSelectedItem();
            t.restoreDefaults();
        });
        restoreDefaultsAction.setGraphic(MaterialIcon._Action.SETTINGS_BACKUP_RESTORE.getImageView(getIconSizeToolBarInt()));

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                applyAction,
                restoreDefaultsAction
        ));

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        mToolBar.setStyle("-fx-spacing: 0px;");
        mToolBar.setPadding(Insets.EMPTY);

        Platform.runLater(() -> {
            FxHelper.undecorateButtons(mToolBar.getItems().stream());

        });

        setTop(mToolBar);
    }
}
