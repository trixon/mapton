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
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeContextMenu;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core_nb.Initializer;
import org.openide.awt.Actions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.nbp.dialogs.NbAboutFx;
import se.trixon.almond.util.AboutModel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBar extends BaseToolBar {

    private final String CSS_FILE = getClass().getResource("toolbar_app.css").toExternalForm();
    private FxActionSwing mAboutAction;
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private FxActionSwingCheck mAlwaysOnTopAction;
    private FxActionSwingCheck mFullscreenAction;
    private Action mHelpAction;
    private FxActionSwingCheck mMapAction;
    private FxActionSwing mOptionsAction;
    private FxActionSwing mOptionsPlatformAction;
    private FxActionSwing mPluginsAction;
    private FxActionSwing mQuitAction;
    private FxActionSwing mResetWindowsAction;
    private FxActionSwing mRestartAction;
    private Label mStatusLabel;
    private ContextMenu mSystemContextMenu;
    private Action mSystemMenuAction;
    private Action mToolboxAction;
    private PopOver mToolboxPopOver;

    public AppToolBar() {
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();
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
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mPluginsAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mHelpAction
            ));
        } else {
            menuActions.addAll(Arrays.asList(
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mOptionsAction,
                    mOptionsPlatformAction,
                    mPluginsAction,
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
            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeContextMenu() * 1.5);
            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
//                buttonBase.getStylesheets().add(CSS_FILE);
            });

            getStylesheets().add(CSS_FILE);

            mStatusLabel = new Label();
            getItems().add(2, mStatusLabel);
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
        mSystemMenuAction.setGraphic(MaterialIcon._Navigation.MENU.getImageView(getIconSizeToolBarInt(), mOptions.getIconColorBright()));
        setTooltip(mSystemMenuAction, new KeyCodeCombination(KeyCode.CONTEXT_MENU));

        //Toolbox
        mToolboxAction = new Action(Dict.APPLICATION_TOOLS.toString(), event -> {
            if (shouldOpen(mToolboxPopOver)) {
                show(mToolboxPopOver, event.getSource());
            }
        });
        mToolboxAction.setGraphic(MaterialIcon._Content.ADD.getImageView(getIconSizeToolBarInt(), mOptions.getIconColorBright()));
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

        //options
        mOptionsAction = new FxActionSwing(Dict.OPTIONS.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OptionsAction").actionPerformed(null);
        });
        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }

        //options - platform
        mOptionsPlatformAction = new FxActionSwing(String.format("%s (%s)", Dict.OPTIONS.toString(), Dict.PLATFORM.toString()), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OptionsPlatformAction").actionPerformed(null);
        });

        //About
        mAboutAction = new FxActionSwing(String.format(Dict.ABOUT_S.toString(), "Mapton"), () -> {
            AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(Initializer.class, "about"), SystemHelper.getResourceAsImageView(Initializer.class, "logo.png"));
            NbAboutFx nbAboutFx = new NbAboutFx(aboutModel);
            nbAboutFx.display();
        });

        //restart
        mRestartAction = new FxActionSwing(Dict.RESTART.toString(), () -> {
            Actions.forID("File", "se.trixon.almond.nbp.actions.RestartAction").actionPerformed(null);
        });

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
                    mOptionsGeneral.maximizedMapProperty().set(mOptions.isMapOnly());
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
