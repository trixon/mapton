/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.toolbar;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.openide.awt.Actions;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.core.AppStatusPanel;
import static se.trixon.mapton.core.api.Mapton.getIconSizeContextMenu;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBar;
import se.trixon.mapton.core.api.MaptonOptions;
import se.trixon.mapton.core.bookmark.BookmarkView;
import se.trixon.mapton.core.map.MapController;
import se.trixon.mapton.core.map.MapTopComponent;
import se.trixon.mapton.core.map.SearchView;
import se.trixon.mapton.core.map.StyleView;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBar extends ToolBar {

    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private PopOver mBookmarkPopOver;
    private final java.awt.event.ActionEvent mDummySwingActionEvent = new java.awt.event.ActionEvent(new JButton(), 0, "");
    private Action mHomeAction;
    private final MapController mMapController = MapController.getInstance();
    private MapTopComponent mMapTopComponent;
    private final MaptonOptions mOptions = MaptonOptions.getInstance();
    private Action mStyleAction;
    private PopOver mStylePopOver;
    private FxActionSwing mSysAboutAction;
    private Action mSysHelpAction;
    private FxActionSwing mSysOptionsAction;
    private FxActionSwing mSysPluginsAction;
    private FxActionSwing mSysQuitAction;
    private FxActionSwingCheck mSysViewAlwaysOnTopAction;
    private FxActionSwingCheck mSysViewFullscreenAction;
    private FxActionSwing mSysViewLogOutAction;
    private FxActionSwing mSysViewLogSysAction;
    private FxActionSwingCheck mSysViewMapAction;
    private FxActionSwing mSysViewNotesAction;
    private FxActionSwing mSysViewResetAction;
    private Action mWinBookmarkAction;
    private FxActionSwing mWinMapAction;

    public AppToolBar() {
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();
        setDisable(true);
    }

    private void init() {
        //Test
        Action testAction = new Action("-DEV TEST-", (ActionEvent event) -> {
            AppStatusPanel.getInstance().setStatusText("Status updated!");
            SwingUtilities.invokeLater(() -> {

            });
        });
        testAction.setGraphic(MaterialIcon._Alert.WARNING.getImageView(getIconSizeToolBar()));

        ActionGroup viewActionGroup = new ActionGroup(Dict.VIEW.toString(),
                mSysViewMapAction,
                mSysViewAlwaysOnTopAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysViewNotesAction,
                mSysViewLogOutAction,
                mSysViewLogSysAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysViewResetAction
        );

        ActionGroup systemActionGroup = new ActionGroup(Dict.MENU.toString(), MaterialIcon._Navigation.MENU.getImageView(getIconSizeToolBar()),
                viewActionGroup,
                ActionUtils.ACTION_SEPARATOR,
                mSysPluginsAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysHelpAction,
                mSysAboutAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysQuitAction
        );

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mWinMapAction,
                mHomeAction,
                mWinBookmarkAction,
                mStyleAction,
                testAction,
                ActionUtils.ACTION_SPAN,
                mSysViewFullscreenAction,
                mSysOptionsAction,
                systemActionGroup
        ));

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);

            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeContextMenu() * 1.5);
            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            getItems().add(3, new SearchView().getPresenter());
        });

    }

    private void initActionsFx() {
        //Home
        mHomeAction = new Action(Dict.HOME.toString(), (ActionEvent event) -> {
            mMapController.goHome();
        });
        mHomeAction.setGraphic(MaterialIcon._Action.HOME.getImageView(getIconSizeToolBar()));

        //Bookmark
        mWinBookmarkAction = new Action(Dict.BOOKMARKS.toString(), (ActionEvent event) -> {
            if (mOptions.isBookmarkPopover()) {
                mBookmarkPopOver.show((Node) event.getSource());
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "se.trixon.mapton.core.bookmark.BookmarkAction").actionPerformed(null);
                });
            }
        });
        mWinBookmarkAction.setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBar()));
        mWinBookmarkAction.setSelected(mOptions.isBookmarkVisible());

        //Style
        mStyleAction = new Action(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), (ActionEvent event) -> {
            mStylePopOver.show((Node) event.getSource());
        });
        mStyleAction.setGraphic(MaterialIcon._Image.COLOR_LENS.getImageView(getIconSizeToolBar()));

        //Help
        mSysHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            SystemHelper.desktopBrowse("https://trixon.se/projects/mapton/documentation/");
        });
        mSysHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));
    }

    private void initActionsSwing() {
        //Map
        mWinMapAction = new FxActionSwing(Dict.BOOKMARKS.toString(), () -> {
            Actions.forID("Window", "se.trixon.mapton.core.map.MapTopComponent").actionPerformed(null);
        });
        mWinMapAction.setGraphic(MaterialIcon._Maps.MAP.getImageView(getIconSizeToolBar()));
//
//
//
        //Full screen
        mSysViewFullscreenAction = new FxActionSwingCheck(Dict.FULL_SCREEN.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
        });
        mSysViewFullscreenAction.setAccelerator(KeyCombination.keyCombination("F11"));
        mSysViewFullscreenAction.setGraphic(MaterialIcon._Navigation.FULLSCREEN.getImageView(getIconSizeToolBar()));

        //Map
        mSysViewMapAction = new FxActionSwingCheck(Dict.MAP.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
        });
        mSysViewMapAction.setAccelerator(KeyCombination.keyCombination("F12"));
        mSysViewMapAction.setSelected(mOptions.isMapOnly());

        //OnTop
        mSysViewAlwaysOnTopAction = new FxActionSwingCheck(Dict.ALWAYS_ON_TOP.toString(), () -> {
            Actions.forID("View", "se.trixon.almond.nbp.StayOnTopAction").actionPerformed(null);
        });
        mSysViewAlwaysOnTopAction.setSelected(mAlmondOptions.getAlwaysOnTop());

        //Notes
        mSysViewNotesAction = new FxActionSwing(Dict.NOTES.toString(), () -> {
            Actions.forID("Window", "se.trixon.almond.nbp.fx.NotesTopComponent").actionPerformed(null);
        });
        //LogOut
        mSysViewLogOutAction = new FxActionSwing(Dict.OUTPUT.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.io.ui.IOWindowAction").actionPerformed(null);
        });

        //LogSys
        mSysViewLogSysAction = new FxActionSwing(Dict.SYSTEM.toString(), () -> {
            Actions.forID("View", "org.netbeans.core.actions.LogAction").actionPerformed(null);
        });

        //Reset
        mSysViewResetAction = new FxActionSwing(Dict.RESET.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ResetWindowsAction").actionPerformed(null);
        });
