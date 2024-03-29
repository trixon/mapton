/*
 * Copyright 2023 Patrik Karlström.
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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.FileChooserHelper;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MEngineListener;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.MWhatsHereEngine;
import org.mapton.api.Mapton;
import org.mapton.core.api.AutoOpener;
import org.mapton.core.api.BookmarkEditor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Scaler;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.FileHelper;

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
    private final MOptions mMOptions = MOptions.getInstance();

    public MapContextMenu() {
        mBookmarkEditor = new BookmarkEditor();

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
        var pngFileNameExtensionFilter = FileChooserHelper.getExtensionFilters().get("png");
        var pdfFileNameExtensionFilter = FileChooserHelper.getExtensionFilters().get("pdf");
        var fileChooser = new FileChooserBuilder(MapContextMenu.class)
                .addFileFilter(pngFileNameExtensionFilter)
                .addFileFilter(pdfFileNameExtensionFilter)
                .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                .setFileFilter(pngFileNameExtensionFilter)
                .setFilesOnly(true)
                .setSelectionApprover(FileChooserHelper.getFileExistSelectionApprover(Almond.getFrame()))
                .setTitle(getBundleString("export_view"))
                .createFileChooser();

        var templateFile = new File(FastDateFormat.getInstance("'Mapton_'yyyyMMdd_HHmmss").format(new Date()));

        fileChooser.setSelectedFile(templateFile);

        mContextMenu.hide();

        FxHelper.runLaterDelayed(10, () -> {
            if (fileChooser.showSaveDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
                new Thread(() -> {
                    var file = FileChooserHelper.getFileWithProperExt(fileChooser);

                    try {
                        var bufferedImage = getEngine().getImageRenderer().call();
                        if (FilenameUtils.isExtension(file.getName(), "png")) {
                            ImageIO.write(bufferedImage, "png", file);
                        } else {
                            var doc = new PDDocument();
                            var rectangle = PDRectangle.A4;
                            var page = new PDPage(new PDRectangle(rectangle.getHeight(), rectangle.getWidth()));

                            doc.addPage(page);
                            var pdImage = LosslessFactory.createFromImage(doc, bufferedImage);
                            try (var pageContentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true)) {
                                var scaler = new Scaler(new Dimension(pdImage.getWidth(), pdImage.getHeight()));
                                int pageHeight = (int) rectangle.getWidth();
                                int pageWidth = (int) rectangle.getHeight();
                                scaler.setHeight(pageHeight);
                                scaler.setWidth(pageWidth);
                                var dim = scaler.getDimension();
                                float width = (float) dim.getWidth();
                                int height = dim.height;
                                var x = (pageWidth - width) / 2;
                                var y = (pageHeight - height) / 2;
                                pageContentStream.drawImage(pdImage, x, y, width, height);
                            }
                            doc.save(file);
                            doc.close();
                        }

                        AutoOpener.openIfActivated(file);

                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }, getClass().getCanonicalName()).start();
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
        var setHomeAction = new Action(MDict.SET_HOME.toString(), (ActionEvent t) -> {
            mMOptions.setMapHome(getEngine().getCenter());
            mMOptions.setMapHomeZoom(getEngine().getZoom());
        });

        var whatsHereAction = new Action(getBundleString("whats_here"), (ActionEvent t) -> {
            whatsHere();
        });

        var copyImageAction = new Action(getBundleString("copy_image"), (ActionEvent t) -> {
            copyImage();
        });
        copyImageAction.setDisabled(true);

        var exportImageAction = new Action(getBundleString("export_image"), (ActionEvent t) -> {
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

        Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener(lookupEvent -> {
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
                var rootNode = FxOnScreenDummy.getInstance();
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

            var contextMenues = new ArrayList<MContextMenuItem>(Lookup.getDefault().lookupAll(MContextMenuItem.class));
            contextMenues.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));

            for (var provider : contextMenues) {
                var menuItem = new MenuItem(provider.getName());
                switch (provider.getType()) {
                    case COPY -> {
                        mContextCopyMenu.getItems().add(menuItem);
                        menuItem.setOnAction(event -> {
                            String s = provider.getUrl();
                            Mapton.getLog().v("Copy location", s);
                            SystemHelper.copyToClipboard(s);
                        });
                    }

                    case EXTRAS -> {
                        mContextExtrasMenu.getItems().add(menuItem);
                        menuItem.setOnAction(provider.getAction());
                    }

                    case OPEN -> {
                        mContextOpenMenu.getItems().add(menuItem);
                        menuItem.setOnAction(event -> {
                            String s = provider.getUrl();
                            if (!StringUtils.isBlank(s)) {
                                Mapton.getLog().v("Open location", s);
                                SystemHelper.desktopBrowse(s);
                            }
                        });
                    }

                    default ->
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
            var engines = new ArrayList< MWhatsHereEngine>(Lookup.getDefault().lookupAll(MWhatsHereEngine.class));

            if (!engines.isEmpty()) {
                var whatsHereEngine = engines.get(0);
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
        }, getClass().getCanonicalName()).start();
    }
}
