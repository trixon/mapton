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
package org.mapton.workbench;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import de.codecentric.centerdevice.MenuToolkit;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import org.mapton.workbench.api.WorkbenchManager;
import org.mapton.workbench.modules.LogModule;
import org.mapton.workbench.modules.MapModule;
import org.mapton.workbench.modules.PreferencesModule;
import org.mapton.workbench.modules.UpdaterModule;
import org.mapton.workbench.modules.updater.UpdateNotificator;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.LifecycleManager;
import org.openide.util.NbBundle;
import se.trixon.almond.util.AboutModel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;

public class MaptonApplication extends Application {

    public static final String APP_TITLE = "Mapton";
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private static final Logger LOGGER = Logger.getLogger(MaptonApplication.class.getName());
    private Action mAboutAction;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private long mFullScreenChanged;
    private Action mHelpAction;
    private Action mLogAction;
    private LogModule mLogModule;
    private Action mRestartAction;
    private UpdaterModule mUpdaterModule;
    private MapModule mMapModule;
    private final MOptions2 mOptions2 = MOptions2.getInstance();
    private Action mOptionsAction;
    private Action mPluginAction;
    private final SwingNode mPluginManagerUiNode;
    private PreferencesModule mPreferencesModule;
    private Action mQuitAction;
    private Stage mStage;
    private Action mUpdateManagerAction;
    private Action mWindowFullscreenAction;
    private Action mWindowOnTopAction;
    private Workbench mWorkbench;
    private WorkbenchManager mWorkbenchManager;

    public static void main(String[] args) {
        launch(args);
    }

    public MaptonApplication() {
        initPluginManagerUi(mPluginManagerUiNode = new SwingNode());
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        mStage.getIcons().add(new Image(WorkbenchManager.class.getResourceAsStream("logo.png")));
        mStage.setFullScreenExitKeyCombination(KeyCombination.valueOf("F11"));

        createUI();

        if (IS_MAC) {
            initMac();
        }

        try {
            String title = NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion");
            if (title.isEmpty()) {
                mStage.setTitle(APP_TITLE);
            } else {
                mStage.setTitle(title);
            }
        } catch (MissingResourceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            mStage.setTitle(APP_TITLE);
        }

        initAccelerators();
        initListeners();

        new UpdateNotificator();
    }

    @Override
    public void stop() throws Exception {
        LifecycleManager.getDefault().exit();
    }

    private void createUI() {
        mWorkbenchManager = WorkbenchManager.getInstance();
        mWorkbench = mWorkbenchManager.getWorkbench();

        MaterialIcon.setDefaultColor(mOptions2.general().getIconColorBright());
        initActions();

        Scene scene = new Scene(mWorkbench);
        mStage.setScene(scene);

        mUpdaterModule = new UpdaterModule();

        mLogModule = new LogModule();
        Mapton.getLog().setOut(mLogModule);
        Mapton.getLog().setErr(mLogModule);
        Mapton.log(SystemHelper.getSystemInfo());
        mPreferencesModule = new PreferencesModule();

        Menu viewMenu = new Menu(Dict.VIEW.toString());
        viewMenu.getItems().setAll(
                ActionUtils.createCheckMenuItem(mWindowFullscreenAction),
                ActionUtils.createCheckMenuItem(mWindowOnTopAction)
        );

        Menu systemMenu = new Menu(Dict.SYSTEM.toString());
        systemMenu.getItems().setAll(
                ActionUtils.createMenuItem(mUpdateManagerAction),
                ActionUtils.createMenuItem(mLogAction),
                ActionUtils.createMenuItem(mPluginAction)
        );

        mWorkbench.getNavigationDrawerItems().setAll(
                ActionUtils.createMenuItem(mOptionsAction),
                viewMenu,
                systemMenu,
                ActionUtils.createMenuItem(mHelpAction),
                ActionUtils.createMenuItem(mAboutAction),
                ActionUtils.createMenuItem(mRestartAction),
                ActionUtils.createMenuItem(mQuitAction)
        );

        Platform.runLater(() -> {
            mAlmondFX.addStageWatcher(mStage, MaptonApplication.class);
            mWindowOnTopAction.setSelected(mStage.isAlwaysOnTop());
            mWindowFullscreenAction.setSelected(mStage.isFullScreen());
            mStage.show();
            mMapModule = new MapModule();
            mWorkbenchManager.getFixModules().add(mMapModule);
            mWorkbenchManager.getFixModules().add(mPreferencesModule);
            mWorkbenchManager.getFixModules().add(mUpdaterModule);
            mWorkbenchManager.getFixModules().add(mLogModule);

            Platform.runLater(() -> {
                mWorkbenchManager.setAllowModulePopulation(true);
                mWorkbenchManager.populateModules();
                mWorkbench.openModule(mMapModule);
            });
        });
    }

    private void displayOptions() {
        mWorkbench.openModule(mPreferencesModule);
    }

