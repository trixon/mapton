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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.base.ui.SearchView;
import org.mapton.core_nb.Initializer;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.nbp.dialogs.NbAboutFx;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.nbp.dialogs.NbSystemInformation;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.FxActionSwing;
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
    private FxActionSwing mAboutAction;
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private FxActionSwingCheck mAlwaysOnTopAction;
    private final ResourceBundle mBundle;
    private File mFile;
    private FxActionSwingCheck mFullscreenAction;
    private Action mHelpAction;
    private FxActionSwingCheck mMapAction;
    private FxActionSwing mOpenAction;
    private FxActionSwing mOptionsAction;
    private FxActionSwing mPluginsAction;
    private FxActionSwing mQuitAction;
    private FxActionSwing mResetWindowsAction;
    private FxActionSwing mRestartAction;
    private SearchView mSearchView;
    private Label mStatusLabel;
    private FxActionSwing mSysInfoAction;
    private ContextMenu mSystemContextMenu;
    private Action mSystemMenuAction;
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
            mSearchView.getPresenter().requestFocus();
            ((TextField) mSearchView.getPresenter()).clear();
        });
    }

    public void open() {
        TreeMap<String, ArrayList<MCoordinateFileOpener>> extToCoordinateFileOpeners = new TreeMap<>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).forEach(coordinateFileOpener -> {
            for (String extension : coordinateFileOpener.getExtensions()) {
                extToCoordinateFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(coordinateFileOpener);
            }
        });

        if (!extToCoordinateFileOpeners.isEmpty()) {
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

    public void toggleSystemMenu() {
        Platform.runLater(() -> {
            if (mSystemContextMenu.isShowing()) {
                mSystemContextMenu.hide();
                onObjectHiding(mSystemContextMenu);
            } else {
                Node node = getItems().get(0);
                Bounds bounds = node.getBoundsInLocal();
                Bounds screenBounds = node.localToScreen(bounds);
                mSystemContextMenu.show(node, screenBounds.getMinX(), screenBounds.getMaxY());
            }
        });
    }

    public void toogleToolboxPopOver() {
        tooglePopOver(mToolboxPopOver, mToolboxAction);
    }

    private void init() {
        setPadding(Insets.EMPTY);

        ActionGroup viewActionGroup = new ActionGroup(Dict.VIEW.toString(),
                mAlwaysOnTopAction,
                ActionUtils.ACTION_SEPARATOR,
                mResetWindowsAction
        );

        ArrayList<Action> menuActions = new ArrayList<>();
        if (IS_MAC) {
            menuActions.addAll(Arrays.asList(
                    mOpenAction,
                    ActionUtils.ACTION_SEPARATOR,
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mPluginsAction,
                    mSysInfoAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mHelpAction
            ));
        } else {
            menuActions.addAll(Arrays.asList(
                    mOpenAction,
                    ActionUtils.ACTION_SEPARATOR,
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mOptionsAction,
                    mPluginsAction,
                    mSysInfoAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mHelpAction,
                    mAboutAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mRestartAction,
                    mQuitAction
            ));
        }

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mSystemMenuAction,
                ActionUtils.ACTION_SPAN,
                ActionUtils.ACTION_SPAN,
                mMapAction,
                mToolboxAction
        ));

        if (!IS_MAC) {
            actions.add(actions.size() - 1, mFullscreenAction);
        }

        mSystemContextMenu = ActionUtils.createContextMenu(menuActions);
        mSystemContextMenu.setOnHiding(event -> {
            onObjectHiding(mSystemContextMenu);
        });

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

            getItems().add(2, mStatusLabel = new Label());
            mStatusLabel.setTextFill(mOptions.getIconColorBright());
        });
    }

    private void initActionsFx() {
        //Help
        mHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            SystemHelper.desktopBrowse("https://mapton.org/help/");
        });
        mHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));

        //System menu
        mSystemMenuAction = new Action(Dict.MENU.toString(), event -> {
            if (shouldOpen(mSystemContextMenu)) {
                toggleSystemMenu();
            }
        });
        mSystemMenuAction.setGraphic(MaterialIcon._Navigation.MENU.getImageView(getIconSizeToolBar(), mOptions.getIconColorBright()));
        setTooltip(mSystemMenuAction, new KeyCodeCombination(KeyCode.CONTEXT_MENU));

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
        //open
        mOpenAction = new FxActionSwing(Dict.OPEN.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OpenAction").actionPerformed(null);
        });
        mOpenAction.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));

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

        //OnTop
        mAlwaysOnTopAction = new FxActionSwingCheck(Dict.ALWAYS_ON_TOP.toString(), () -> {
            Actions.forID("View", "se.trixon.almond.nbp.StayOnTopAction").actionPerformed(null);
        });
        mAlwaysOnTopAction.setSelected(mAlmondOptions.getAlwaysOnTop());

        //Reset
        mResetWindowsAction = new FxActionSwing(Dict.RESET_WINDOWS.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ResetWindowsAction").actionPerformed(null);
        });

//
        //Plugins
        mPluginsAction = new FxActionSwing(Dict.PLUGINS.toString(), () -> {
            final java.awt.event.ActionEvent dummySwingActionEvent = new java.awt.event.ActionEvent(new JButton(), 0, "");
            Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction").actionPerformed(dummySwingActionEvent);
        });
        mPluginsAction.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN));

        //SysInfo
        mSysInfoAction = new FxActionSwing(Dict.SYSTEM_INFORMATION.toString(), () -> {
            if (SystemUtils.IS_OS_WINDOWS) {
                Mapton.notification(MKey.NOTIFICATION_INFORMATION, mBundle.getString("collecting_system_information"), mBundle.getString("stay_alert"));
            }

            new NbSystemInformation().displayDialog();
        });

        //options
        mOptionsAction = new FxActionSwing(Dict.OPTIONS.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OptionsAction").actionPerformed(null);
        });
        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }

        //About
        mAboutAction = new FxActionSwing(String.format(Dict.ABOUT_S.toString(), "Mapton"), () -> {
            AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(Initializer.class, "about"), SystemHelperFx.getResourceAsImageView(Initializer.class, "logo.png"));
            NbAboutFx nbAboutFx = new NbAboutFx(aboutModel);
            nbAboutFx.display();
        });

        //restart
        mRestartAction = new FxActionSwing(Dict.RESTART.toString(), () -> {
            Actions.forID("File", "se.trixon.almond.nbp.actions.RestartAction").actionPerformed(null);
        });
        mRestartAction.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN));

        //quit
        mQuitAction = new FxActionSwing(Dict.QUIT.toString(), () -> {
            Actions.forID("File", "se.trixon.almond.nbp.actions.QuitAction").actionPerformed(null);
        });
        mQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));
    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = (JFrame) Almond.getFrame();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
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
}
