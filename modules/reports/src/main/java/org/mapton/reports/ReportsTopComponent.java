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

import java.util.Comparator;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MWorkbenchModule;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE;
import org.mapton.reports.api.MReport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

@ServiceProvider(service = MWorkbenchModule.class)
public final class ReportsTopComponent extends MWorkbenchModule {

    private Label mPlaceholderLabel;
    private final Preferences mPreferences = NbPreferences.forModule(ReportsTopComponent.class).node("expanded_state");
    private TreeMap<String, TreeItem<MReport>> mReportParents = new TreeMap<>();
    private BorderPane mRoot;
    private TreeView<MReport> mTreeView;

    public ReportsTopComponent() {
        super(Dict.REPORTS.toString(), MaterialIcon._Action.BUILD.getImageView(ICON_SIZE_MODULE).getImage());

        createUI();
    }

    @Override
    public Node activate() {
        return mRoot;
    }

    private void createUI() {
        mPlaceholderLabel = new Label();

        mTreeView = new TreeView<>();
        mTreeView.setPrefWidth(FxHelper.getUIScaled(250));
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
    }

    private TreeItem<MReport> getParent(TreeItem<MReport> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mReportParents.containsKey(path)) {
                parent = mReportParents.get(path);
            } else {
                MReport report = new MReport() {
                    @Override
                    public String getName() {
                        return segment;
                    }

                    @Override
                    public Node getNode() {
                        return null;
                    }

                    @Override
                    public String getParent() {
                        return path;
                    }
                };

                parent.getChildren().add(parent = mReportParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(report)));
            }

            sb.append("/");
        }

        return parent;
    }

    private void populate() {
        mReportParents.clear();

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
        new Thread(() -> {
            Lookup.getDefault().lookupAll(MReport.class).forEach((report) -> {
                TreeItem<MReport> reportTreeItem = new TreeItem<>(report);
                String category = report.getParent();
                TreeItem<MReport> parent = mReportParents.computeIfAbsent(category, k -> getParent(root, category));
                parent.getChildren().add(reportTreeItem);
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
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<MReport>> c1 = (TreeItem<MReport> o1, TreeItem<MReport> o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<MReport>> c2 = (TreeItem<MReport> o1, TreeItem<MReport> o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

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
