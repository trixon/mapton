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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Menu;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javax.swing.JButton;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.openide.awt.Actions;
import se.trixon.almond.nbp.AlmondOptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBar extends ToolBar {

    public static final int ICON_SIZE = 48;
    public static final int ICON_SIZE_MENU = ICON_SIZE / 3;
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private final AlmondOptions mAlmondOptions = AlmondOptions.INSTANCE;
    private final java.awt.event.ActionEvent mDummySwingActionEvent = new java.awt.event.ActionEvent(new JButton(), 0, "");
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Color mIconColor = Color.BLACK;
    private FxActionSwing mSysAboutAction;
    private Action mSysHelpAction;
    private FxActionSwing mSysOptionsAction;
    private FxActionSwing mSysPluginsAction;
    private FxActionSwing mSysQuitAction;
    private FxActionSwingCheck mSysViewAlwaysOnTopAction;
    private FxActionSwingCheck mSysViewFullscreenAction;
    private FxActionSwing mSysViewLogOutAction;
    private FxActionSwing mSysViewLogSysAction;
    private FxActionSwing mSysViewMapAction;
    private FxActionSwing mSysViewResetAction;

    public AppToolBar() {
        initActionsFx();
        initActionsSwing();
        init();
    }

    private void adjustButtonWidth(Stream<Node> stream, double prefWidth) {
        stream.filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            buttonBase.setPrefWidth(prefWidth);
        });
    }

    private Glyph getGlyph(FontAwesome.Glyph glyph) {
        return mFontAwesome.create(glyph).size(ICON_SIZE).color(mIconColor);
    }

    private void init() {
        ActionGroup viewActionGroup = new ActionGroup(Dict.VIEW.toString(),
                mSysViewFullscreenAction,
                mSysViewMapAction,
                mSysViewAlwaysOnTopAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysViewLogOutAction,
                mSysViewLogSysAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysViewResetAction
        );

        ActionGroup systemActionGroup = new ActionGroup(Dict.MENU.toString(), getGlyph(FontAwesome.Glyph.BARS),
                viewActionGroup,
                ActionUtils.ACTION_SEPARATOR,
                mSysPluginsAction,
                mSysOptionsAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysHelpAction,
                mSysAboutAction,
                ActionUtils.ACTION_SEPARATOR,
                mSysQuitAction
        );

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                ActionUtils.ACTION_SPAN,
                systemActionGroup
        ));

        Menu viewMenu = new Menu(Dict.VIEW.toString() + "XX");
        viewMenu.getItems().addAll(
                ActionUtils.createCheckMenuItem(mSysViewFullscreenAction)
        );

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);

            adjustButtonWidth(getItems().stream(), ICON_SIZE * 1.5);
            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });
        });
    }

    private void initActionsFx() {
        //Help
        mSysHelpAction = new Action(Dict.HELP.toString(), (ActionEvent event) -> {
            SystemHelper.desktopBrowse("https://trixon.se/projects/mapton/documentation/");
        });
        mSysHelpAction.setAccelerator(KeyCombination.keyCombination("F1"));
    }

    private void initActionsSwing() {
        //Full screen
        mSysViewFullscreenAction = new FxActionSwingCheck(Dict.FULL_SCREEN.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
        });
        mSysViewFullscreenAction.setAccelerator(KeyCombination.keyCombination("F11"));

        //Map
        mSysViewMapAction = new FxActionSwing(Dict.MAP.toString(), () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
        });
        mSysViewMapAction.setAccelerator(KeyCombination.keyCombination("F12"));

        //OnTop
        mSysViewAlwaysOnTopAction = new FxActionSwingCheck(Dict.ALWAYS_ON_TOP.toString(), () -> {
            Actions.forID("View", "se.trixon.almond.nbp.StayOnTopAction").actionPerformed(null);
        });
        mSysViewAlwaysOnTopAction.setSelected(mAlmondOptions.getAlwaysOnTop());

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
        mSysOptionsAction.setGraphic(getGlyph(FontAwesome.Glyph.COG).size(ICON_SIZE_MENU));
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
}
