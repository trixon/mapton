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

import java.awt.BorderLayout;
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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.mapton.core.api.DictMT;
import se.trixon.mapton.core.api.MapContextMenuProvider;
import se.trixon.mapton.core.api.MapEngine;
import se.trixon.mapton.core.api.Mapton;
import se.trixon.mapton.core.api.MaptonOptions;
import se.trixon.mapton.core.api.MaptonTopComponent;
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
public final class MapTopComponent extends MaptonTopComponent {

    private final ResourceBundle mBundle = NbBundle.getBundle(MapTopComponent.class);
    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private final Mapton mMapton = Mapton.getInstance();
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MaptonOptions.KEY_MAP_ENGINE:
                    setEngine(getEngine());
                    break;

                default:
                    break;
            }
        });

        //mMapton.setMapTopComponent(this);
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initContextMenu();
        setEngine(getEngine());
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

    private Scene createScene() {
        mRoot = new BorderPane(new Label("loading map engine..."));

        return new Scene(mRoot);
    }

    private MapEngine getEngine() {
        MapEngine defaultEngine = null;

        for (MapEngine mapEngine : Lookup.getDefault().lookupAll(MapEngine.class)) {
            if (StringUtils.equalsIgnoreCase(mapEngine.getName(), mOptions.getMapEngine())) {
                return mapEngine;
            } else {
                defaultEngine = mapEngine;
            }
        }

        return defaultEngine;
    }

    private void initContextMenu() {
        Action setHomeAction = new Action(DictMT.SET_HOME.toString(), (ActionEvent t) -> {
//            mOptions.setMapHome(mMap.getCenter());
//            mOptions.setMapHomeZoom(mMap.getZoom());
        });

        Action whatsHereAction = new Action(mBundle.getString("whats_here"), (ActionEvent t) -> {
//            whatsHere();
        });

        Action exportImageAction = new Action(mBundle.getString("export_image"), (ActionEvent t) -> {
//            exportImage();
        });

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

//        WebView webView = mMapView.getWebview();
//        webView.setContextMenuEnabled(false);
        mRoot.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                mContextMenu.show(mRoot, e.getScreenX(), e.getScreenY());
            } else {
                mContextMenu.hide();
            }
        });

        Lookup.getDefault().lookupResult(MapContextMenuProvider.class).addLookupListener((LookupEvent ev) -> {
            populateContextProviders();
        });

        populateContextProviders();
    }

    private void populateContextProviders() {
        Platform.runLater(() -> {
            mContextCopyMenu.getItems().clear();
            mContextOpenMenu.getItems().clear();
            mContextExtrasMenu.getItems().clear();

            for (MapContextMenuProvider provider : Lookup.getDefault().lookupAll(MapContextMenuProvider.class)) {
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
                            NbLog.v("Open location", s);
                            SystemHelper.desktopBrowse(s);
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

    private void setEngine(MapEngine mapEngine) {
        if (mapEngine.isSwing()) {
            SwingUtilities.invokeLater(() -> {
                removeAll();
                add((JComponent) mapEngine.getUI(), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
        } else {
            Platform.runLater(() -> {
                resetFx();
                mRoot.setCenter((Node) mapEngine.getUI());
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            });
        }

        NbLog.v(Mapton.LOG_TAG, String.format("Set Map Engine to %s", mapEngine.getName()));
    }

}
