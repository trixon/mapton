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
package org.mapton.base.ui;

import java.awt.Point;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MEngineListener;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.MWhatsHereEngine;
import org.mapton.api.Mapton;
import org.mapton.base.ui.bookmark.BookmarkEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class MapContextMenu {

    private final BookmarkEditor mBookmarkEditor;
    private Menu mContextCopyMenu;
    private Menu mContextExtrasMenu;
    private ContextMenu mContextMenu;
    private Menu mContextOpenMenu;
    private File mExportFile;
    private final MOptions mMOptions = MOptions.getInstance();

    public MapContextMenu() {
        mBookmarkEditor = BookmarkEditor.getDefault();

        initContextMenu();
        initListeners();
    }

    private void copyImage() {
        mContextMenu.hide();
        try {
            SystemHelperFx.copyToClipboard(getEngine().getImageRenderer().call());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void exportImage() {
        SimpleDialog.clearFilters();
        SimpleDialog.addFilters("*", "png");
        SimpleDialog.setFilter("png");

        SimpleDialog.setTitle(getBundleString("export_view"));

        SimpleDialog.setPath(mExportFile == null ? FileUtils.getUserDirectory() : mExportFile.getParentFile());
        SimpleDialog.setSelectedFile(new File(new SimpleDateFormat("'Mapton_'yyyyMMdd_HHmmss").format(new Date())));

        mContextMenu.hide();

        FxHelper.runLaterDelayed(10, () -> {
            if (SimpleDialog.saveFile(new String[]{"png"})) {
                mExportFile = SimpleDialog.getPath();
                try {
                    ImageIO.write(getEngine().getImageRenderer().call(), "png", mExportFile);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private String getBundleString(String key) {
        return NbBundle.getMessage(getClass(), key);
    }

    private MEngine getEngine() {
        return Mapton.getEngine();
    }

    private void initContextMenu() {
        Action setHomeAction = new Action(MDict.SET_HOME.toString(), (ActionEvent t) -> {
            mMOptions.setMapHome(getEngine().getCenter());
            mMOptions.setMapHomeZoom(getEngine().getZoom());
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
                mBookmarkEditor.getAddBookmarkAction(),
                ActionUtils.ACTION_SEPARATOR,
                copyImageAction,
                exportImageAction,
                ActionUtils.ACTION_SEPARATOR,
                ActionUtils.ACTION_SEPARATOR,
                setHomeAction
        );

        mContextCopyMenu = new Menu(getBundleString("copy_location"));
        mContextOpenMenu = new Menu(getBundleString("open_location"));
        mContextExtrasMenu = new Menu(getBundleString("extras"));
        mContextMenu = ActionUtils.createContextMenu(actions);

        int insertPos = mContextMenu.getItems().size() - 2;
        mContextMenu.getItems().add(insertPos, mContextExtrasMenu);
        mContextMenu.getItems().add(insertPos, mContextOpenMenu);
        mContextMenu.getItems().add(insertPos, mContextCopyMenu);
        mContextMenu.setOnShowing(event -> {
            copyImageAction.setDisabled(getEngine().getImageRenderer() == null);
            exportImageAction.setDisabled(getEngine().getImageRenderer() == null);
        });

        Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener((LookupEvent ev) -> {
            populateContextProviders();
        });

        populateContextProviders();
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            populateContextProviders();
        }, MKey.MAP_POPULATE_CONTEXT_MENY);

        MEngine.addEngineListener(new MEngineListener() {
            @Override
            public void displayContextMenu(Point screenXY) {
                Node rootNode = FxOnScreenDummy.getInstance();
                rootNode.getScene().getWindow().requestFocus();
                rootNode.requestFocus();

                mContextMenu.show(rootNode, screenXY.x, screenXY.y);
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
                        item.setOnAction(event -> {
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
                        item.setOnAction(event -> {
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
                int zoom = (int) (5 + getEngine().getZoom() * 18);
                String s = whatsHereEngine.getResult(getEngine().getLatLonMouse(), zoom);
                if (StringUtils.isNotBlank(s)) {
                    Mapton.notification(MKey.NOTIFICATION_FX_INFORMATION, getBundleString("whats_here"), s);
                    Mapton.execute(() -> {
                        getEngine().onWhatsHere(s);
                    });
                }

                Mapton.getGlobalState().put(MEngine.KEY_STATUS_PROGRESS, 1d);
            } else {
                //TODO err inf dialog
            }
        }).start();
    }
}
