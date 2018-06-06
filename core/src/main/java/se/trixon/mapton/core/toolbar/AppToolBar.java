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
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import static se.trixon.mapton.core.api.Mapton.getIconSizeContextMenu;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBar;
import se.trixon.mapton.core.api.MaptonOptions;
import se.trixon.mapton.core.api.ToolActionProvider;
import se.trixon.mapton.core.bookmark.BookmarkView;
import se.trixon.mapton.core.layer.LayerView;
import se.trixon.mapton.core.map.SearchView;
import se.trixon.mapton.core.map.StyleView;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBar extends ToolBar {

    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private Action mBookmarkAction;
    private PopOver mBookmarkPopOver;
    private final java.awt.event.ActionEvent mDummySwingActionEvent = new java.awt.event.ActionEvent(new JButton(), 0, "");
    private FxActionSwing mHomeAction;
    private Action mLayerAction;
    private PopOver mLayerPopOver;
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
    private FxActionSwingCheck mSysViewMapAction;
    private FxActionSwing mSysViewResetAction;
    private MenuButton mToolsMenuButton;

    public AppToolBar() {
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();
        setDisable(true);
    }

    public void toogleBookmarkPopover() {
        if (mBookmarkPopOver.isShowing()) {
            mBookmarkPopOver.hide();
        } else {
            mStylePopOver.hide();
            mLayerPopOver.hide();
            ((ButtonBase) getItems().get(1)).fire();
        }
    }

    public void toogleLayerPopover() {
        if (mLayerPopOver.isShowing()) {
            mLayerPopOver.hide();
        } else {
            mStylePopOver.hide();
            mBookmarkPopOver.hide();
            ((ButtonBase) getItems().get(2)).fire();
        }
    }

    public void toogleStylePopover() {
        if (mStylePopOver.isShowing()) {
            mStylePopOver.hide();
        } else {
            mLayerPopOver.hide();
            mBookmarkPopOver.hide();
            ((ButtonBase) getItems().get(3)).fire();
        }
    }

    private void init() {
        ActionGroup viewActionGroup = new ActionGroup(Dict.VIEW.toString(),
                mSysViewAlwaysOnTopAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysViewResetAction
        );

        ActionGroup systemActionGroup;
        if (IS_MAC) {
            systemActionGroup = new ActionGroup(Dict.MENU.toString(), MaterialIcon._Navigation.MENU.getImageView(getIconSizeToolBar()),
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mSysPluginsAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mSysHelpAction
            );
        } else {
            systemActionGroup = new ActionGroup(Dict.MENU.toString(), MaterialIcon._Navigation.MENU.getImageView(getIconSizeToolBar()),
                    viewActionGroup,
                    ActionUtils.ACTION_SEPARATOR,
                    mSysOptionsAction,
                    mSysPluginsAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mSysHelpAction,
                    mSysAboutAction,
                    ActionUtils.ACTION_SEPARATOR,
                    mSysQuitAction
            );
        }

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mHomeAction,
                mBookmarkAction,
                mLayerAction,
                mStyleAction,
                ActionUtils.ACTION_SPAN,
                mSysViewMapAction,
                mSysViewFullscreenAction,
                systemActionGroup
        ));

        mToolsMenuButton = new MenuButton(Dict.TOOLS.toString());
        Lookup.getDefault().lookupResult(ToolActionProvider.class).addLookupListener((LookupEvent ev) -> {
            populateMenuTools();
        });
        populateMenuTools();

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);

            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeContextMenu() * 1.5);
            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            getItems().add(4, new SearchView().getPresenter());
            getItems().add(5, mToolsMenuButton);
        });

    }

    private void initActionsFx() {
        //Bookmark
        mBookmarkAction = new Action(Dict.BOOKMARKS.toString(), (ActionEvent event) -> {
            if (mOptions.isPreferPopover()) {
                mBookmarkPopOver.show((Node) event.getSource());
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "se.trixon.mapton.core.bookmark.BookmarkAction").actionPerformed(null);
                });
            }
        });
        mBookmarkAction.setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBar()));
        mBookmarkAction.setSelected(mOptions.isBookmarkVisible());

        //Layer
        mLayerAction = new Action(Dict.LAYERS.toString(), (ActionEvent event) -> {
            if (mOptions.isPreferPopover()) {
                mLayerPopOver.show((Node) event.getSource());
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "se.trixon.mapton.core.layer.LayerAction").actionPerformed(null);
                });
            }
        });
        mLayerAction.setGraphic(MaterialIcon._Maps.LAYERS.getImageView(getIconSizeToolBar()));
        mLayerAction.setSelected(mOptions.isBookmarkVisible());

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
        //Home
        mHomeAction = new FxActionSwing(Dict.HOME.toString(), () -> {
            Actions.forID("Mapton", "se.trixon.mapton.core.action.HomeAction").actionPerformed(null);
        });
        mHomeAction.setGraphic(MaterialIcon._Action.HOME.getImageView(getIconSizeToolBar()));
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
            Actions.forID("Mapton", "se.trixon.mapton.core.action.OnlyMapAction").actionPerformed(null);
        });
        mSysViewMapAction.setGraphic(MaterialIcon._Maps.MAP.getImageView(getIconSizeToolBar()));
        mSysViewMapAction.setAccelerator(KeyCombination.keyCombination("F12"));
        mSysViewMapAction.setSelected(mOptions.isMapOnly());

        //OnTop
        mSysViewAlwaysOnTopAction = new FxActionSwingCheck(Dict.ALWAYS_ON_TOP.toString(), () -> {
            Actions.forID("View", "se.trixon.almond.nbp.StayOnTopAction").actionPerformed(null);
        });
        mSysViewAlwaysOnTopAction.setSelected(mAlmondOptions.getAlwaysOnTop());

        //Reset
        mSysViewResetAction = new FxActionSwing(Dict.RESET_WINDOWS.toString(), () -> {
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
            Actions.forID("Mapton", "se.trixon.mapton.core.action.OptionsAction").actionPerformed(null);
        });
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
        mBookmarkPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        mBookmarkPopOver.setHeaderAlwaysVisible(true);
        mBookmarkPopOver.setCloseButtonEnabled(false);
        mBookmarkPopOver.setDetachable(false);
        mBookmarkPopOver.setContentNode(new BookmarkView());
        mBookmarkPopOver.setAnimated(false);

        mLayerPopOver = new PopOver();
        mLayerPopOver.setTitle(Dict.LAYERS.toString());
        mLayerPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        mLayerPopOver.setHeaderAlwaysVisible(true);
        mLayerPopOver.setCloseButtonEnabled(false);
        mLayerPopOver.setDetachable(false);
        mLayerPopOver.setContentNode(new LayerView());
        mLayerPopOver.setAnimated(false);

        mStylePopOver = new PopOver();
        mStylePopOver.setTitle(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()));
        mStylePopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        mStylePopOver.setHeaderAlwaysVisible(true);
        mStylePopOver.setCloseButtonEnabled(false);
        mStylePopOver.setDetachable(false);
        mStylePopOver.setContentNode(new StyleView());
        mStylePopOver.setAnimated(false);

    }

    private void populateMenuTools() {
        ObservableList<MenuItem> menuButtonItems = mToolsMenuButton.getItems();
        menuButtonItems.clear();
        TreeMap<String, Menu> parents = new TreeMap<>();
        ArrayList<MenuItem> rootItems = new ArrayList<>();

        Lookup.getDefault().lookupAll(ToolActionProvider.class).forEach((toolActionProvider) -> {
            final MenuItem menuItem = ActionUtils.createMenuItem(toolActionProvider.getAction());
            final String parent = toolActionProvider.getParent();

            if (parent == null) {
                rootItems.add(menuItem);
            } else {
                parents.computeIfAbsent(parent, k -> new Menu(parent)).getItems().add(menuItem);
            }
        });

        Comparator<MenuItem> menuItemComparator = (MenuItem o1, MenuItem o2) -> o1.getText().compareTo(o2.getText());
        parents.values().forEach((parent) -> {
            FXCollections.sort(parent.getItems(), menuItemComparator);
            menuButtonItems.add(parent);
        });

        if (!rootItems.isEmpty() && !parents.isEmpty()) {
            menuButtonItems.add(new SeparatorMenuItem());
        }

        Collections.sort(rootItems, menuItemComparator);

        rootItems.forEach((rootItem) -> {
            menuButtonItems.add(rootItem);
        });
    }
}
