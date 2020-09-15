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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.mapton.base.ui.MapContextMenu;
import org.mapton.base.ui.grid.LocalGridsView;
import org.mapton.core_nb.api.MTopComponent;
import org.mapton.core_nb.ui.grid.LocalGridEditor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.DelayedResetRunner;
import se.trixon.almond.util.swing.SwingHelper;

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
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MapAction",
        preferredID = "MapTopComponent"
)
@ActionID(category = "Window", id = "org.mapton.demo.MapTopComponent")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "D-1"),
    @ActionReference(path = "Menu/Window", position = 0)
})
@Messages({
    "CTL_MapAction=&Map"
})
public final class MapTopComponent extends MTopComponent {

    private MEngine mEngine;
    private boolean mMapInitialized = false;
    private ProgressHandle mProgressHandle;
    private JPanel mProgressPanel;
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.name", String.format("Mapton - %s", Dict.MAP.toString())); // NOI18N

        var map = se.trixon.almond.util.swing.dialogs.SimpleDialog.getExtensionFilters();
        map.put("*", new FileNameExtensionFilter(Dict.ALL_FILES.toString(), "*"));
        map.put("csv", new FileNameExtensionFilter("Comma-separated value (*.csv)", "csv"));
        map.put("geo", new FileNameExtensionFilter("SBG Geo (*.geo)", "geo"));
        map.put("json", new FileNameExtensionFilter("JSON (*.json)", "json"));
        map.put("kml", new FileNameExtensionFilter("Keyhole Markup Language (*.kml)", "kml"));
        map.put("kmz", new FileNameExtensionFilter("Keyhole Markup Language (*.kmz)", "kmz"));
        map.put("grid", new FileNameExtensionFilter("Mapton Grid (*.grid)", "grid"));
        map.put("png", new FileNameExtensionFilter(String.format("%s (*.png)", Dict.IMAGE.toString()), "png"));
        map.put("txt", new FileNameExtensionFilter(String.format("%s (*.txt)", Dict.TEXT.toString()), "txt"));
    }

    @Override
    public void paint(Graphics g) {
        try {
            super.paint(g);

            if (mMOptions.isDisplayCrosshair()) {
                Graphics2D g2 = (Graphics2D) g;
                int x = getWidth() / 2;
                int y = (getHeight() + MapToolBarPanel.getInstance().getToolBarPanel().getHeight()) / 2;
                final int gap = SwingHelper.getUIScaled(6);
                final int length = SwingHelper.getUIScaled(6) + gap;

                Stroke[] strokes = {new BasicStroke(SwingHelper.getUIScaled(5)), new BasicStroke(SwingHelper.getUIScaled(2))};
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
        WindowManager.getDefault().findTopComponentGroup("MapGroup").close();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        WindowManager.getDefault().findTopComponentGroup("MapGroup").open();
    }

    @Override
    protected void fxPostConstructor() {
        super.fxPostConstructor();

        SwingHelper.runLater(() -> {
            removeAll();
            JLabel label = new JLabel(String.format("<html>%s<br/><br/><br/></html>", Dict.PATIENCE_IS_A_VIRTUE.toString()));
            label.setVerticalAlignment(SwingConstants.BOTTOM);
            label.setFont(label.getFont().deriveFont(label.getFont().getSize() * 2f));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
        });

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                MOptions.getInstance().engineProperty().addListener((ov, t, t1) -> {
                    setEngine(Mapton.getEngine());
                });

                setEngine(Mapton.getEngine());
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        new MapContextMenu();
        mMOptions.displayCrosshairProperty().addListener((ov, t, t1) -> {
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

    private Scene createScene() {
        mRoot = new BorderPane();

        initListeners();
        LocalGridsView.setLocalGridEditor(LocalGridEditor.getInstance());

        return new Scene(mRoot);
    }

    private void initListeners() {
        setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    final List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    SwingHelper.runLaterDelayed(2, () -> {
                        new FileDropSwitchboard(files);
                    });
                } catch (UnsupportedFlavorException | IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        mRoot.setOnDragOver(dragEvent -> {
            Dragboard board = dragEvent.getDragboard();
            if (board.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        mRoot.setOnDragDropped(dragEvent -> {
            new FileDropSwitchboard(dragEvent.getDragboard().getFiles());
        });

        DelayedResetRunner delayedResetRunner = new DelayedResetRunner(10, () -> {
            final JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();

            if (SystemUtils.IS_OS_WINDOWS) {
                try {
                    final JRootPane rootPane = getRootPane();
                    final Dimension originalSize = rootPane.getSize();

                    SwingUtilities.invokeLater(() -> {
                        rootPane.setSize(new Dimension(originalSize.width - 1, originalSize.height - 0));
                        SwingUtilities.invokeLater(() -> {
                            rootPane.setSize(originalSize);
                        });
                    });
                } catch (Exception e) {
                    //nvm
                }
            }
        });

        SwingUtilities.invokeLater(() -> {
            addHierarchyListener((HierarchyEvent hierarchyEvent) -> {
                if (hierarchyEvent.getChangedParent() instanceof JLayeredPane) {
                    delayedResetRunner.reset();
                }
            });
        });

        Mapton.getGlobalState().addListener(gsce -> {
            double state = gsce.getValue();
            if (-1.0 == state) {
                mProgressHandle = ProgressHandleFactory.createSystemHandle(Dict.CACHING.toString());
                mProgressHandle.start();
                mProgressHandle.switchToIndeterminate();
            } else {
                mProgressHandle.finish();
            }
        }, MEngine.KEY_STATUS_PROGRESS);
    }

    private synchronized void markMapAsInitialized() {
        if (!mMapInitialized) {
            mMapInitialized = true;
            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Mapton.getExecutionFlow().setReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED);
            }).start();
        }
    }

    private void setEngine(MEngine engine) {
        mEngine = engine;
        SwingUtilities.invokeLater(() -> {
            setToolTipText(String.format("%s: %s", MDict.MAP_ENGINE.toString(), engine.getName()));
        });

        if (engine.isSwing()) {
            if (mProgressPanel == null) {
                mProgressPanel = new JPanel(new BorderLayout());
                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                mProgressPanel.add(progressBar, BorderLayout.NORTH);
            }

            SwingUtilities.invokeLater(() -> {
                removeAll();
                add(MapToolBarPanel.getInstance().getToolBarPanel(), BorderLayout.NORTH);
                add(mProgressPanel, BorderLayout.CENTER);

                new Thread(() -> {
                    JComponent engineUI = engine.getMapComponent();
                    engineUI.setMinimumSize(new Dimension(1, 1));
                    engineUI.setPreferredSize(new Dimension(1, 1));

                    SwingHelper.runLaterDelayed(0, () -> {
                        remove(mProgressPanel);
                        add(engineUI, BorderLayout.CENTER);
                        revalidate();
                        repaint();

                        try {
                            engine.onActivate();
                            engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                        } catch (NullPointerException e) {
                        }

                        markMapAsInitialized();
                    });
                }).start();

                revalidate();
                repaint();
            });
        } else {
            Platform.runLater(() -> {
                resetFx();
                add(MapToolBarPanel.getInstance().getToolBarPanel(), BorderLayout.NORTH);
                mRoot.setCenter(engine.getMapNode());
                try {
                    engine.onActivate();
                    engine.panTo(mMOptions.getMapCenter(), mMOptions.getMapZoom());
                } catch (Exception e) {
                }
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();

                    markMapAsInitialized();
                });
            });
        }

        Mapton.logLoading("Map Engine", engine.getName());
    }
}
