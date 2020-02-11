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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MKey;
import org.mapton.api.MToolApp;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolboxView extends BorderPane {

    private static final int CELL_WIDTH = FxHelper.getUIScaled(192);
    private static final int CELL_HEIGHT = FxHelper.getUIScaled(118);

    private final ArrayList<MToolApp> mTools = new ArrayList<>();
    private final GridView<MToolApp> mGridView = new GridView<>();

    public AppToolboxView() {
        createUI();
        addListeners();

        initTools();
    }

    private void addListeners() {
        Lookup.getDefault().lookupResult(MToolApp.class).addLookupListener((LookupEvent ev) -> {
            initTools();
        });
    }

    private void createUI() {
        setCenter(mGridView);
        mGridView.setCellFactory((GridView<MToolApp> gridView) -> new ActionGridCell());
        mGridView.setPrefWidth(CELL_WIDTH * 4.6);
        mGridView.setCellHeight(CELL_HEIGHT);
        mGridView.setCellWidth(CELL_WIDTH);
        setMaxHeight(CELL_HEIGHT * 3.65);
    }

    private void initTools() {
        mTools.clear();
        Lookup.getDefault().lookupAll(MToolApp.class).forEach((tool) -> {
            mTools.add(tool);
        });

        Comparator<MToolApp> c1 = (MToolApp o1, MToolApp o2) -> StringUtils.defaultString(o1.getParent()).compareToIgnoreCase(StringUtils.defaultString(o2.getParent()));
        Comparator<MToolApp> c2 = (MToolApp o1, MToolApp o2) -> o1.getAction().getText().compareToIgnoreCase(o2.getAction().getText());

        mTools.sort(c1.thenComparing(c2));

        populate();
    }

    private void populate() {
        mGridView.getItems().clear();

        TreeSet<String> categories = new TreeSet<>();
        for (MToolApp tool : mTools) {
            mGridView.getItems().add(tool);
            final String parent = StringUtils.defaultString(tool.getParent());
            categories.add(parent);
        }

        for (String category : categories) {
            MToolApp tool = new MToolApp() {
                @Override
                public Action getAction() {
                    return null;
                }

                @Override
                public String getParent() {
                    return category;
                }

            };

            mGridView.getItems().add(getFirstIndex(category), tool);
        }

    }

    private int getFirstIndex(String s) {
        for (int i = 0; i < mGridView.getItems().size(); i++) {
            final String parent = StringUtils.defaultString(mGridView.getItems().get(i).getParent(), "?");

            if (parent.equalsIgnoreCase(s)) {
                return i;
            }
        }

        return 0;
    }

    public class ActionGridCell extends GridCell<MToolApp> {

        private Button mButton;

        public ActionGridCell() {
            mButton = new Button();
            setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                    Mapton.getGlobalState().send(MKey.APP_TOOL_STARTED, event);
                    mButton.fire();
                }
            });
        }

        @Override
        protected void updateItem(MToolApp item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                if (item.getAction() == null) {
                    Label label = new Label(item.getParent().toUpperCase(Locale.getDefault()));
                    label.setAlignment(Pos.CENTER);
                    label.setBackground(Mapton.getThemeBackground());
                    label.setFont(Font.font(FxHelper.getUIScaled(Font.getDefault().getSize() * 1.6)));
                    label.setMinSize(CELL_WIDTH, CELL_HEIGHT);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setTextFill(Mapton.options().getIconColorBright());

                    setGraphic(label);
                } else {
                    mButton = ActionUtils.createButton(item.getAction());
                    mButton.setContentDisplay(ContentDisplay.TOP);
                    mButton.setMinSize(CELL_WIDTH, CELL_HEIGHT);
                    setGraphic(mButton);
                }
            }
        }
    }
}
