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
package org.mapton.core.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MOptions;
import org.mapton.api.MTopComponent;
import org.mapton.api.MWhatsHereEngine;
import org.mapton.api.Mapton;
import org.mapton.core.ui.AppStatusView.StatusWindowMode;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui//Map//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true, position = Integer.MIN_VALUE)
@ActionID(category = "Window", id = "org.mapton.core.ui.MapTopComponent")
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

    private static final Logger LOGGER = Logger.getLogger(MEngine.class.getName());
    private final HashSet<TopComponent> mActiveMapMagnets = new HashSet<>();
    private AppStatusPanel mAppStatusPanel;
    private final ResourceBundle mBundle = NbBundle.getBundle(MapTopComponent.class);
    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private File mDestination;
    private MEngine mEngine;
    private final HashSet<TopComponent> mMapMagnets = new HashSet<>();
    private final Mapton mMapton = Mapton.getInstance();
    private final MOptions mOptions = MOptions.getInstance();
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        mMOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_ENGINE:
                    setEngine(Mapton.getEngine());

                case MOptions.KEY_DISPLAY_CROSSHAIR:
                    repaint();
                    revalidate();
                    break;

                default:
                    break;
            }
        });
    }

    public void displayContextMenu(Point screenXY) {
        Platform.runLater(() -> {
            Node rootNode = AppStatusView.getInstance();
            rootNode.getScene().getWindow().requestFocus();
            rootNode.requestFocus();

            mContextMenu.show(rootNode, screenXY.x, screenXY.y);
        });
    }

    @Override
    public void paint(Graphics g) {
        try {
            super.paint(g);

            if (mOptions.is(MOptions.KEY_DISPLAY_CROSSHAIR)) {
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

        AppStatusView.getInstance().setWindowMode(StatusWindowMode.OTHER);
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

        AppStatusView.getInstance().setWindowMode(StatusWindowMode.MAP);
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initContextMenu();
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
                    mRoot.setBottom(AppStatusView.getInstance());
                } else {
                    if (mRoot.getBottom() != null) {
                        mRoot.setBottom(null);
                        getStatusPanel().resetFx();
                    }
                }
            });
        }
    }

    private void copyImage() {
        mContextMenu.hide();
        try {
            SystemHelper.copyToClipboard(mEngine.getImageRenderer().call());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Scene createScene() {
        mRoot = new BorderPane();

        initListeners();

        return new Scene(mRoot);
    }

    private void exportImage() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image (*.png)", "*.png");
        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(new FileChooser.ExtensionFilter(Dict.ALL_FILES.toString(), "*"));
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        //SimpleDialog.setOwner(mStage);
        SimpleDialog.setTitle(mBundle.getString("export_view"));

        SimpleDialog.setPath(mDestination == null ? FileUtils.getUserDirectory() : mDestination.getParentFile());
        SimpleDialog.setSelectedFile(new File(new SimpleDateFormat("'Mapton_'yyyyMMdd_HHmmss").format(new Date())));

        mContextMenu.hide();
        if (SimpleDialog.saveFile(new String[]{"png"})) {
            mDestination = SimpleDialog.getPath();
            try {
                ImageIO.write(mEngine.getImageRenderer().call(), "png", mDestination);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private AppStatusPanel getStatusPanel() {
        if (mAppStatusPanel == null) {
            mAppStatusPanel = AppStatusPanel.getInstance();
        }

        return mAppStatusPanel;
    }

    private void initContextMenu() {
        Action setHomeAction = new Action(MDict.SET_HOME.toString(), (ActionEvent t) -> {
            mMOptions.setMapHome(mEngine.getCenter());
            mMOptions.setMapHomeZoom(mEngine.getZoom());
        });

        Action whatsHereAction = new Action(mBundle.getString("whats_here"), (ActionEvent t) -> {
            whatsHere();
        });

        Action copyImageAction = new Action(mBundle.getString("copy_image"), (ActionEvent t) -> {
            copyImage();
        });
        copyImageAction.setDisabled(true);

        Action exportImageAction = new Action(mBundle.getString("export_image"), (ActionEvent t) -> {
            exportImage();
        });
        exportImageAction.setDisabled(true);

        Collection<? extends Action> actions = Arrays.asList(
                whatsHereAction,
                MBookmarkManager.getInstance().getAddBookmarkAction(),
                ActionUtils.ACTION_SEPARATOR,
                copyImageAction,
                exportImageAction,
                ActionUtils.ACTION_SEPARATOR,
                ActionUtils.ACTION_SEPARATOR,
                setHomeAction
        );

        mContextCopyMenu = new Menu(mBundle.getString("copy_location"));
        mContextOpenMenu = new Menu(mBundle.getString("open_location"));
        mContextExtrasMenu = new Menu(mBundle.getString("extras"));
        mContextMenu = ActionUtils.createContextMenu(actions);

        int insertPos = mContextMenu.getItems().size() - 2;
        mContextMenu.getItems().add(insertPos, mContextExtrasMenu);
        mContextMenu.getItems().add(insertPos, mContextOpenMenu);
        mContextMenu.getItems().add(insertPos, mContextCopyMenu);
        mContextMenu.setOnShowing((event) -> {
            copyImageAction.setDisabled(mEngine.getImageRenderer() == null);
            exportImageAction.setDisabled(mEngine.getImageRenderer() == null);
        });

        Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener((LookupEvent ev) -> {
            populateContextProviders();
        });

        populateContextProviders();
    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            addHierarchyListener((HierarchyEvent hierarchyEvent) -> {
                if (hierarchyEvent.getChangedParent() instanceof JLayeredPane) {
                    Dimension d = ((JFrame) WindowManager.getDefault().getMainWindow()).getContentPane().getPreferredSize();
                    final boolean showOnlyMap = 1 == d.height && 1 == d.width;
                    mMOptions.setMapOnly(showOnlyMap);
                    try {
                        attachStatusbar();
                    } catch (NullPointerException e) {
                    }
                }
            });
        });
    }

    private void populateContextProviders() {
        Platform.runLater(() -> {
            mContextCopyMenu.getItems().clear();
            mContextOpenMenu.getItems().clear();
            mContextExtrasMenu.getItems().clear();

            ArrayList<MContextMenuItem> contextMenues = new ArrayList<>(Lookup.getDefault().lookupAll(MContextMenuItem.class));
            contextMenues.sort((MContextMenuItem o1, MContextMenuItem o2) -> o1.getName().compareTo(o2.getName()));

            for (MContextMenuItem provider : contextMenues) {
                MenuItem item = new MenuItem(provider.getName());
                switch (provider.getType()) {
                    case COPY:
                        mContextCopyMenu.getItems().add(item);
                        item.setOnAction((ActionEvent event) -> {
                            String s = provider.getUrl();
                            NbLog.v("Copy location", s);
                            SystemHelper.copyToClipboard(s);
                        });
                        break;

                    case EXTRAS:
                        mContextExtrasMenu.getItems().add(item);
                        item.setOnAction(provider.getAction());
                        break;

                    case OPEN:
                        mContextOpenMenu.getItems().add(item);
                        item.setOnAction((ActionEvent event) -> {
                            String s = provider.getUrl();
                            if (!StringUtils.isBlank(s)) {
                                NbLog.v("Open location", s);
                                SystemHelper.desktopBrowse(s);
                            }
                        });
                        break;

                    default:
                        throw new AssertionError();
                }
            }

            mContextCopyMenu.getItems().sorted((MenuItem o1, MenuItem o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextCopyMenu.setVisible(!mContextCopyMenu.getItems().isEmpty());

            mContextOpenMenu.getItems().sorted((MenuItem o1, MenuItem o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextOpenMenu.setVisible(!mContextOpenMenu.getItems().isEmpty());

            mContextExtrasMenu.getItems().sorted((MenuItem o1, MenuItem o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextExtrasMenu.setVisible(!mContextExtrasMenu.getItems().isEmpty());
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
                final JComponent engineUI = (JComponent) engine.getUI();
                engineUI.setMinimumSize(new Dimension(1, 1));
                engineUI.setPreferredSize(new Dimension(1, 1));
                add(getFxPanel(), BorderLayout.NORTH);
                getFxPanel().setVisible(false);
                add(engineUI, BorderLayout.CENTER);
                attachStatusbar();
                revalidate();
                repaint();

                try {
                    engine.onActivate();
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                    Mapton.getAppToolBar().refreshEngine(engine);
                } catch (NullPointerException e) {
                }
            });
        } else {
            Platform.runLater(() -> {
                resetFx();
                mRoot.setCenter((Node) engine.getUI());
                attachStatusbar();
                try {
                    engine.onActivate();
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (Exception e) {
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                    Mapton.getAppToolBar().refreshEngine(engine);
                });
            });
        }

        Mapton.logLoading("Map Engine", engine.getName());
    }

    private void whatsHere() {
        Mapton.getGlobalState().put(MEngine.KEY_STATUS_PROGRESS, -1d);

        new Thread(() -> {
            ArrayList< MWhatsHereEngine> engines = new ArrayList<>(Lookup.getDefault().lookupAll(MWhatsHereEngine.class));

            if (!engines.isEmpty()) {
                MWhatsHereEngine whatsHereEngine = engines.get(0);
                int zoom = (int) (5 + mEngine.getZoom() * 18);
                String s = whatsHereEngine.getResult(mEngine.getLatLonMouse(), zoom);
                if (StringUtils.isNotBlank(s)) {
                    NbLog.i(MapTopComponent.class, "WhatsHere: " + s);
                    Mapton.execute(() -> {
                        mEngine.onWhatsHere(s);
                    });
                }

                Mapton.getGlobalState().put(MEngine.KEY_STATUS_PROGRESS, 1d);
            } else {
                //TODO err inf dialog
            }
        }).start();
    }
}
