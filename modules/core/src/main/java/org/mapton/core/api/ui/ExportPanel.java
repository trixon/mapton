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
package org.mapton.core.api.ui;

import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javax.swing.JFileChooser;
import org.mapton.api.MNotificationIcons;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.FileChooserPaneSwingFx;

/**
 *
 * @author Patrik Karlström
 */
public class ExportPanel extends FxDialogPanel {

    private ExportView mExportView;
    private final Object mLookupKey;

    public ExportPanel(Object lookupKey) {
        mLookupKey = lookupKey;
    }

    public void export() {
        mExportView.export();
    }

    public List<? extends ExportProvider> getExportProviders() {
        return Lookup.getDefault().lookupAll(ExportProvider.class).stream()
                .filter(e -> e.getLookupKey() == mLookupKey)
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .toList();
    }

    public void load() {
        mExportView.load();
    }

    @Override
    protected void fxConstructor() {
        mExportView = new ExportView();
        setScene(new Scene(mExportView));
    }

    class ExportView extends BorderPane {

        private final ComboBox<String> mEncodingComboBox = new ComboBox<>();
        private final ComboBox<ExportProvider> mExporterComboBox = new ComboBox<>();
        private final FileChooserPaneSwingFx mFileChooserPane;
        private final CheckBox mOpenCheckBox = new CheckBox("Öppna den exporterade filen");
        private final ComboBox<String> mTransformationComboBox = new ComboBox<>();

        public ExportView() {
            mFileChooserPane = new FileChooserPaneSwingFx(Dict.SAVE.toString(), Dict.DESTINATION.toString(), Almond.getFrame(), JFileChooser.FILES_AND_DIRECTORIES);

            createUI();
            initListeners();
        }

        public void load() {
            mExporterComboBox.getItems().setAll(getExportProviders());
            mExporterComboBox.getSelectionModel().selectFirst();
        }

        private void createUI() {
            var topBox = new VBox();
            topBox.getChildren().addAll(
                    new Label("Format"),
                    mExporterComboBox,
                    new Label("Encoding"),
                    mEncodingComboBox,
                    new Label("Transformation"),
                    mTransformationComboBox,
                    mOpenCheckBox
            );

            setTop(topBox);
            setBottom(mFileChooserPane);
        }

        private void export() {
            var exporter = mExporterComboBox.getSelectionModel().getSelectedItem();
            if (exporter != null) {
                var thread = new Thread(() -> {
                    var exportConfiguration = new ExportConfiguration();
                    exportConfiguration.setFile(mFileChooserPane.getPath());

                    var progressHandle = ProgressHandleFactory.createUIHandle(exporter.getName(), null, null);
                    progressHandle.start();

                    exporter.export(exportConfiguration);

                    progressHandle.finish();
                    NotificationDisplayer.getDefault().notify(
                            Dict.OPERATION_COMPLETED.toString(),
                            MNotificationIcons.getInformationIcon(),
                            exporter.getName(),
                            null,
                            NotificationDisplayer.Priority.LOW
                    );
                }, "Exporter");

                thread.start();
            }
        }

        private void initListeners() {
            mExporterComboBox.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
                var fileChooser = mFileChooserPane.getFileChooserSwing();
                fileChooser.resetChoosableFileFilters();
                var extensionFilter = n.getExtensionFilter();
                fileChooser.addChoosableFileFilter(extensionFilter);
                fileChooser.setFileFilter(extensionFilter);

                mEncodingComboBox.setDisable(!n.isSupportsEncoding());
                mTransformationComboBox.setDisable(!n.isSupportsTransformation());

                setCenter(n.getNode());
            });
        }
    }
}
