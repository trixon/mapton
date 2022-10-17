/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.core.ui.area;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.locationtech.jts.geom.Coordinate;
import org.mapton.api.MArea;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AreaPane extends BorderPane {

    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();

    public AreaPane() {
        createUI();
    }

    private void createUI() {
        var browseAction = new Action(Dict.BROWSE.toString(), actionEvent -> {
        });
        browseAction.setGraphic(MaterialIcon._Social.PUBL.getImageView(getIconSizeToolBarInt()));
        browseAction.setDisabled(true);

        var actions = Arrays.asList(
                browseAction,
                ActionUtils.ACTION_SPAN
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        setTop(toolBar);
        var treeView = mAreaFilterManager.getTreeView();
        setCenter(treeView);
        treeView.setCellFactory(param -> new AreaTreeCell());
    }

    class AreaTreeCell extends CheckBoxTreeCell<MArea> {

        private final ArrayList<Coordinate> mTreeCoordinates = new ArrayList<>();

        public AreaTreeCell() {
            createUI();
        }

        @Override
        public void updateItem(MArea area, boolean empty) {
            super.updateItem(area, empty);

            if (area == null || empty) {
                clearContent();
            } else {
                addContent(area);
            }
        }

        private void addContent(MArea area) {
            setText(area.getName());
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    mTreeCoordinates.clear();
                    mergeChildren(getTreeItem());
                    Mapton.getEngine().fitToBounds(mTreeCoordinates);
                }
            });
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
            setOnMouseClicked(null);
        }

        private void createUI() {
        }

        private void mergeChildren(TreeItem<MArea> treeItem) {
            var geometry = treeItem.getValue().getGeometry();

            if (treeItem.isLeaf()) {
                if (geometry != null) {
                    mTreeCoordinates.addAll(Arrays.asList(geometry.getCoordinates()));
                }
            } else {
                for (var childTreeItem : treeItem.getChildren()) {
                    mergeChildren(childTreeItem);
                }
            }
        }
    }
}
