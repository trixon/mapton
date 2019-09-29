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
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.codecentric.centerdevice.MenuToolkit;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.*;
import org.netbeans.modules.autoupdate.ui.PluginManagerUI;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.LifecycleManager;
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
    private Action mHelpAction;
//    private NewsModule mNewsModule;
    private Action mOptionsAction;
    private ToolbarItem mOptionsToolbarItem;
    private Action mPluginAction;
    private SwingNode mPluginManagerUiNode;
//    private PreferencesModule mPreferencesModule;
    private Stage mStage;
    private Workbench mWorkbench;

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the application can not be launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public MaptonApplication() {
        initPluginManagerUi(mPluginManagerUiNode = new SwingNode());
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(MaptonApplication.class.getResourceAsStream("logo.png")));

        mAlmondFX.addStageWatcher(stage, MaptonApplication.class);
        createUI();
        if (IS_MAC) {
            initMac();
        }
        mStage.setTitle(APP_TITLE);
        mStage.show();
        initAccelerators();
        initListeners();
        //mWorkbench.openModule(mPreferencesModule);
    }

    @Override
    public void stop() throws Exception {
        LifecycleManager.getDefault().exit();
    }

    private void activateModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        int pageIndex = 0;//TODO get actual page index
        int moduleIndex = pageIndex * mWorkbench.getModulesPerPage() + moduleIndexOnPage - 1;
        try {
            mWorkbench.openModule(mWorkbench.getModules().get(moduleIndex));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    private void activateOpenModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        try {
            mWorkbench.openModule(mWorkbench.getOpenModules().get(moduleIndexOnPage - 1));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    private void createUI() {
//        mPreferencesModule = new PreferencesModule();
//        mNewsModule = new NewsModule();

        mWorkbench = Workbench.builder().build();

        mWorkbench.getStylesheets().add(MaptonApplication.class.getResource("customTheme.css").toExternalForm());
        initToolbar();
        initWorkbenchDrawer();

        populateTools();
        Scene scene = new Scene(mWorkbench);
//        scene.getStylesheets().add("css/modena_dark.css");
        mStage.setScene(scene);
    }

    private void displayOptions() {
//        mWorkbench.openModule(mPreferencesModule);
    }

    private void initAccelerators() {
        final ObservableMap<KeyCombination, Runnable> accelerators = mStage.getScene().getAccelerators();
        for (int i = 0; i < 10; i++) {
            final int index = i;
            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), (Runnable) () -> {
                activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), (Runnable) () -> {
                activateModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("DIGIT" + i), KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                activateOpenModule(index);
            });

            accelerators.put(new KeyCodeCombination(KeyCode.valueOf("NUMPAD" + i), KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                activateOpenModule(index);
            });
        }

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        accelerators.put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
            if (mWorkbench.getActiveModule() != null) {
                mWorkbench.closeModule(mWorkbench.getActiveModule());
            }
        });

        if (!IS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), (Runnable) () -> {
                displayOptions();
            });
        }
    }

    private void initListeners() {
//        Lookup.getDefault().lookupResult(TbTool.class).addLookupListener((LookupEvent ev) -> {
//            populateTools();
//        });
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

    private void initToolbar() {
        mOptionsToolbarItem = new ToolbarItem(Dict.OPTIONS.toString(), MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_TOOLBAR, Color.LIGHTGRAY),
                event -> {
                    displayOptions();
                }
        );

        mWorkbench.getToolbarControlsRight().addAll(mOptionsToolbarItem);
    }

    private void initWorkbenchDrawer() {
        //options
        mOptionsAction = new Action(Dict.OPTIONS.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();
            displayOptions();
        });
        mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_DRAWER));

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
        //mHelpAction.setAccelerator(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY));
        mHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));

        //about
        Action aboutAction = new Action(Dict.ABOUT.toString(), (ActionEvent event) -> {
            mWorkbench.hideNavigationDrawer();

            AboutModel aboutModel = new AboutModel(
                    SystemHelper.getBundle(getClass(), "about"),
                    SystemHelper.getResourceAsImageView(MaptonApplication.class, "logo.png")
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

        mWorkbench.getNavigationDrawerItems().setAll(
                ActionUtils.createMenuItem(mPluginAction),
                ActionUtils.createMenuItem(mHelpAction),
                ActionUtils.createMenuItem(aboutAction)
        );

        if (!IS_MAC) {
            mOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }
    }

    private void populateTools() {
//        mTools = new ArrayList<>(Lookup.getDefault().lookupAll(TbTool.class));
//        Collections.sort(mTools, (TbTool o1, TbTool o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
//
//        mWorkbench.getModules().clear();
//        for (TbTool tool : mTools) {
//            mWorkbench.getModules().add(tool.getModule());
//        }
//
//        TbPreferences.getInstance();
////        try {
////            TbPreferences.getInstance().createPreferences();
////        } catch (Exception e) {
////            Exceptions.printStackTrace(e);
////        }
//        mWorkbench.getModules().addAll(mNewsModule, mPreferencesModule);
    }

}
