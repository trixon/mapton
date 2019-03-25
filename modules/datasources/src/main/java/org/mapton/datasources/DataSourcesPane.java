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
package org.mapton.datasources;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class DataSourcesPane extends BorderPane {

    static final String KEY_FILES = "files";
    static final String KEY_WMS_SOURCE = "wms.source";
    static final String KEY_WMS_STYLE = "wms.style";
    private static final int ICON_SIZE = (int) (getIconSizeToolBar() * 0.8);
    private Action mApplyAction;
    private Tab mFileTab;
    private TextArea mFileTextArea;
    private final Preferences mPreferences = NbPreferences.forModule(DataSourcesPane.class);
    private TabPane mTabPane;
    private ToolBar mToolBar;
    private Tab mWmsSourceTab;
    private TextArea mWmsSourceTextArea;
    private Tab mWmsStyleTab;
    private TextArea mWmsStyleTextArea;

    public DataSourcesPane() {
        createUI();
        init();
    }

    void save() {
        Platform.runLater(() -> {
            mPreferences.put(KEY_WMS_SOURCE, mWmsSourceTextArea.getText());
            mPreferences.put(KEY_WMS_STYLE, mWmsStyleTextArea.getText());
            mPreferences.put(KEY_FILES, mFileTextArea.getText());
        });
    }

    private void apply() {
        save();
    }

    private void applyFile() {
        //TODO Read files and create point, lines and polygons for publishing
        //TODO Support simple attributes such as colors, line width and markers.
        //TODO Don't put any burden on map engine renderers, unless needed
        //TODO Get importers from lookup
        ArrayList<File> files = new ArrayList<>();
        for (String line : mFileTextArea.getText().split("\n")) {
            File file = new File(line);
            if (file.exists()) {
                files.add(file);
            }
        }

        Mapton.getGlobalState().put("data_sources.files", files);
    }

    private void createUI() {
        initToolBar();

        mFileTextArea = new TextArea();
        mWmsSourceTextArea = new TextArea();
        mWmsStyleTextArea = new TextArea();

        mFileTab = new Tab(Dict.FILE.toString(), mFileTextArea);
        mWmsSourceTab = new Tab("WMS " + Dict.SOURCE.toString(), mWmsSourceTextArea);
        mWmsStyleTab = new Tab("WMS " + Dict.STYLE.toString(), mWmsStyleTextArea);
        mTabPane = new TabPane(mWmsSourceTab, mWmsStyleTab, mFileTab);

        for (Tab tab : mTabPane.getTabs()) {
            tab.setClosable(false);
        }

        setCenter(mTabPane);
    }

    private void init() {
        mFileTextArea.setText(mPreferences.get(KEY_FILES, ""));
        mWmsSourceTextArea.setText(mPreferences.get(KEY_WMS_SOURCE, ""));
        mWmsStyleTextArea.setText(mPreferences.get(KEY_WMS_STYLE, ""));

        apply();
    }

    private void initToolBar() {
        mApplyAction = new Action(Dict.APPLY.toString(), (event) -> {
            apply();
        });
        mApplyAction.setGraphic(MaterialIcon._Navigation.CHECK.getImageView(ICON_SIZE));

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                ActionUtils.ACTION_SPAN,
                mApplyAction
        ));

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        mToolBar.setStyle("-fx-spacing: 0px;");
        mToolBar.setPadding(Insets.EMPTY);

        Platform.runLater(() -> {
            FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), ICON_SIZE * 1.5);
            mToolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });
        });

        setTop(mToolBar);
    }
}
