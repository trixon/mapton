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
package org.mapton.core.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.base.ui.FxOnScreenDummy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

@ActionID(category = "Mapton", id = "org.mapton.core.actions.MenuToolBarAction")
@ActionRegistration(lazy = false, displayName = "menu tool bar")

@ActionReference(path = "Menu", position = 9009)
/**
 * <pre>
 * This class appears in the main window menu bar and is the parent of FxOnScreenDummy.
 * It also contains a colored box as an optional indicator.
 * The previous label is deprecated and the JFrame title shoud be used instead.
 * </pre>
 *
 * @author Patrik Karlström
 */
public final class AppMenuToolBarAction extends AbstractAction implements Presenter.Toolbar {

    private final JFXPanel mFxPanel = new JFXPanel();
    private final JPanel mPanel = new JPanel(new BorderLayout());

    public AppMenuToolBarAction() {
        init();
        initListeners();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Component getToolbarPresenter() {
        return mPanel;
    }

    private void init() {
        if (!Mapton.getThemeColor().equals(Mapton.getDefaultThemeColor())) {
            mPanel.setBackground(FxHelper.colorToColor(Mapton.getThemeColor()));
        }

        mPanel.add(mFxPanel, BorderLayout.EAST);
        var minDimension = new Dimension(20, 20);
        mPanel.setMinimumSize(minDimension);
        mPanel.addHierarchyListener(hierarchyEvent -> {
            int height = mPanel.getParent().getPreferredSize().height;
            var dimension = new Dimension(height, height);
            mPanel.setMaximumSize(dimension);
            mPanel.setPreferredSize(dimension);
            mPanel.setBackground(mPanel.getParent().getBackground());
        });

        SystemHelper.runLaterDelayed(500, () -> {
            Platform.runLater(() -> {
                var scene = new Scene(FxOnScreenDummy.getInstance());
                FxHelper.loadDarkTheme(scene);
                mFxPanel.setScene(scene);
            });
        });
    }

    private void initListeners() {
        var globalState = Mapton.getGlobalState();

        globalState.addListener(gsce -> {
            SwingHelper.runLater(() -> {
                mPanel.setBackground(FxHelper.colorToColor(Mapton.getThemeColor()));
            });
        }, MKey.APP_THEME_BACKGROUND, MKey.APP_THEME_FOREGROUND);
    }
}
