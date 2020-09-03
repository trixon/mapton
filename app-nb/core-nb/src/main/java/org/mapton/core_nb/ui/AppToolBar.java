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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.PreferenceChangeEvent;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.util.Duration;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.mapton.base.ui.SearchView;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBar extends BaseToolBar {

    private final String CSS_FILE = getClass().getResource("toolbar_app.css").toExternalForm();
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private final ResourceBundle mBundle;
    private File mFile;
    private FxActionSwingCheck mFullscreenAction;
    private FxActionSwingCheck mMapAction;
    private SearchView mSearchView;
    private Label mStatusLabel;
    private Action mToolboxAction;
    private PopOver mToolboxPopOver;

    public AppToolBar() {
        mBundle = NbBundle.getBundle(AppToolBar.class);
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();
    }

    public void activateSearch() {
        Platform.runLater(() -> {
            getScene().getWindow().requestFocus();
            mSearchView.requestFocus();
            mSearchView.clear();
        });
    }

    public void open() {
        TreeMap<String, ArrayList<MCoordinateFileOpener>> extToCoordinateFileOpeners = new TreeMap<>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).forEach(coordinateFileOpener -> {
            for (String extension : coordinateFileOpener.getExtensions()) {
                extToCoordinateFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(coordinateFileOpener);
            }
        });

        if (extToCoordinateFileOpeners.isEmpty()) {
            NbMessage.warning(Dict.WARNING.toString(), mBundle.getString("no_file_openers"));
        } else {
            ArrayList<FileNameExtensionFilter> fileNameExtensionFilters = new ArrayList<>();
            SimpleDialog.setTitle(Dict.OPEN.toString());
            SimpleDialog.clearFilters();
            extToCoordinateFileOpeners.entrySet().stream().map(entry -> {
                entry.getValue().sort((MCoordinateFileOpener o1, MCoordinateFileOpener o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                return entry;
            }).forEachOrdered(entry -> {
                entry.getValue().forEach(coordinateFileOpener -> {
                    SimpleDialog.addFilter(new FileNameExtensionFilter(String.format("%s (%s)", coordinateFileOpener.getName(), entry.getKey()), entry.getKey()));
                });
            });

            if (mFile == null) {
                SimpleDialog.setPath(FileUtils.getUserDirectory());
            } else {
                SimpleDialog.setPath(mFile.getParentFile());
                SimpleDialog.setSelectedFile(new File(""));
            }

            if (SimpleDialog.openFile(true)) {
                mFile = SimpleDialog.getPaths()[0];
                new FileDropSwitchboard(Arrays.asList(SimpleDialog.getPaths()));
            }
        }
    }

    public void toogleToolboxPopOver() {
        tooglePopOver(mToolboxPopOver, mToolboxAction);
    }

    private int getIconSizeToolBar() {
        return (int) (Mapton.getIconSizeToolBar() * .6);
    }

    private void init() {
        setPadding(Insets.EMPTY);

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                ActionUtils.ACTION_SPAN,
                ActionUtils.ACTION_SPAN,
                mMapAction,
                mToolboxAction
        ));

        if (!IS_MAC) {
            actions.add(actions.size() - 1, mFullscreenAction);
        }

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);
            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeToolBar() * 1.0);
            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
