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
package org.mapton.workbench.modules.map;

import java.awt.Point;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MEngineListener;
import org.mapton.api.MOptions;
import org.mapton.api.MOptions2;
import org.mapton.api.MWhatsHereEngine;
import org.mapton.api.Mapton;
import org.mapton.workbench.bookmark.BookmarkEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;
import se.trixon.windowsystemfx.Window;
import se.trixon.windowsystemfx.WindowSystemComponent;

/**
 *
 * @author Patrik Karlström
 */
@WindowSystemComponent.Description(
        iconBase = "",
        preferredId = "org.mapton.workbench.modules.map.MapWindow",
        parentId = "editor",
        position = 1
)
@ServiceProvider(service = Window.class)
public class MapWindow extends Window {

    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private File mDestination;
    private MEngine mEngine;
    private final MOptions mMOptions = MOptions.getInstance();
    private StackPane mNode;

    public MapWindow() {
        mEngine = Mapton.getEngine();
    }

    @Override
    public Node getNode() {
        if ((mNode == null)) {
            createUI();
            initContextMenu();
            initListeners();
        }

        return mNode;
    }

    private void copyImage() {
        mContextMenu.hide();
        try {
            SystemHelper.copyToClipboard(mEngine.getImageRenderer().call());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void createUI() {
        mNode = new StackPane(
                Mapton.getEngine().getUI(),
                crosshairSegment(Side.TOP),
                crosshairSegment(Side.RIGHT),
                crosshairSegment(Side.BOTTOM),
                crosshairSegment(Side.LEFT)
        );
    }

    private Node crosshairSegment(Side side) {
        final var gap = FxHelper.getUIScaled(6);
        final var length = FxHelper.getUIScaled(6) + gap;
        final var pad = length * 1.8;
        final var h = length / 4;

        Rectangle r = new Rectangle();
        r.visibleProperty().bind(MOptions2.getInstance().general().displayCrosshairProperty());
        r.setDisable(true);

        if (side == Side.BOTTOM || side == Side.TOP) {
            r.setWidth(h);
            r.setHeight(length);
        } else {
            r.setWidth(length);
            r.setHeight(h);
        }

        r.setStroke(Color.BLACK);
        r.setFill(Color.WHITE);
        r.setStrokeWidth(1.0);
        r.setStrokeLineCap(StrokeLineCap.BUTT);

        switch (side) {
            case TOP:
                StackPane.setMargin(r, new Insets(pad, 0, 0, 0));
                break;
            case RIGHT:
                StackPane.setMargin(r, new Insets(0, pad, 0, 0));
                break;
            case BOTTOM:
                StackPane.setMargin(r, new Insets(0, 0, pad, 0));
                break;
            case LEFT:
                StackPane.setMargin(r, new Insets(0, 0, 0, pad));
                break;
        }

        return r;
    }

    private void exportImage() {
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Image (*.png)", "*.png");
        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(new FileChooser.ExtensionFilter(Dict.ALL_FILES.toString(), "*"));
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        SimpleDialog.setOwner(mNode.getScene().getWindow());
        SimpleDialog.setTitle(getBundleString("export_view"));

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

    private String getBundleString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

    private void initContextMenu() {
        Action setHomeAction = new Action(MDict.SET_HOME.toString(), (ActionEvent t) -> {
            mMOptions.setMapHome(mEngine.getCenter());
            mMOptions.setMapHomeZoom(mEngine.getZoom());
        });

        Action whatsHereAction = new Action(getBundleString("whats_here"), (ActionEvent t) -> {
            whatsHere();
        });

        Action copyImageAction = new Action(getBundleString("copy_image"), (ActionEvent t) -> {
            copyImage();
        });
        copyImageAction.setDisabled(true);

        Action exportImageAction = new Action(getBundleString("export_image"), (ActionEvent t) -> {
            exportImage();
        });
        exportImageAction.setDisabled(true);

        Collection<? extends Action> actions = Arrays.asList(
                whatsHereAction,
                BookmarkEditor.getInstance().getAddBookmarkAction(),
                ActionUtils.ACTION_SEPARATOR,
                copyImageAction,
                exportImageAction,
                ActionUtils.ACTION_SEPARATOR,
                ActionUtils.ACTION_SEPARATOR,
                setHomeAction
        );

        mContextCopyMenu = new Menu(MDict.COPY_LOCATION.toString());
        mContextOpenMenu = new Menu(MDict.OPEN_LOCATION.toString());
        mContextExtrasMenu = new Menu(getBundleString("extras"));
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
        final ObjectProperty<String> engineProperty = MOptions2.getInstance().general().engineProperty();
        engineProperty.addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            mNode.getChildren().setAll(Mapton.getEngine().getUI());
        });

        engineProperty.addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            mEngine = Mapton.getEngine();
        });

        MEngine.addEngineListener(new MEngineListener() {
            @Override
            public void displayContextMenu(Point screenXY) {
                mContextMenu.show(mNode, screenXY.x, screenXY.y);
            }

            @Override
            public void hideContextMenu() {
                if (mContextMenu.isShowing()) {
                    mContextMenu.hide();
                }
            }
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
                            Mapton.getLog().v("Copy location", s);
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
                                Mapton.getLog().v("Open location", s);
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

    private void whatsHere() {
        Mapton.getGlobalState().put(MEngine.KEY_STATUS_PROGRESS, -1d);

        new Thread(() -> {
            ArrayList< MWhatsHereEngine> engines = new ArrayList<>(Lookup.getDefault().lookupAll(MWhatsHereEngine.class));

            if (!engines.isEmpty()) {
                MWhatsHereEngine whatsHereEngine = engines.get(0);
                int zoom = (int) (5 + mEngine.getZoom() * 18);
                String s = whatsHereEngine.getResult(mEngine.getLatLonMouse(), zoom);
                if (StringUtils.isNotBlank(s)) {
                    Mapton.getLog().i(MapWindow.class, "WhatsHere: " + s);
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
