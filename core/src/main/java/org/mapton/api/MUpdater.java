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
package org.mapton.api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.mapton.core.updater.UpdaterTool;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.NbPrint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MUpdater extends MBaseMaskerPane {

    public static abstract class ByFile extends MUpdater {

        protected NbPrint mPrint = new NbPrint(NbBundle.getMessage(UpdaterTool.class, "updater_tool"));

        private BorderPane mBorderPane;
        private Button mButton;
        private File mFile;
        private Label mLabel;
        private Runnable mRunnable;
        private String mTooltipText;

        public ByFile() {
        }

        public abstract Long getAgeLimit();

        public File getFile() {
            return mFile;
        }

        @Override
        public Node getNode() {
            if (mBorderPane == null) {
                createUI();
            }

            return mBody;
        }

        @Override
        public void onSelect() {
            updateStatus();
        }

        public void setFile(File file) {
            mFile = file;
        }

        public void setRunnable(Runnable runnable) {
            mRunnable = runnable;
        }

        public void setTooltipText(String tooltipText) {
            mTooltipText = tooltipText;
        }

        private void createUI() {
            mButton = new Button(Dict.UPDATE.toString());
            if (mTooltipText != null) {
                mButton.setTooltip(new Tooltip(mTooltipText));
            }

            mButton.setOnAction((event) -> {
                mMaskerPane.setVisible(true);

                new Thread(() -> {
                    mRunnable.run();
                    updateStatus();
                    notify(Dict.OPERATION_COMPLETED.toString());
                }).start();
            });

            mLabel = new Label();

            VBox box = new VBox(FxHelper.getUIScaled(8), mButton, mLabel);
            box.setAlignment(Pos.CENTER);
            mBorderPane = new BorderPane(box);
            mNotificationPane.setContent(mBorderPane);
        }

        private void updateStatus() {
            Platform.runLater(() -> {
                String lastUpdate = "-";
                if (mFile != null && mFile.isFile()) {
                    lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date(mFile.lastModified()));
                }
                mLabel.setText(String.format(Dict.UPDATED_S.toString(), lastUpdate));
            });
        }
    }
}
