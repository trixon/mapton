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
package org.mapton.files;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MCoordinateFileManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MOptionsPopOver;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class FilesPane extends BorderPane {

    private List<Action> mActions;
    private final CheckListView<MCoordinateFile> mListView = new CheckListView<>();
    private final MCoordinateFileManager mManager = MCoordinateFileManager.getInstance();
    private Action mRefreshAction;

    public FilesPane() {
        createUI();
        refreshCheckedStates();
        initListeners();
    }

    private void createUI() {
        var closeAction = new Action(Dict.CLOSE.toString(), event -> {
            if (getSelected() != null) {
                remove();
            }
        });
        closeAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(getIconSizeToolBarInt()));

        var closeAllAction = new Action(Dict.CLOSE_ALL.toString(), event -> {
            if (!mListView.getItems().isEmpty()) {
                removeAll();
            }
        });
        closeAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        mRefreshAction = new Action(Dict.REFRESH.toString(), event -> {
            mManager.refresh();
        });
        mRefreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        var optionsPopOver = new MOptionsPopOver();
        optionsPopOver.getAction().setDisabled(true);

        mActions = Arrays.asList(
                closeAction,
                closeAllAction,
                ActionUtils.ACTION_SPAN,
                mRefreshAction,
                optionsPopOver.getAction()
        );

        var toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());

        FxHelper.slimToolBar(toolBar);
        setTop(toolBar);
        setCenter(mListView);

        mListView.itemsProperty().bind(mManager.itemsProperty());
    }

    private MCoordinateFile getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MCoordinateFile> c) -> {
            if (getSelected() != null) {
//                getSelected().fitToBounds();
            }
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends MCoordinateFile> c) -> {
            refreshCheckedStates();
            try {
                mManager.save();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        final IndexedCheckModel<MCoordinateFile> checkModel = mListView.getCheckModel();

        checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends MCoordinateFile> c) -> {
            Platform.runLater(() -> {
                mManager.getItems().forEach(document -> {
                    document.setVisible(checkModel.isChecked(document));
                });

                try {
                    mManager.save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });
    }

    private void refreshCheckedStates() {
        final IndexedCheckModel<MCoordinateFile> checkModel = mListView.getCheckModel();

        for (MCoordinateFile document : mManager.getItems()) {
            if (document.isVisible()) {
                checkModel.check(document);
            } else {
                checkModel.clearCheck(document);
            }
        }
    }

    private void remove() {
        final MCoordinateFile document = getSelected();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.CLOSE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_FILE_CLOSE.toString(), document.getFile().getName()),
                    String.format(Dict.Dialog.TITLE_CLOSE_S.toString(), Dict.FILE.toString().toLowerCase()) + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.CLOSE.toString());

            if (Dict.CLOSE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll(document);
                });
            }
        });
    }

    private void removeAll() {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.CLOSE_ALL.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_FILE_CLOSE_ALL.toString(),
                    Dict.Dialog.TITLE_FILE_CLOSE_ALL.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.CLOSE_ALL.toString());

            if (Dict.CLOSE_ALL.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll();
                });
            }
        });
    }
}
