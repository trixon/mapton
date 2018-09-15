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
package se.trixon.mapton.core.ui;

import fr.dudie.nominatim.model.Address;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
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
import se.trixon.almond.util.fx.dialogs.SimpleDialog;
import se.trixon.mapton.api.MContextMenuItem;
import se.trixon.mapton.api.MDict;
import se.trixon.mapton.api.MEngine;
import se.trixon.mapton.api.MNominatim;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.api.MTopComponent;
import se.trixon.mapton.api.Mapton;
import se.trixon.mapton.core.bookmark.BookmarkManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.ui//Map//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "se.trixon.mapton.core.ui.MapTopComponent")
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

    private AppStatusPanel mAppStatusPanel;
    private final ResourceBundle mBundle = NbBundle.getBundle(MapTopComponent.class);
    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private File mDestination;
    private MEngine mEngine;
    private final Mapton mMapton = Mapton.getInstance();
    private final MNominatim mNominatim = MNominatim.getInstance();
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        mMOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_ENGINE:
                    setEngine(Mapton.getEngine());
                    break;

                default:
                    break;
            }
        });
    }

    public void displayContextMenu(Point screenXY) {
        Platform.runLater(() -> {
            mContextMenu.show(mRoot, screenXY.x, screenXY.y);
        });
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initContextMenu();
        setEngine(Mapton.getEngine());
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

    private void attachStatusbar(boolean showOnlyMap) {
        if (mEngine.isSwing()) {
            try {
                if (showOnlyMap) {
                    add(getStatusbar().getFxPanel(), BorderLayout.SOUTH);
                } else {
                    getStatusbar().resetSwing();
                }
            } catch (NullPointerException e) {
                // nvm
            }
        } else {
            Platform.runLater(() -> {
                if (showOnlyMap) {
                    mRoot.setBottom(getStatusbar().getProvider());
                } else {
                    if (mRoot.getBottom() != null) {
                        mRoot.setBottom(null);
                        getStatusbar().resetFx();
                    }
                }
            });
        }
    }

    private Scene createScene() {
        mRoot = new BorderPane(new Label("loading map engine..."));

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

        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
            SimpleDialog.setSelectedFile(new File(""));
        }

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

    private AppStatusPanel getStatusbar() {
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

        Action exportImageAction = new Action(mBundle.getString("export_image"), (ActionEvent t) -> {
            exportImage();
        });
        exportImageAction.setDisabled(true);

        Collection<? extends Action> actions = Arrays.asList(
                whatsHereAction,
                BookmarkManager.getInstance().getAddBookmarkAction(),
                ActionUtils.ACTION_SEPARATOR,
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
            exportImageAction.setDisabled(mEngine.getImageRenderer() == null);
        });

        Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener((LookupEvent ev) -> {
            populateContextProviders();
        });

        populateContextProviders();
    }

    private void initListeners() {
        SwingUtilities.invokeLater(() -> {
            addHierarchyListener((HierarchyEvent e) -> {
                if (e.getChangedParent() instanceof JLayeredPane) {
                    Dimension d = ((JFrame) WindowManager.getDefault().getMainWindow()).getContentPane().getPreferredSize();
                    final boolean showOnlyMap = 1 == d.height && 1 == d.width;
                    mMOptions.setMapOnly(showOnlyMap);
                    attachStatusbar(showOnlyMap);
                }
            });
        });
    }

    private void populateContextProviders() {
        Platform.runLater(() -> {
            mContextCopyMenu.getItems().clear();
            mContextOpenMenu.getItems().clear();
            mContextExtrasMenu.getItems().clear();

            for (MContextMenuItem provider : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
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
        if (engine.isSwing()) {
            SwingUtilities.invokeLater(() -> {
                removeAll();
                final JComponent ui = (JComponent) engine.getUI();
                ui.setMinimumSize(new Dimension(1, 1));
                ui.setPreferredSize(new Dimension(1, 1));
                add(getFxPanel(), BorderLayout.NORTH);
                getFxPanel().setVisible(false);
                add(ui, BorderLayout.CENTER);
                attachStatusbar(mMOptions.isMapOnly());
                revalidate();
                repaint();
                try {
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (NullPointerException e) {
                }
            });
        } else {
            Platform.runLater(() -> {
                resetFx();
                mRoot.setCenter((Node) engine.getUI());
                attachStatusbar(mMOptions.isMapOnly());
                try {
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (Exception e) {
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            });
        }

        Mapton.getAppToolBar().refreshEngine(engine);
        NbLog.v(Mapton.LOG_TAG, String.format("Set Map Engine to %s", engine.getName()));
    }

    private void whatsHere() {
        Platform.runLater(() -> {
            getStatusbar().getProvider().setProgress(-1);
        });

        new Thread(() -> {
            try {
                int zoom = (int) (5 + mEngine.getZoom() * 18);
                Address address = mNominatim.getAddress(mEngine.getLatLonMouse(), zoom);
                NbLog.v(Mapton.LOG_TAG, ToStringBuilder.reflectionToString(address, ToStringStyle.MULTI_LINE_STYLE));
                String s = address.getDisplayName();

                Mapton.execute(() -> {
                    mEngine.onWhatsHere(s);
                });

                Platform.runLater(() -> {
                    getStatusbar().getProvider().setProgress(1);
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }
}
