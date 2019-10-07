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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MEngine;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MOptions2;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
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
    private MEngine mEngine;
    private final HashSet<TopComponent> mMapMagnets = new HashSet<>();
    private final Mapton mMapton = Mapton.getInstance();
    private BorderPane mRoot;

    public MapTopComponent() {
        super();
        setName(Dict.MAP.toString());

        putClientProperty(PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(PROP_UNDOCKING_DISABLED, Boolean.TRUE);
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

        //aaaAppStatusView.getInstance().setWindowMode(StatusWindowMode.OTHER);
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

        //aaaAppStatusView.getInstance().setWindowMode(StatusWindowMode.MAP);
    }

    @Override
    protected void initFX() {
        setScene(createScene());
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

    private Scene createScene() {
        mRoot = new BorderPane();

        return new Scene(mRoot);
    }

}