//                buttonBase.getStylesheets().add(CSS_FILE);
            });

            getStylesheets().add(CSS_FILE);
            mSearchView = new SearchView();
            getItems().add(getItems().size() - 3, mSearchView.getPresenter());
            updateBackgroundColor();

            getItems().add(2, mStatusLabel = new Label());
            mStatusLabel.setTextFill(mOptions.getIconColorBright());
        });

        Mapton.getGlobalState().addListener(gsce -> {
            updateBackgroundColor();
        }, MKey.APP_THEME_BACKGROUND);

    }

    private void initActionsFx() {
        //Toolbox
        mToolboxAction = new Action(Dict.APPLICATION_TOOLS.toString(), event -> {
            if (shouldOpen(mToolboxPopOver)) {
                show(mToolboxPopOver, event.getSource());
            }
        });
        mToolboxAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBar(), mOptions.getIconColorBright()));
        setTooltip(mToolboxAction, new KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN));
    }

    private void initActionsSwing() {
        //Full screen
        mFullscreenAction = new FxActionSwingCheck(Dict.FULL_SCREEN.toString(), () -> {
            if (IS_MAC) {
                Actions.forID("Almond", "se.trixon.almond.nbp.osx.actions.ToggleFullScreenAction").actionPerformed(null);
            } else {
                Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
            }
        });
        mFullscreenAction.setAccelerator(KeyCombination.keyCombination("F11"));
        mFullscreenAction.setGraphic(MaterialIcon._Navigation.FULLSCREEN.getImageView(getIconSizeToolBar(), mOptions.getIconColorBright()));
        setTooltip(mFullscreenAction, new KeyCodeCombination(KeyCode.F11));

        //Map
        mMapAction = new FxActionSwingCheck(Dict.MAP.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OnlyMapAction").actionPerformed(null);
        });
        mMapAction.setGraphic(MaterialIcon._Maps.MAP.getImageView(getIconSizeToolBar(), mOptions.getIconColorBright()));
        mMapAction.setAccelerator(KeyCombination.keyCombination("F12"));
        mMapAction.setSelected(mOptions.isMapOnly());
        setTooltip(mMapAction, new KeyCodeCombination(KeyCode.F12));
    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = (JFrame) Almond.getFrame();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    updateBackgroundColor();
                    final boolean fullscreen = frame.isUndecorated();
                    mOptions.setFullscreen(fullscreen);
                    mFullscreenAction.setSelected(fullscreen);
                    Platform.runLater(() -> {
                        MaterialIcon._Navigation fullscreenIcon = fullscreen == true ? MaterialIcon._Navigation.FULLSCREEN_EXIT : MaterialIcon._Navigation.FULLSCREEN;
                        mFullscreenAction.setGraphic(fullscreenIcon.getImageView(getIconSizeToolBar(), mOptions.getIconColorBright()));
                    });
                }
            });
        });

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_ONLY:
                    mMapAction.setSelected(mOptions.isMapOnly());
                    mOptions.maximizedMapProperty().set(mOptions.isMapOnly());
                    break;
            }
        });

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                Notifications notifications = evt.getValue();
                notifications.owner(AppToolBar.this).position(Pos.TOP_RIGHT);

                switch (evt.getKey()) {
                    case MKey.NOTIFICATION:
                        notifications.show();
                        break;

                    case MKey.NOTIFICATION_CONFIRM:
                        notifications.showConfirm();
                        break;

                    case MKey.NOTIFICATION_ERROR:
                        notifications.showError();
                        break;

                    case MKey.NOTIFICATION_INFORMATION:
                        notifications.showInformation();
                        break;

                    case MKey.NOTIFICATION_WARNING:
                        notifications.showWarning();
                        break;

                    default:
                        throw new AssertionError();
                }
            });
        }, MKey.NOTIFICATION, MKey.NOTIFICATION_CONFIRM, MKey.NOTIFICATION_ERROR, MKey.NOTIFICATION_INFORMATION, MKey.NOTIFICATION_WARNING);

        Mapton.getGlobalState().addListener(evt -> {
            Platform.runLater(() -> {
                mStatusLabel.setText(evt.getValue());
                ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(250), mStatusLabel);
                scaleTransition.setToX(1.2);
                scaleTransition.setToY(1.2);
                scaleTransition.setCycleCount(8);
                scaleTransition.setAutoReverse(true);

                scaleTransition.play();
            });
        }, MKey.APP_TOOL_LABEL);

        Mapton.getGlobalState().addListener(evt -> {
            Platform.runLater(() -> {
                mToolboxPopOver.hide();
            });
        }, MKey.APP_TOOL_STARTED);
    }

    private void initPopOvers() {
        mToolboxPopOver = new PopOver();
        initPopOver(mToolboxPopOver, Dict.APPLICATION_TOOLS.toString(), new AppToolboxView(), true);
        mToolboxPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        mToolboxPopOver.setCloseButtonEnabled(true);
        mToolboxPopOver.setDetachable(true);
        mToolboxPopOver.setOnShowing(event -> {
            mToolboxPopOver.getScene().getStylesheets().remove(CSS_FILE);
        });
        mToolboxPopOver.setOnHiding(event -> {
            getButtonForAction(mToolboxAction).getStylesheets().add(CSS_FILE);
            onObjectHiding(mToolboxPopOver);
        });
    }

    private void updateBackgroundColor() {
        FxHelper.runLaterDelayed(10, () -> {
            setBackground(FxHelper.createBackground(Mapton.getThemeColor()));
        });
    }
}
