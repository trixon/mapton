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
package org.mapton.api.ui;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.PopOverWatcher;

/**
 *
 * @author Patrik Karlström
 */
public class MPopOver extends PopOver {

    public static final int GAP = FxHelper.getUIScaled(8);
    public static final int WIDTH = FxHelper.getUIScaled(96);
    private Action mAction;

    public static void autoSize(VBox vBox) {
        for (var node : vBox.getChildren()) {
            VBox.setVgrow(node, Priority.ALWAYS);

            if (node instanceof Control) {
                Control c = (Control) node;
                c.setPrefWidth(2 * WIDTH + GAP);
            }
        }
        vBox.setPadding(FxHelper.getUIScaledInsets(GAP));
    }

    public MPopOver() {
        setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        setHeaderAlwaysVisible(true);
        setCloseButtonEnabled(false);
        setDetachable(true);
        setAnimated(true);

        mAction = new Action(getTitle(), actionEvent -> {
            if (isShowing()) {
                hide();
            } else {
                var node = (ButtonBase) actionEvent.getSource();
                show(node);
                PopOverWatcher.getInstance().registerPopOver(this, node);
            }
        });
    }

    public Action getAction() {
        return mAction;
    }

    public void setAction(Action action) {
        mAction = action;
    }
}
