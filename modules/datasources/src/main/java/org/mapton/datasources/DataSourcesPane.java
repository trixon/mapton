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

    private static final int ICON_SIZE = (int) (getIconSizeToolBar() * 0.8);
    private static final String KEY_CONTENT = "content";
    private Action mApplyAction;
    private final Preferences mPreferences = NbPreferences.forModule(DataSourcesPane.class);
    private TextArea mTextArea;
    private ToolBar mToolBar;

    public DataSourcesPane() {
        createUI();
        init();
    }

    void save() {
        Platform.runLater(() -> {
            mPreferences.put(KEY_CONTENT, mTextArea.getText());
        });
    }

    private void apply() {
        ArrayList<File> files = new ArrayList<>();
        for (String line : mTextArea.getText().split("\n")) {
            File file = new File(line);
            if (file.exists()) {
                files.add(file);
            }
        }

        Mapton.getGlobalState().put("data_sources.files", files);
    }

    private void createUI() {
        mTextArea = new TextArea();
        initToolBar();

        setCenter(mTextArea);
    }

    private void init() {
        mTextArea.setText(mPreferences.get(KEY_CONTENT, ""));
    }

    private void initToolBar() {
        mApplyAction = new Action(Dict.APPLY.toString(), (event) -> {
            apply();
        });
        mApplyAction.setGraphic(MaterialIcon._Navigation.CHECK.getImageView(ICON_SIZE));

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
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
