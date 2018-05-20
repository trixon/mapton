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
package se.trixon.mapton.core.map;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.MapStateEventType;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
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
import se.trixon.mapton.core.AppStatusPanel;
import se.trixon.mapton.core.AppStatusView;
import se.trixon.mapton.core.api.DictMT;
import se.trixon.mapton.core.api.MapContextMenuProvider;
import se.trixon.mapton.core.api.MapStyleProvider;
import se.trixon.mapton.core.api.Mapton;
import se.trixon.mapton.core.api.MaptonOptions;
import se.trixon.mapton.core.api.MaptonTopComponent;
import se.trixon.mapton.core.bookmark.BookmarkManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//se.trixon.mapton.core.map//Map//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MapTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "se.trixon.mapton.core.map.MapTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Window" /*, position = 333 */)
    ,
    @ActionReference(path = "Shortcuts", name = "D-M")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapAction",
        preferredID = "MapTopComponent"
)
@Messages({
    "CTL_MapAction=Map",
    "CTL_MapTopComponent=Map Window",
    "HINT_MapTopComponent=This is a Map window"
})
public final class MapTopComponent extends MaptonTopComponent {

    private final ResourceBundle mBundle = NbBundle.getBundle(MapTopComponent.class);
    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private File mDestination;

    private GoogleMap mMap;
    private final MapController mMapController = MapController.getInstance();
    private MapOptions mMapOptions;
    private GoogleMapView mMapView;
    private final Mapton mMapton = Mapton.getInstance();
    private final MaptonOptions mOptions = MaptonOptions.getInstance();
    private BorderPane mRoot;
    private AppStatusView mStatusBar;
    private Slider mZoomSlider;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);

        mMapton.setMapTopComponent(this);
    }

    @Override
    public GoogleMap getMap() {
        return mMap;
    }

    public MapOptions getMapOptions() {
        return mMapOptions;
    }

    public GoogleMapView getMapView() {
        return mMapView;
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initContextMenu();
    }

    private Scene createScene() {
        mMapView = new GoogleMapView(Locale.getDefault().getLanguage(), mOptions.getMapKey());
        mRoot = new BorderPane(mMapView);

        mMapView.addMapInitializedListener(() -> {
            mMapOptions = new MapOptions()
                    .center(mOptions.getMapCenter())
                    .zoom(mOptions.getMapZoom())
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .rotateControl(true)
                    .clickableIcons(false)
                    .streetViewControl(false)
                    .mapTypeControl(false)
                    .fullscreenControl(false)
                    .scaleControl(true)
                    .styleString(MapStyleProvider.getStyle(mOptions.getMapStyle()))
                    .zoomControl(false);

            mZoomSlider = new Slider(0, 22, 1);
            mMapView.getChildren().add(mZoomSlider);
            Mapton.getAppToolBar().setDisable(false);

            initMap();

            Platform.runLater(() -> {
                mMap.setZoom(mOptions.getMapZoom());
                mMap.setCenter(mOptions.getMapCenter());
            });

            mStatusBar = AppStatusPanel.getInstance().getProvider();
            if (mOptions.isMapOnly()) {
                mRoot.setBottom(mStatusBar);
            }

            initListeners();
        });

        return new Scene(mRoot);
    }

    private void exportImage() {
        mZoomSlider.setVisible(false);
        WritableImage image = mMapView.snapshot(new SnapshotParameters(), null);

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
        mZoomSlider.setVisible(true);
        if (SimpleDialog.saveFile(new String[]{"png"})) {
            mDestination = SimpleDialog.getPath();
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", mDestination);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private void initContextMenu() {
        Action setHomeAction = new Action(DictMT.SET_HOME.toString(), (ActionEvent t) -> {
            mOptions.setMapHome(mMap.getCenter());
            mOptions.setMapHomeZoom(mMap.getZoom());
        });

        Action saveImageAction = new Action(mBundle.getString("export_image"), (ActionEvent t) -> {
            exportImage();
        });

        Collection<? extends Action> actions = Arrays.asList(
                BookmarkManager.getInstance().getAddBookmarkAction(),
                setHomeAction,
                ActionUtils.ACTION_SEPARATOR,
                saveImageAction,
                ActionUtils.ACTION_SEPARATOR
        );

        mContextCopyMenu = new Menu(mBundle.getString("copy_location"));
        mContextOpenMenu = new Menu(mBundle.getString("open_location"));
        mContextExtrasMenu = new Menu(mBundle.getString("extras"));
        mContextMenu = ActionUtils.createContextMenu(actions);
        mContextMenu.getItems().addAll(mContextCopyMenu, mContextOpenMenu, mContextExtrasMenu);

        WebView webView = mMapView.getWebview();
        webView.setContextMenuEnabled(false);
        webView.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                mContextMenu.show(webView, e.getScreenX(), e.getScreenY());
            } else {
                mContextMenu.hide();
            }
        });

        Lookup.getDefault().lookupResult(MapContextMenuProvider.class).addLookupListener((LookupEvent ev) -> {
            populateContextProviders();
        });

        populateContextProviders();
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case MaptonOptions.KEY_MAP_STYLE:
                        initMap();
                        break;

                    case MaptonOptions.KEY_MAP_TYPE:
                        mMap.setMapType(mOptions.getMapType());
                        break;

                    default:
                }
            });
        });

        SwingUtilities.invokeLater(() -> {
            addHierarchyListener((HierarchyEvent e) -> {
                if (e.getChangedParent() instanceof JLayeredPane) {
                    Dimension d = ((JFrame) WindowManager.getDefault().getMainWindow()).getContentPane().getPreferredSize();
                    final boolean showOnlyEditor = 1 == d.height && 1 == d.width;
                    mOptions.setMapOnly(showOnlyEditor);
                    Platform.runLater(() -> {
                        if (showOnlyEditor) {
                            mRoot.setBottom(mStatusBar);
                        } else {
                            if (mRoot.getBottom() != null) {
                                mRoot.setBottom(null);
                                AppStatusPanel.getInstance().reset();
                            }
                        }
                    });
                }
            });
        });
    }

    private void initMap() {
        mMapOptions.styleString(MapStyleProvider.getStyle(mOptions.getMapStyle()));
        if (mMap != null) {
            mMapOptions
                    .center(mMap.getCenter())
                    .zoom(mMap.getZoom());
        }

        mMap = mMapView.createMap(mMapOptions);
        mMap.zoomProperty().bindBidirectional(mZoomSlider.valueProperty());

        mMap.addStateEventHandler(MapStateEventType.zoom_changed, () -> {
            mMapController.setZoom(mMap.getZoom());
        });

        mMap.addMouseEventHandler(UIEventType.mousemove, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            mMapController.setLatLong(latLong);
            AppStatusPanel.getInstance().getProvider().updateLatLong();
        });
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
                            NbLog.v("Open location", s);
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
                            NbLog.v("Copy location", s);
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

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        Platform.runLater(() -> {
        });
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        Platform.runLater(() -> {
            try {
                mOptions.setMapCenter(mMap.getCenter());
                mOptions.setMapZoom(mMap.getZoom());
            } catch (Exception e) {
            }
        });
    }
}
