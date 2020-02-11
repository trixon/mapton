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
package org.mapton.core_nb.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MOptions;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseToolBar extends ToolBar {

    public static final int DEFAULT_POP_OVER_WIDTH = 350;
    public static final boolean IS_MAC = SystemUtils.IS_OS_MAC;

    protected final HashSet<PopOver> mAlwaysShowPopOvers = new HashSet<>();
    protected final MOptions mOptions = MOptions.getInstance();
    protected final HashSet<PopOver> mPopOvers = new HashSet<>();
    private final HashMap<Action, Double> mButtonWidths = new HashMap<>();
    private final HashMap<Object, Long> mObjectClosingTimes = new HashMap<>();

    public BaseToolBar() {
    }

    public void onObjectHiding(Object object) {
        mObjectClosingTimes.put(object, System.currentTimeMillis());

    }

    protected ButtonBase getButtonForAction(Action action) {
        for (Node item : getItems()) {
            if (item instanceof ButtonBase) {
                ButtonBase buttonBase = (ButtonBase) item;
                if (buttonBase.getOnAction().equals(action)) {
                    return buttonBase;
                }
            }
        }

        return null;
    }

    protected void initPopOver(PopOver popOver, String title, Node content, boolean alwaysUsePopOver) {
        popOver.setTitle(title);
        popOver.setContentNode(content);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setCloseButtonEnabled(false);
        popOver.setDetachable(false);
        popOver.setAnimated(false);
        popOver.setOnHiding(windowEvent -> {
            onObjectHiding(popOver);
        });
        mPopOvers.add(popOver);
        if (alwaysUsePopOver) {
            mAlwaysShowPopOvers.add(popOver);
        }
    }

    protected void setPopOverWidths(double width, PopOver... popOvers) {
        for (PopOver popOver : popOvers) {
            ((Region) popOver.getContentNode()).setPrefWidth(width);
        }
    }

    protected void setTextFromActions() {
        for (Map.Entry<Action, Double> entry : mButtonWidths.entrySet()) {
            ButtonBase b = getButtonForAction(entry.getKey());
            b.setPrefWidth(entry.getValue());
            b.textProperty().bind(entry.getKey().textProperty());
        }
    }

    protected void setTooltip(Action action, KeyCodeCombination keyCodeCombination) {
        action.setLongText(String.format("%s (%s)", action.getText(), keyCodeCombination.getDisplayText()));
    }

    protected boolean shouldOpen(Object object) {
        return System.currentTimeMillis() - mObjectClosingTimes.getOrDefault(object, 0L) > 200;
    }

    protected void show(PopOver popOver, Object owner) {
        popOver.show((Node) owner, -6);
    }

    protected void storeButtonWidths(Action... actions) {
        for (Action action : actions) {
            mButtonWidths.put(action, getButtonForAction(action).prefWidthProperty().getValue());
        }
    }

    protected void tooglePopOver(PopOver popOver, Action action) {
        Platform.runLater(() -> {
            if (popOver.isAutoHide()) {
                if (popOver.isShowing()) {
                    popOver.hide();
                } else {
                    mPopOvers.forEach((item) -> {
                        item.hide();
                    });

                    getItems().stream()
                            .filter((item) -> (item instanceof ButtonBase))
                            .map((item) -> (ButtonBase) item)
                            .filter((buttonBase) -> (buttonBase.getOnAction() == action))
                            .forEachOrdered((buttonBase) -> {
                                buttonBase.fire();
                            });
                }
            } else {
                if (popOver.isShowing()) {
                    popOver.hide();
                } else {
                    show(popOver, getButtonForAction(action));
                }
            }
        });
    }

    protected boolean usePopOver(PopOver popOver) {
        return mOptions.isPreferPopover() || mOptions.isMapOnly() || mAlwaysShowPopOvers.contains(popOver);
    }

}
