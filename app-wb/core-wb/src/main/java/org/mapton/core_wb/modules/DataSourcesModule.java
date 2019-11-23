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
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mapton.api.MDataSourceInitializer;
import org.mapton.api.MKey;
import org.mapton.api.ui.DataSourceTab;
import org.mapton.core_wb.api.MWorkbenchModule;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MWorkbenchModule.class)
public class DataSourcesModule extends MWorkbenchModule {

    private DataSourceTab mFileTab;
    private TabPane mTabPane;
    private DataSourceTab mWmsSourceTab;
    private DataSourceTab mWmsStyleTab;

    public DataSourcesModule() {
        super(Dict.DATA_SOURCES.toString(), MaterialDesignIcon.CLOUD);

        createUI();
        init();
    }

    @Override
    public Node activate() {
        return mTabPane;
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
        var applyToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.CHECK_CIRCLE_OUTLINE), event -> {
            apply();
        });
        setTooltip(applyToolbarItem, Dict.APPLY.toString());

        var discardToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.UNDO), event -> {
            DataSourceTab t = (DataSourceTab) mTabPane.getSelectionModel().getSelectedItem();
            t.restoreDefaults();
        });
        setTooltip(discardToolbarItem, Dict.RESTORE_DEFAULTS.toString());

        getToolbarControlsLeft().addAll(applyToolbarItem, discardToolbarItem);

        var wmsExts = new String[]{"json"};

        mFileTab = new DataSourceTab(Dict.FILE.toString(), MKey.DATA_SOURCES_FILES, null);
        mWmsSourceTab = new DataSourceTab("WMS " + Dict.SOURCE.toString(), MKey.DATA_SOURCES_WMS_SOURCES, wmsExts);
        mWmsStyleTab = new DataSourceTab("WMS " + Dict.STYLE.toString(), MKey.DATA_SOURCES_WMS_STYLES, wmsExts);
        mTabPane = new TabPane(mWmsSourceTab, mWmsStyleTab, mFileTab);

        for (Tab tab : mTabPane.getTabs()) {
            tab.setClosable(false);
        }
    }

    private void init() {
        mFileTab.load("");
        mWmsSourceTab.load(MDataSourceInitializer.getDefaultSources());
        mWmsStyleTab.load(MDataSourceInitializer.getDefaultStyles());

        apply();
    }
}
