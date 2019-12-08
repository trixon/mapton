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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.HierarchyEvent;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import org.mapton.base.ui.MapContextMenu;
import org.mapton.base.ui.StatusBarView;
import org.mapton.base.ui.StatusBarView.StatusWindowMode;
import org.mapton.base.ui.grid.LocalGridsView;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.api.MTopComponent;
import org.mapton.core_nb.ui.grid.LocalGridEditor;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core_nb.ui//Map//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true, position = Integer.MIN_VALUE)
@ActionID(category = "Window", id = "org.mapton.core_nb.ui.MapTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "D-M")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapAction",
        preferredID = "MapTopComponent"
)
@Messages({
    "CTL_MapAction=Map"
})
public final class MapTopComponent extends MTopComponent {

    private final HashSet<TopComponent> mActiveMapMagnets = new HashSet<>();
    private AppStatusPanel mAppStatusPanel;
    private MEngine mEngine;
    private final HashSet<TopComponent> mMapMagnets = new HashSet<>();
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        MOptions2.getInstance().general().engineProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            setEngine(Mapton.getEngine());
        });
    }

    @Override
    public void paint(Graphics g) {
        try {
            super.paint(g);

            if (mMOptions2.general().isDisplayCrosshair()) {
                Graphics2D g2 = (Graphics2D) g;
                int x = getWidth() / 2;
                int y = getHeight() / 2;
                final int gap = FxHelper.getUIScaled(6);
                final int length = FxHelper.getUIScaled(6) + gap;

                Stroke[] strokes = {new BasicStroke(FxHelper.getUIScaled(5)), new BasicStroke(FxHelper.getUIScaled(2))};
                Color[] colors = {new Color(0f, 0f, 0f, 0.4f), Color.WHITE};

                for (int i = 0; i < 2; i++) {
                    g2.setStroke(strokes[i]);
                    g2.setColor(colors[i]);

                    g2.drawLine(x, y + gap, x, y + length);//Down
                    g2.drawLine(x, y - gap, x, y - length);//Up
                    g2.drawLine(x + gap, y, x + length, y);//Right
                    g2.drawLine(x - gap, y, x - length, y);//Left
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        mMapMagnets.clear();
        mActiveMapMagnets.clear();

        final WindowManager windowManager = WindowManager.getDefault();
        windowManager.getModes().stream().filter((mode) -> !(mode.equals(windowManager.findMode(this)))).forEachOrdered((mode) -> {
            TopComponent selectedTopComponent = mode.getSelectedTopComponent();
            for (TopComponent tc : mode.getTopComponents()) {
                if (tc instanceof MTopComponent && tc.isOpened() && !windowManager.isTopComponentFloating(tc)) {
                    if (tc instanceof MMapMagnet) {
                        if (tc.equals(selectedTopComponent)) {
                            mActiveMapMagnets.add(tc);
                        }

                        tc.close();
                        mMapMagnets.add(tc);
                    }
                }
            }
        });

        StatusBarView.getInstance().setWindowMode(StatusWindowMode.OTHER);
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        setEngine(Mapton.getEngine());
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        for (TopComponent tc : mMapMagnets) {
            tc.open();
        }

        for (TopComponent tc : mActiveMapMagnets) {
            tc.requestActive();
        }

        StatusBarView.getInstance().setWindowMode(StatusWindowMode.MAP);
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        new MapContextMenu();
        mMOptions2 = MOptions2.getInstance();
        mMOptions2.general().displayCrosshairProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            repaint();
            revalidate();
        });

    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private void attachStatusbar() {
        boolean showOnlyMap = mMOptions.isMapOnly();

        if (mEngine.isSwing()) {
            try {
                if (showOnlyMap) {
                    add(getStatusPanel().getFxPanel(), BorderLayout.SOUTH);
                } else {
                    getStatusPanel().resetSwing();
                }
            } catch (NullPointerException e) {
                // nvm
            }
        } else {
            Platform.runLater(() -> {
                if (showOnlyMap) {
                    mRoot.setBottom(StatusBarView.getInstance());
                } else {
                    if (mRoot.getBottom() != null) {
                        mRoot.setBottom(null);
                        getStatusPanel().resetFx();
                    }
                }
            });
        }
    }

    private Scene createScene() {
        mRoot = new BorderPane();

        initListeners();
        LocalGridsView.setLocalGridEditor(LocalGridEditor.getInstance());

        return new Scene(mRoot);
    }

    private AppStatusPanel getStatusPanel() {
        if (mAppStatusPanel == null) {
            mAppStatusPanel = AppStatusPanel.getInstance();
        }

        return mAppStatusPanel;
    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            addHierarchyListener((HierarchyEvent hierarchyEvent) -> {
                if (hierarchyEvent.getChangedParent() instanceof JLayeredPane) {
                    Dimension d = ((JFrame) WindowManager.getDefault().getMainWindow()).getContentPane().getPreferredSize();
                    final boolean showOnlyMap = 40 == d.height && 100 == d.width;
                    mMOptions.setMapOnly(showOnlyMap);
                    try {
                        attachStatusbar();
                    } catch (NullPointerException e) {
                    }
                }
            });
        });
    }

    private void setEngine(MEngine engine) {
        mEngine = engine;
        SwingUtilities.invokeLater(() -> {
            setToolTipText(String.format("%s: %s", MDict.MAP_ENGINE.toString(), engine.getName()));
        });

        if (engine.isSwing()) {
            SwingUtilities.invokeLater(() -> {
                removeAll();
                JComponent engineUI = engine.getMapComponent();
                engineUI.setMinimumSize(new Dimension(1, 1));
                engineUI.setPreferredSize(new Dimension(1, 1));
                add(MapToolBarPanel.getInstance().getToolBarPanel(), BorderLayout.NORTH);
//                add(getFxPanel(), BorderLayout.NORTH);
//                getFxPanel().setVisible(false);
                add(engineUI, BorderLayout.CENTER);
                attachStatusbar();
                revalidate();
                repaint();

                try {
                    engine.onActivate();
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (NullPointerException e) {
                }
            });
        } else {
            Platform.runLater(() -> {
                resetFx();
                mRoot.setTop(MapToolBarPanel.getInstance().getToolBar());
                mRoot.setCenter(engine.getMapNode());
                attachStatusbar();
                try {
                    engine.onActivate();
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (Exception e) {
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            });
        }

        Mapton.logLoading("Map Engine", engine.getName());
    }
}
