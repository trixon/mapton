/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.controlsfx.control.action.Action;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.core.api.MapEngine;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBar;
import se.trixon.mapton.core.api.MaptonTopComponent;
import se.trixon.mapton.core.api.ToolboxAction;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.ui//Toolbox//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ToolboxTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = true)
@ActionID(category = "Window", id = "se.trixon.mapton.core.ui.ToolboxTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ToolboxAction",
        preferredID = "ToolboxTopComponent"
)
@Messages({
    "CTL_ToolboxAction=Toolbox"
})
public final class ToolboxTopComponent extends MaptonTopComponent {

    private TreeView<Action> mRoot;

    public ToolboxTopComponent() {
        super();
        setName(Dict.TOOLBOX.toString());
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
        mRoot = new TreeView<>();
        mRoot.setShowRoot(false);
        mRoot.setCellFactory((TreeView<Action> param) -> new ActionTreeCell());

        mRoot.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                mRoot.getSelectionModel().getSelectedItem().getValue().handle(null);
            }
        });

        mRoot.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                mRoot.getSelectionModel().getSelectedItem().getValue().handle(null);
            }
        });

        Lookup.getDefault().lookupResult(MapEngine.class).addLookupListener((LookupEvent ev) -> {
            populateToolbox();
        });

        populateToolbox();

        return new Scene(mRoot);
    }

    private void populateToolbox() {
        TreeItem<Action> root = new TreeItem<>();
        root.setExpanded(true);
        ObservableList<TreeItem<Action>> treeRootChildrens = root.getChildren();
        TreeMap<String, TreeItem<Action>> actionParents = new TreeMap<>();
        ArrayList<TreeItem> tempRootItems = new ArrayList<>();

        Lookup.getDefault().lookupAll(ToolboxAction.class).forEach((toolboxAction) -> {
            TreeItem<Action> treeItem = new TreeItem(toolboxAction.getAction());
            treeItem.getValue().setGraphic(MaterialIcon._Action.BUILD.getImageView(getIconSizeToolBar() / 2));

            final String parentName = toolboxAction.getParent();
            if (parentName == null) {
                tempRootItems.add(treeItem);
            } else {
                actionParents.computeIfAbsent(parentName, k -> new TreeItem(parentName)).getChildren().add(treeItem);
            }
        });

        Comparator<TreeItem> treeItemComparator = (TreeItem o1, TreeItem o2) -> ((Action) o1.getValue()).getText().compareTo(((Action) o2.getValue()).getText());

        actionParents.keySet().stream().map((key) -> {
            TreeItem<Action> parentItem = new TreeItem<>(new Action(key));
            parentItem.getValue().setGraphic(MaterialIcon._Places.BUSINESS_CENTER.getImageView(getIconSizeToolBar() / 2));
            FXCollections.sort(actionParents.get(key).getChildren(), treeItemComparator);
            actionParents.get(key).getChildren().forEach((item) -> {
                parentItem.getChildren().add(item);
            });
            return parentItem;
        }).forEachOrdered((parentItem) -> {
            treeRootChildrens.add(parentItem);
        });

        Collections.sort(tempRootItems, treeItemComparator);
        tempRootItems.forEach((rootItem) -> {
            treeRootChildrens.add(rootItem);
        });

        mRoot.setRoot(root);
    }

    class ActionTreeCell extends TreeCell<Action> {

        public ActionTreeCell() {
            createUI();
        }

        @Override
        protected void updateItem(Action action, boolean empty) {
            super.updateItem(action, empty);

            if (action == null || empty) {
                clearContent();
            } else {
                addContent(action);
            }
        }

        private void addContent(Action action) {
            setText(action.getText());
            setGraphic(action.getGraphic());
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
        }

    }

}
