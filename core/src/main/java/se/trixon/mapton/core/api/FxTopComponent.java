/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.trixon.mapton.core.api;

import java.awt.BorderLayout;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.openide.windows.TopComponent;

/**
 *
 * @author Patrik Karlström
 */
public abstract class FxTopComponent extends TopComponent {

    private final JFXPanel mFxPanel = new JFXPanel();
    private Scene mScene;

    public FxTopComponent() {
        setLayout(new BorderLayout());
        add(mFxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            initFX();
            mFxPanel.setScene(mScene);
        });
    }

    public JFXPanel getFxPanel() {
        return mFxPanel;
    }

    public Scene getScene() {
        return mScene;
    }

    public void setScene(Scene scene) {
        mScene = scene;
    }

    /**
     * Runs on the JavaFX Application Thread.
     */
    protected abstract void initFX();
}
