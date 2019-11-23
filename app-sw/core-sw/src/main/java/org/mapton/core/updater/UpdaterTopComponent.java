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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MMaskerPaneBase;
import org.mapton.api.MUpdater;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
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
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@ActionID(category = "Mapton", id = "org.mapton.updater.UpdaterTopComponent")
@TopComponent.OpenActionRegistration(
        displayName = "Updater",
        preferredID = "UpdaterTopComponent"
)
public final class UpdaterTopComponent extends MTopComponent {

    private BorderPane mInnerBorderPane;
    private ListView<MUpdater> mListView;
    private BorderPane mRoot;
    private UpdaterMaskerPane mUpdaterMaskerPane;

    public UpdaterTopComponent() {
        setName(getBundleString("updater_tool"));
    }

    @Override
    protected void initFX() {
        setScene(createScene());

        Lookup.getDefault().lookupResult(MUpdater.class).addLookupListener((LookupEvent ev) -> {
            refresh();
        });

        refresh();
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
        Label titleLabel = Mapton.createTitle(getBundleString("updater_tool"));
        mUpdaterMaskerPane = new UpdaterMaskerPane();

        Action refreshAction = new Action(Dict.REFRESH.toString(), (ActionEvent event) -> {
            refresh();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        Action updateAction = new Action(Dict.UPDATE.toString(), (ActionEvent event) -> {
            for (MUpdater updater : mListView.getItems()) {
                if (updater.isMarkedForUpdate()) {
                    mUpdaterMaskerPane.update();
                    break;
                }
            }
        });
        updateAction.setGraphic(MaterialIcon._Action.SYSTEM_UPDATE_ALT.getImageView(getIconSizeToolBarInt()));

        List<Action> actions = Arrays.asList(
                refreshAction,
                ActionUtils.ACTION_SPAN,
                updateAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        toolBar.setStyle("-fx-spacing: 0px;");
        toolBar.setPadding(Insets.EMPTY);

        mListView = new ListView<>();
        mListView.setCellFactory((ListView<MUpdater> param) -> new UpdaterListCell());
        mInnerBorderPane = new BorderPane(mListView);
        mInnerBorderPane.setTop(toolBar);
        mUpdaterMaskerPane.setContent(mInnerBorderPane);
        mRoot = new BorderPane(mUpdaterMaskerPane.getNode());
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }

    private void refresh() {
        new Thread(() -> {
            ArrayList<MUpdater> updaters = new ArrayList<>(Lookup.getDefault().lookupAll(MUpdater.class));
            for (MUpdater updater : updaters) {
                updater.setMarkedForUpdate(updater.isOutOfDate());
            }

            Comparator<MUpdater> c1 = (MUpdater o1, MUpdater o2) -> Boolean.compare(o2.isOutOfDate(), o1.isMarkedForUpdate());
            Comparator<MUpdater> c2 = (MUpdater o1, MUpdater o2) -> o1.getCategory().compareTo(o2.getCategory());
            Comparator<MUpdater> c3 = (MUpdater o1, MUpdater o2) -> o1.getName().compareTo(o2.getName());

            updaters.sort(c1.thenComparing(c2).thenComparing(c3));

            Platform.runLater(() -> {
                mListView.getItems().setAll(updaters);
            });
        }).start();
    }

    class UpdaterListCell extends ListCell<MUpdater> {

        private final Label mCategoryLabel = new Label();
        private final Label mCommentLabel = new Label();
        private final Font mHeaderFont;
        private final Label mLastUpdatedLabel = new Label();
        private final CheckBox mNameCheckBox = new CheckBox();
        private VBox mVBox;

        public UpdaterListCell() {
            mHeaderFont = Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, FxHelper.getScaledFontSize() * 1.1);
            createUI();
        }

        @Override
        protected void updateItem(MUpdater updater, boolean empty) {
            super.updateItem(updater, empty);
            if (updater == null || empty) {
                clearContent();
            } else {
                addContent(updater);
            }
        }

        private void addContent(MUpdater updater) {
            setText(null);

            mNameCheckBox.setText(updater.getName());
            mNameCheckBox.setSelected(updater.isOutOfDate());
            mNameCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                updater.setMarkedForUpdate(t1);
            });

            mCategoryLabel.setText(updater.getCategory());
            mCommentLabel.setText(updater.getComment());
            mLastUpdatedLabel.setText(updater.getLastUpdated());

            setGraphic(mVBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            mNameCheckBox.setFont(mHeaderFont);
            Font font = Font.font(FxHelper.getScaledFontSize() * 0.9);
            Font italicFont = Font.font(font.getFamily(), FontPosture.ITALIC, font.getSize());
            mCategoryLabel.setFont(font);
            mCommentLabel.setFont(italicFont);
            mLastUpdatedLabel.setFont(font);

            mVBox = new VBox(
                    FxHelper.getUIScaled(2),
                    mCategoryLabel,
                    mNameCheckBox,
                    mCommentLabel,
                    mLastUpdatedLabel
            );

            mVBox.setPadding(FxHelper.getUIScaledInsets(4));
        }
    }

    class UpdaterMaskerPane extends MMaskerPaneBase {

        void update() {
            mMaskerPane.setVisible(true);

            new Thread(() -> {
                for (MUpdater updater : mListView.getItems()) {
                    if (updater.isMarkedForUpdate()) {
                        updater.run();
                    }
                }

                Platform.runLater(() -> {
                    refresh();
                    mMaskerPane.setVisible(false);
                    notify(Dict.OPERATION_COMPLETED.toString());
                });
            }).start();
        }
    }
}