//
//
//
        //Plugins
        mSysPluginsAction = new FxActionSwing(Dict.PLUGINS.toString(), () -> {
            Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction").actionPerformed(mDummySwingActionEvent);
        });

        //options
        mSysOptionsAction = new FxActionSwing(Dict.OPTIONS.toString(), () -> {
            Actions.forID("Window", "org.netbeans.modules.options.OptionsWindowAction").actionPerformed(null);
        });
        mSysOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBar()));
        if (!IS_MAC) {
            mSysOptionsAction.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        }

        //About
        mSysAboutAction = new FxActionSwing(String.format(Dict.ABOUT_S.toString(), "Mapton"), () -> {
            Actions.forID("Help", "org.netbeans.core.actions.AboutAction").actionPerformed(null);
        });

        //quit
        mSysQuitAction = new FxActionSwing(Dict.QUIT.toString(), () -> {
            Actions.forID("File", "se.trixon.almond.nbp.actions.QuitAction").actionPerformed(null);
        });
        mSysQuitAction.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN));

    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = (JFrame) Almond.getFrame();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    final boolean fullscreen = frame.isUndecorated();
                    mOptions.setFullscreen(fullscreen);
                    Platform.runLater(() -> {
                        MaterialIcon._Navigation fullscreenIcon = fullscreen == true ? MaterialIcon._Navigation.FULLSCREEN_EXIT : MaterialIcon._Navigation.FULLSCREEN;
                        mSysViewFullscreenAction.setGraphic(fullscreenIcon.getImageView(getIconSizeToolBar()));
                    });
                }
            });
        });

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MaptonOptions.KEY_MAP_ONLY:
                    mSysViewMapAction.setSelected(mOptions.isMapOnly());
                    break;

                default:
                    break;
            }
        });
    }

    private void initPopOvers() {
        mBookmarkPopOver = new PopOver();
        mBookmarkPopOver.setTitle(Dict.BOOKMARKS.toString());
        mBookmarkPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        mBookmarkPopOver.setHeaderAlwaysVisible(true);
        mBookmarkPopOver.setCloseButtonEnabled(false);
        mBookmarkPopOver.setDetachable(false);
        mBookmarkPopOver.setContentNode(new BookmarkView());
        mBookmarkPopOver.setAnimated(false);

        mStylePopOver = new PopOver();
        mStylePopOver.setTitle(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()));
        mStylePopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        mStylePopOver.setHeaderAlwaysVisible(true);
        mStylePopOver.setCloseButtonEnabled(false);
        mStylePopOver.setDetachable(false);
        mStylePopOver.setContentNode(new StyleView());
        mStylePopOver.setAnimated(false);
    }

    private MapTopComponent mGetMapTC() {
        if (mMapTopComponent == null) {
            mMapTopComponent = (MapTopComponent) WindowManager.getDefault().findTopComponent("MapTopComponent");
        }

        return mMapTopComponent;
    }
}
