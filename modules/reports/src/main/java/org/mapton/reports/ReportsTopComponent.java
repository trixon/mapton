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
package org.mapton.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MTopComponent;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.reports.api.MReport;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.reports//Reports//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ReportsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 99)
@ActionID(category = "Mapton", id = "org.mapton.reports.ReportsTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "Reports",
        preferredID = "ReportsTopComponent"
)
public final class ReportsTopComponent extends MTopComponent {

    private Label mPlaceholderLabel;
    private final Preferences mPreferences = NbPreferences.forModule(ReportsTopComponent.class).node("expanded_state");
    private BorderPane mRoot;
    private TreeView<MReport> mTreeView;

    public ReportsTopComponent() {
        setName(Dict.REPORTS.toString());
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
        mPlaceholderLabel = new Label();

        mTreeView = new TreeView<>();
        mTreeView.setShowRoot(false);
        mTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends TreeItem<MReport>> c) -> {
            TreeItem<MReport> selectedItem = mTreeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                mRoot.setCenter(mPlaceholderLabel);
            } else {
                MReport selectedReport = selectedItem.getValue();
                mRoot.setCenter(selectedReport.getNode());
                selectedReport.onSelect();
            }
        });

        Lookup.getDefault().lookupResult(MReport.class).addLookupListener((LookupEvent ev) -> {
            populate();
        });

        populate();
        mRoot = new BorderPane(mPlaceholderLabel);
        mRoot.setLeft(mTreeView);

        return new Scene(mRoot);
    }

    private void populate() {
        //TODO Refactor to the style of BookmarkView#populate()
        MReport rootReport = new MReport() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public Node getNode() {
                return null;
            }

            @Override
            public String getParent() {
                return "";
            }
        };

        TreeItem<MReport> root = new TreeItem<>(rootReport);
        ObservableList<TreeItem<MReport>> treeRootChildrens = root.getChildren();
        TreeMap<String, TreeItem<MReport>> actionParents = new TreeMap<>();
        ArrayList<TreeItem> tempRootItems = new ArrayList<>();

        new Thread(() -> {
            Lookup.getDefault().lookupAll(MReport.class).forEach((reportAction) -> {
                TreeItem<MReport> treeItem = new TreeItem(reportAction);
                treeItem.setGraphic(MaterialIcon._Action.ASSESSMENT.getImageView((int) (getIconSizeToolBar() / 1.5)));

                final String parentName = reportAction.getParent();
                if (parentName == null) {
                    tempRootItems.add(treeItem);
                } else {
                    actionParents.computeIfAbsent(parentName, k -> new TreeItem(parentName)).getChildren().add(treeItem);
                }
            });

            Comparator<TreeItem> treeItemComparator = (TreeItem o1, TreeItem o2) -> ((MReport) o1.getValue()).getName().compareTo(((MReport) o2.getValue()).getName());

            actionParents.keySet().stream().map((key) -> {
                TreeItem<MReport> parentItem = new TreeItem<>(new ParentReport(key));
                parentItem.setGraphic(MaterialIcon._File.FOLDER_OPEN.getImageView((int) (getIconSizeToolBar() / 1.5)));
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

            Platform.runLater(() -> {
                postPopulate(root, "");
                mTreeView.setRoot(root);
            });
        }).start();
    }

    private void postPopulate(TreeItem<MReport> treeItem, String level) {
        final MReport value = treeItem.getValue();
        final String path = String.format("%s/%s", value.getParent(), value.getName());
        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            BooleanProperty booleanProperty = (BooleanProperty) observable;
            TreeItem ti = (TreeItem) booleanProperty.getBean();
            MReport report = (MReport) ti.getValue();
            mPreferences.putBoolean(path, newValue);
        });

        Comparator c1 = new Comparator<TreeItem<MReport>>() {
            @Override
            public int compare(TreeItem<MReport> o1, TreeItem<MReport> o2) {
                return Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
            }
        };

        Comparator c2 = new Comparator<TreeItem<MReport>>() {
            @Override
            public int compare(TreeItem<MReport> o1, TreeItem<MReport> o2) {
                return o1.getValue().getName().compareTo(o2.getValue().getName());
            }
        };

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (TreeItem<MReport> childTreeItem : treeItem.getChildren()) {
            postPopulate(childTreeItem, level + "-");
        }
    }

    class ParentReport extends MReport {

        private final String mName;

        public ParentReport(String name) {
            mName = name;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public Node getNode() {
            return null;
        }

        @Override
        public String getParent() {
            return null;
        }
    }
}
