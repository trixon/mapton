/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.worldclock_nb;

import java.util.Arrays;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core_nb.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.addon.worldclock_nb//WorldClock//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WorldClockTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
public final class WorldClockTopComponent extends MTopComponent {

    private BorderPane mRoot;

    public WorldClockTopComponent() {
        setName(WorldClockTool.NAME);
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
        Action firstAction = new Action(Dict.FIRST.toString(), event -> {
        });
        firstAction.setGraphic(MaterialIcon._Navigation.FIRST_PAGE.getImageView(getIconSizeToolBarInt()));

        Action previousAction = new Action(Dict.PREVIOUS.toString(), event -> {
        });
        previousAction.setGraphic(MaterialIcon._Navigation.CHEVRON_LEFT.getImageView(getIconSizeToolBarInt()));

        Action nextAction = new Action(Dict.NEXT.toString(), event -> {
        });
        nextAction.setGraphic(MaterialIcon._Navigation.CHEVRON_RIGHT.getImageView(getIconSizeToolBarInt()));

        Action lastAction = new Action(Dict.LAST.toString(), event -> {
        });
        lastAction.setGraphic(MaterialIcon._Navigation.LAST_PAGE.getImageView(getIconSizeToolBarInt()));

        Action randomAction = new Action(Dict.RANDOM.toString(), event -> {
        });
        randomAction.setGraphic(MaterialIcon._Places.CASINO.getImageView(getIconSizeToolBarInt()));

        Action clearAction = new Action(Dict.CLEAR.toString(), event -> {
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        List<Action> actions = Arrays.asList(
                firstAction,
                previousAction,
                randomAction,
                nextAction,
                lastAction,
                ActionUtils.ACTION_SPAN,
                clearAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        FxHelper.slimToolBar(toolBar);
        Label titleLabel = Mapton.createTitle(WorldClockTool.NAME);

        BorderPane innerPane = new BorderPane(toolBar);
        mRoot = new BorderPane();
        innerPane.setTop(titleLabel);
        mRoot.setTop(innerPane);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }
}
