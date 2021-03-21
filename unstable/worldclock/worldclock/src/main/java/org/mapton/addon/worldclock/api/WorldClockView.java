/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.addon.worldclock.api;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

public final class WorldClockView extends BorderPane {

    private static final double TILE_SIZE = 150;

    public WorldClockView() {
        createUI();
        initListeners();
    }

    private void createUI() {
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
        setTop(toolBar);
        var timeTile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE)
                .skinType(SkinType.TIME)
                .title("Time Tile")
                .text("Whatever text")
                .duration(LocalTime.of(1, 22))
                .description("Average reply time")
                .textVisible(true)
                .build();

        var clockTile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE * 2)
                .skinType(SkinType.CLOCK)
                .title("Clock Tile")
                .text("Whatever text")
                .dateVisible(true)
                .locale(Locale.US)
                .running(true)
                .build();

        ListView<Tile> listview = new ListView<>();
        listview.getItems().setAll(timeTile, clockTile);
        setCenter(listview);
    }

    private void initListeners() {
    }

}
