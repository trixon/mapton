/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BMeasurementReport;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.nbp.core.SelectionLockManager;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.butterfly_core.ui//Measurements//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MeasurementsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "mapBottom", openAtStartup = false)
public final class MeasurementsTopComponent extends MTopComponent {

    public MeasurementsTopComponent() {
        setName(SDict.MEASUREMENTS.toString());
        putClientProperty("print.printable", Boolean.TRUE); // NOI18N
        putClientProperty("print.name", "Mapton - %s".formatted(SDict.MEASUREMENTS.toString())); // NOI18N
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    protected void initFX() {
        setScene(new Scene(new MeasurementsView()));
        Mapton.getGlobalState().put(BKey.OBJECT_MEASUREMENTS, Mapton.getGlobalState().get(BKey.OBJECT_MEASUREMENTS));
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

    class MeasurementsView extends BorderPane {

        private LogPanel mLogPanel;
        private MeasurementsPane mMeasurementPane;

        public MeasurementsView() {
            createUI();
            initListeners();
            refresh(null);
        }

        private void createUI() {
            mLogPanel = new LogPanel();
            mMeasurementPane = new MeasurementsPane();
        }

        private void initListeners() {
            Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
                Platform.runLater(() -> {
                    refresh(evt.getValue());
                });
            }, BKey.OBJECT_MEASUREMENTS);
        }

        private void load(String text) {
            mLogPanel.setText(text);
        }

        @SuppressWarnings("unchecked")
        private void refresh(Object o) {
            if (SelectionLockManager.getInstance().isLocked()) {
                return;
            }

            Node centerObject = null;

            if (o == null) {
                centerObject = null;
            } else if (o instanceof Node node) {
                centerObject = node;
            } else if (o instanceof BMeasurementReport measurement) {
                mMeasurementPane.load(measurement);
                centerObject = mMeasurementPane;
            } else if (o instanceof String s) {
                centerObject = mLogPanel;
                load(s);
            } else {
                centerObject = mLogPanel;
                load(ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE));
            }

            setCenter(centerObject);
        }
    }
}
