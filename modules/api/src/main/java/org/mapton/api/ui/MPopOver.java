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

import java.awt.MouseInfo;
import java.awt.Toolkit;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.ToolBar;
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
    public static final int WIDTH = FxHelper.getUIScaled(128);
    private Action mAction;
    private boolean mAutoArrowLocation;

    public static void autoSize(VBox vBox) {
        for (var node : vBox.getChildren()) {
            VBox.setVgrow(node, Priority.ALWAYS);

            if (node instanceof Control c) {
                c.setPrefWidth(2.0 * WIDTH + GAP);
            }
        }
        vBox.setPadding(FxHelper.getUIScaledInsets(GAP));
    }

    public MPopOver() {
        this("", true);
    }

    public MPopOver(String title, boolean autoArrowLocation) {
        mAutoArrowLocation = autoArrowLocation;
        setHeaderAlwaysVisible(true);
        setCloseButtonEnabled(false);
        setDetachable(true);
        setAnimated(false);
        setHideOnEscape(true);
        setArrowSize(0);

        mAction = new Action(title, actionEvent -> {
            if (isShowing()) {
                hide();
            } else {
                if (mAutoArrowLocation && false) {//disable for now
                    var mousePoint = MouseInfo.getPointerInfo().getLocation();
                    var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    var x = mousePoint.getX();
                    var quota = x / screenSize.getWidth();
                    if (quota < 0.2) {
                        setArrowLocation(ArrowLocation.TOP_LEFT);
                    } else if (quota > 0.8) {
                        setArrowLocation(ArrowLocation.TOP_RIGHT);
                    } else {
                        setArrowLocation(ArrowLocation.TOP_CENTER);
                    }
                }

                var node = (ButtonBase) actionEvent.getSource();
                var offset = FxHelper.getUIScaled(-10.0);

                if (getContentNode() instanceof ToolBar) {
                    offset = FxHelper.getUIScaled(-1.0);
                }
                show(node, offset);

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

    public void setAutoArrowLocation(boolean autoArrowLocation) {
        mAutoArrowLocation = autoArrowLocation;
    }
}