    private void initAccelerators() {
        final ObservableMap<KeyCombination, Runnable> accelerators = mStage.getScene().getAccelerators();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), () -> {
                mWorkbenchManager.activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), () -> {
                mWorkbenchManager.activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN), () -> {
                mWorkbenchManager.activateOpenModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN), () -> {
                mWorkbenchManager.activateOpenModule(index);
            });
        }

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        accelerators.put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN), () -> {
            mWorkbench.openModule(mMapModule);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.CONTEXT_MENU, KeyCombination.CONTROL_ANY), () -> {
            mWorkbenchManager.toggleNavigationDrawer();
        });

        accelerators.put(new KeyCodeCombination(KeyCode.F1, KeyCombination.CONTROL_ANY), () -> {
            mHelpAction.handle(null);
        });

        accelerators.put(new KeyCodeCombination(KeyCode.F11, KeyCombination.CONTROL_ANY), () -> {
            if (SystemHelper.age(mFullScreenChanged) > 10) {
                mWindowFullscreenAction.handle(null);
            }
            mWindowFullscreenAction.setSelected(mStage.isFullScreen());
        });

        accelerators.put(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), () -> {
            mLogAction.handle(null);
        });

        final Runnable openModulePage = () -> {
            mWorkbench.openAddModulePage();
        };
        accelerators.put(new KeyCodeCombination(KeyCode.ADD, KeyCombination.SHORTCUT_DOWN), openModulePage);
        accelerators.put(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.SHORTCUT_DOWN), openModulePage);

        accelerators.put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), () -> {
            mWorkbenchManager.closeActiveModule();
        });

        if (!IS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> {
                displayOptions();
            });
        }
    }

    private void initActions() {
        //Window OnTop
        mWindowOnTopAction = new Action(Dict.ALWAYS_ON_TOP.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            mStage.setAlwaysOnTop(!mStage.isAlwaysOnTop());
        });

        //Window Full screen
        mWindowFullscreenAction = new Action(Dict.FULL_SCREEN.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            mStage.setFullScreen(!mStage.isFullScreen());
            mWindowFullscreenAction.setSelected(mStage.isFullScreen());
        });

        //log
        mLogAction = new Action(Dict.LOG.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            mWorkbench.getModules().add(mLogModule);
            mWorkbench.openModule(mLogModule);
        });

        //update manager
        mUpdateManagerAction = new Action(Dict.UPDATE_MANAGER.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            mWorkbench.openModule(mUpdaterModule);
        });

        //restart
        mRestartAction = new Action(Dict.RESTART.toString(), (ActionEvent event) -> {
            LifecycleManager.getDefault().markForRestart();
            LifecycleManager.getDefault().exit();
        });

        //quit
        mQuitAction = new Action(Dict.QUIT.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
        mQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            displayOptions();
        });
        mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        //plugins
        mPluginAction = new Action(Dict.PLUGINS.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Plugins");

            alert.getDialogPane().setContent(mPluginManagerUiNode);
            alert.setResizable(true);
            alert.showAndWait();
        });

        //help
        mHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            SystemHelper.desktopBrowse("https://mapton.org/help/");
        });
        mHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));

        //about
        mAboutAction = new Action(Dict.ABOUT.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();

            AboutModel aboutModel = new AboutModel(
                    SystemHelper.getBundle(getClass(), "about"),
                    SystemHelper.getResourceAsImageView(WorkbenchManager.class, "logo.png")
            );

            AboutPane aboutPane = new AboutPane(aboutModel);

            double scaledFontSize = FxHelper.getScaledFontSize();
            Label appLabel = new Label(aboutModel.getAppName());
            appLabel.setFont(new Font(scaledFontSize * 1.8));
            Label verLabel = new Label(String.format("%s %s", Dict.VERSION.toString(), aboutModel.getAppVersion()));
            verLabel.setFont(new Font(scaledFontSize * 1.2));
            Label dateLabel = new Label(aboutModel.getAppDate());
            dateLabel.setFont(new Font(scaledFontSize * 1.2));

            VBox box = new VBox(appLabel, verLabel, dateLabel);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPadding(new Insets(0, 0, 0, 22));
            BorderPane topBorderPane = new BorderPane(box);
            topBorderPane.setLeft(aboutModel.getImageView());
            topBorderPane.setPadding(new Insets(22));
            BorderPane mainBorderPane = new BorderPane(aboutPane);
            mainBorderPane.setTop(topBorderPane);

            WorkbenchDialog dialog = WorkbenchDialog.builder(Dict.ABOUT.toString(), mainBorderPane, ButtonType.CLOSE).build();
            mWorkbench.showDialog(dialog);
        });

        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }
    }

    private void initListeners() {
        mStage.fullScreenProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mFullScreenChanged = System.currentTimeMillis();
        });
    }

    private void initMac() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
        menuToolkit.setApplicationMenu(applicationMenu);

        applicationMenu.getItems().remove(0);
        MenuItem aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        aboutMenuItem.setOnAction(mAboutAction);

        MenuItem settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
        settingsMenuItem.setOnAction(mOptionsAction);
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        applicationMenu.getItems().add(0, aboutMenuItem);
        applicationMenu.getItems().add(2, settingsMenuItem);

        int cnt = applicationMenu.getItems().size();
        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
    }

    private void initPluginManagerUi(final SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            JButton button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) swingNode.getScene().getWindow();
                        stage.close();
                    });
                }
            });

            PluginManagerUI pluginManagerUI = new PluginManagerUI(button);
            swingNode.setContent(pluginManagerUI);
            PluginManager p;
        });
    }
}
