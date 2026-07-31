/*
 * Copyright 2026 Patrik Karlström.
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

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ToolBar;
import org.mapton.api.Mapton;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MToolBarPopOver extends MPopOver {

    public static final String TITLE = Dict.COMMANDS.toString();

    public MToolBarPopOver() {
        super(TITLE, false);
        setArrowLocation(ArrowLocation.TOP_RIGHT);
        setHeaderAlwaysVisible(false);
        setDetachable(false);
        getAction().setGraphic(MaterialIcon._Navigation.MORE_HORIZ.getImageView(Mapton.getIconSizeToolBarInt()));
    }

    public void setToolBar(ToolBar toolBar) {
        setContentNode(toolBar);
        toolBar.setOrientation(Orientation.VERTICAL);
        FxHelper.setAlignment(toolBar.getItems().stream(), Pos.CENTER_LEFT);
        setOnShown(event -> {
            Platform.runLater(() -> {
                FxHelper.adjustButtonWidthMax(toolBar.getItems().stream(), 50);
            });
        });

    }
}
