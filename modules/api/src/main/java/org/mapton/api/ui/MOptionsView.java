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
package org.mapton.api.ui;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MOptionsView {

    private final BorderPane mBorderPane = new BorderPane();
    private Action mRestoreDefaultsAction;
    private Runnable mRestoreDefaultsRunnable;
    private final ScrollPane mScrollPane = new ScrollPane();
    private SessionManager mSessionManager;
    private ToolBar mToolBar;

    public MOptionsView() {
        createUI();
    }

    public GridPane createGridPane() {
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
        gp.setPadding(FxHelper.getUIScaledInsets(8));
        FxHelper.autoSizeColumn(gp, 2);

        return gp;
    }

    public void createToolbar(List<Action> actions) {
        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);

        setTop(mToolBar);
    }

    public BorderPane getBorderPane() {
        return mBorderPane;
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    /**
     *
     * <p>
     * @param key
     * @return a key with simple class name as prefix
     */
    public String getKey(String key) {
        return "%s.%s".formatted(getClass().getSimpleName(), key);
    }

    /**
     *
     * <p>
     * @param key
     * @return a key with simple class name and options as prefix
     */
    public String getKeyOptions(String key) {
        return "options.%s.%s".formatted(getClass().getSimpleName(), key);
    }

    public Action getRestoreDefaultsAction(Runnable restoreDefaultsRunnable) {
        mRestoreDefaultsRunnable = restoreDefaultsRunnable;

        return mRestoreDefaultsAction;
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(NbPreferences.forModule(getClass()));
        }

        return mSessionManager;
    }

    public Node getUI() {
        return mBorderPane;
    }

    public void setBottom(Node node) {
        mBorderPane.setBottom(node);
    }

    public void setCenter(Node node) {
        mScrollPane.setContent(node);
        mBorderPane.setCenter(mScrollPane);
    }

    public void setLabelPadding(Region... regions) {
        FxHelper.setPadding(FxHelper.getUIScaledInsets(6, 0, 0, 0),
                regions
        );
    }

    public void setTop(Node node) {
        mBorderPane.setTop(node);
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return mBorderPane.widthProperty();
    }

    private void createUI() {
        mScrollPane.setFitToWidth(true);
        mScrollPane.setFitToHeight(true);
        mScrollPane.setBackground(FxHelper.createBackground(Color.TRANSPARENT));
        mRestoreDefaultsAction = new Action(Dict.RESTORE_DEFAULTS.toString(), actionEvent -> {
            mRestoreDefaultsRunnable.run();
        });
        mRestoreDefaultsAction.setGraphic(MaterialIcon._Action.RESTORE_PAGE.getImageView(Mapton.getIconSizeToolBarInt()));
    }

}
