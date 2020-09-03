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
package org.mapton.core_nb.updater;

import java.util.Arrays;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.base.ui.updater.UpdaterView;
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core_nb.updater//Updater//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "UpdaterTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 9)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_UpdaterAction",
        preferredID = "UpdaterTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.core_nb.updater.UpdaterTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "D-9"),
    @ActionReference(path = "Menu/Window", position = 9)
})
@NbBundle.Messages({
    "CTL_UpdaterAction=&Updater"
})
public final class UpdaterTopComponent extends MTopComponent {

    public UpdaterTopComponent() {
        setName(Dict.UPDATER.toString());
    }

    @Override
    protected void initFX() {
        setScene(createScene());

    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private Scene createScene() {
        UpdaterView updaterView = new UpdaterView();

        Action updateAction = new Action(Dict.UPDATE.toString(), event -> {
            updaterView.update();
        });
        updateAction.setGraphic(MaterialIcon._Action.SYSTEM_UPDATE_ALT.getImageView(getIconSizeToolBarInt()));

        Action refreshAction = new Action(Dict.REFRESH.toString(), event -> {
            updaterView.refreshUpdaters();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        Action clearAction = new Action(Dict.CLEAR.toString(), event -> {
            updaterView.clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        List<Action> actions = Arrays.asList(
                updateAction,
                refreshAction,
                clearAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        toolBar.setStyle("-fx-spacing: 0px;");
        toolBar.setPadding(Insets.EMPTY);

        updateAction.disabledProperty().bind(updaterView.runningProperty().or(updaterView.selectedProperty().not()));
        refreshAction.disabledProperty().bind(updaterView.runningProperty());

        BorderPane root = new BorderPane(updaterView.getLogPanel());
        root.setLeft(updaterView.getListNode());
        root.setTop(toolBar);

        return new Scene(root);
    }
}
