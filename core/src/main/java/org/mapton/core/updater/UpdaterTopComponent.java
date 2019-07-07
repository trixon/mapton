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
package org.mapton.core.updater;

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
import org.mapton.api.MUpdater;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.updater//Updater//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "UpdaterTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 99)
@ActionID(category = "Mapton", id = "org.mapton.updater.UpdaterTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "Updater",
        preferredID = "UpdaterTopComponent"
)
public final class UpdaterTopComponent extends MTopComponent {

    private Label mPlaceholderLabel;
    private final Preferences mPreferences = NbPreferences.forModule(UpdaterTopComponent.class).node("expanded_state");
    private BorderPane mRoot;
    private TreeView<MUpdater> mTreeView;

    public UpdaterTopComponent() {
        setName(NbBundle.getMessage(UpdaterTool.class, "updater_tool"));
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
        mTreeView.setPrefWidth(FxHelper.getUIScaled(250));
        mTreeView.setShowRoot(false);
        mTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends TreeItem<MUpdater>> c) -> {
            TreeItem<MUpdater> selectedItem = mTreeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                mRoot.setCenter(mPlaceholderLabel);
            } else {
                MUpdater selectedUpdater = selectedItem.getValue();
                mRoot.setCenter(selectedUpdater.getNode());
                selectedUpdater.onSelect();
            }
        });

        Lookup.getDefault().lookupResult(MUpdater.class).addLookupListener((LookupEvent ev) -> {
            populate();
        });

        populate();
        mRoot = new BorderPane(mPlaceholderLabel);
        mRoot.setLeft(mTreeView);

        return new Scene(mRoot);
    }

    private void populate() {
        //TODO Refactor to the style of BookmarkView#populate()
        MUpdater rootUpdater = new MUpdater() {
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

        TreeItem<MUpdater> root = new TreeItem<>(rootUpdater);
        ObservableList<TreeItem<MUpdater>> treeRootChildrens = root.getChildren();
        TreeMap<String, TreeItem<MUpdater>> actionParents = new TreeMap<>();
        ArrayList<TreeItem<MUpdater>> tempRootItems = new ArrayList<>();

        new Thread(() -> {
            Lookup.getDefault().lookupAll(MUpdater.class).forEach((updaterAction) -> {
                TreeItem<MUpdater> treeItem = new TreeItem<>(updaterAction);
                treeItem.setGraphic(MaterialIcon._Action.UPDATE.getImageView((int) (getIconSizeToolBar() / 1.5)));

                final String parentName = updaterAction.getParent();
                if (parentName == null) {
                    tempRootItems.add(treeItem);
                } else {
                    MUpdater updater = new MUpdater() {
                        @Override
                        public String getName() {
                            return parentName;
                        }

                        @Override
                        public Node getNode() {
                            return null;
                        }

                        @Override
                        public String getParent() {
                            return null;
                        }
                    };
                    actionParents.computeIfAbsent(parentName, k -> new TreeItem<>(updater)).getChildren().add(treeItem);
                }
            });

            Comparator<TreeItem> treeItemComparator = (TreeItem o1, TreeItem o2) -> ((MUpdater) o1.getValue()).getName().compareTo(((MUpdater) o2.getValue()).getName());

            actionParents.keySet().stream().map((key) -> {
                TreeItem<MUpdater> parentItem = new TreeItem<>(new ParentUpdater(key));
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

    private void postPopulate(TreeItem<MUpdater> treeItem, String level) {
        final MUpdater value = treeItem.getValue();
        final String path = String.format("%s/%s", value.getParent(), value.getName());
        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            BooleanProperty booleanProperty = (BooleanProperty) observable;
            TreeItem ti = (TreeItem) booleanProperty.getBean();
            MUpdater updater = (MUpdater) ti.getValue();
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<MUpdater>> c1 = (TreeItem<MUpdater> o1, TreeItem<MUpdater> o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<MUpdater>> c2 = (TreeItem<MUpdater> o1, TreeItem<MUpdater> o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (TreeItem<MUpdater> childTreeItem : treeItem.getChildren()) {
            postPopulate(childTreeItem, level + "-");
        }
    }

    class ParentUpdater extends MUpdater {

        private final String mName;

        public ParentUpdater(String name) {
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
