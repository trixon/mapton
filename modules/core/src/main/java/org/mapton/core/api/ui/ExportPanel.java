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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.tools.Borders;
import org.mapton.api.FileChooserHelper;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCrsManager;
import org.mapton.api.MDict;
import org.mapton.api.MNotificationIcons;
import org.mapton.api.MOptions;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ExportPanel extends FxDialogPanel {

    private final ResourceBundle mBundle = NbBundle.getBundle(ExportPanel.class);
    private ExportView mExportView;
    private final Object mLookupKey;
    private final Preferences mPreferences = NbPreferences.forModule(ExportPanel.class);

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

        private final BorderPane mBorderPane = new BorderPane();
        private final ComboBox<Charset> mCharsetComboBox = new ComboBox<>();
        private final ComboBox<MCooTrans> mCooTransComboBox = new ComboBox<>();
        private final MCrsManager mCrsManager = MCrsManager.getInstance();
        private final ComboBox<ExportProvider> mExporterComboBox = new ComboBox<>();
        private final CheckBox mOpenCheckBox = new CheckBox(mBundle.getString("autoOpen"));

        public ExportView() {
            createUI();
            initListeners();
        }

        public void load() {
            mExporterComboBox.getItems().setAll(getExportProviders());
            var first = getExportProviders().getFirst();
            var exporter = getExportProviders().stream()
                    .filter(e -> StringUtils.equals(e.getName(), mPreferences.get("activeExporter", "")))
                    .findFirst().orElse(null);
            mExporterComboBox.getSelectionModel().select(exporter != null ? exporter : first);
        }

        private void createUI() {
            mCharsetComboBox.getItems().setAll(Charset.availableCharsets().values());
            mCharsetComboBox.getSelectionModel().select(StandardCharsets.UTF_8);
            mCooTransComboBox.getItems().setAll(mCrsManager.getItems());
            mCooTransComboBox.getSelectionModel().select(MOptions.getInstance().getMapCooTrans());

            var exporterLabel = new Label(Dict.FORMAT.toString());
            var charsetLabel = new Label(Dict.ENCODING.toString());
            var cootransLabel = new Label(MDict.COORDINATE_SYSTEM.toString());
            var wrappedHolder = Borders.wrap(mBorderPane)
                    .etchedBorder()
                    .outerPadding(FxHelper.getUIScaled(18), 0, FxHelper.getUIScaled(18), 0)
                    .buildAll();
            var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(8));
            gp.addRow(0, exporterLabel, cootransLabel, charsetLabel);
            gp.addRow(1, mExporterComboBox, mCooTransComboBox, mCharsetComboBox);
            FxHelper.autoSizeRegionHorizontal(mExporterComboBox, mCooTransComboBox, mCharsetComboBox);
            FxHelper.autoSizeColumn(gp, 3);
//            GridPane.setHgrow(mCooTransComboBox, Priority.SOMETIMES);
//            GridPane.setHgrow(mCharsetComboBox, Priority.SOMETIMES);

            setPadding(FxHelper.getUIScaledInsets(8));
            setTop(gp);
            setCenter(wrappedHolder);
            setBottom(mOpenCheckBox);
        }

        private void export() {
            var exporter = mExporterComboBox.getSelectionModel().getSelectedItem();

            if (exporter != null) {
                var exportFile = getFile();
                if (exportFile != null) {
                    var thread = new Thread(() -> {
                        var exportConfiguration = new ExportConfiguration();
                        exportConfiguration.setFile(exportFile);
                        exportConfiguration.setCharset(mCharsetComboBox.getValue());
                        exportConfiguration.setCooTrans(mCooTransComboBox.getValue());

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

                        if (mOpenCheckBox.isSelected()) {
                            SystemHelper.desktopOpenOrElseParent(exportFile);
                        }
                    }, "Exporter");

                    thread.start();
                }
            }
        }

        private File getFile() {
            var exporter = mExporterComboBox.getValue();
            var filter = exporter.getExtensionFilter();

            var file = new FileChooserBuilder(mLookupKey.toString() + exporter.getName())
                    .addFileFilter(filter)
                    .setFileFilter(filter)
                    .setSelectionApprover(FileChooserHelper.getFileExistSelectionApprover(Almond.getFrame()))
                    .setTitle(Dict.EXPORT.toString()).showSaveDialog();

            if (file == null) {
                return file;
            } else if (!StringUtils.equalsIgnoreCase(FilenameUtils.getExtension(file.getName()), filter.getExtensions()[0])) {
                var s = "%s.%s".formatted(file.getAbsolutePath(), filter.getExtensions()[0]);
                return new File(s);
            }

            return file;
        }

        private String getPrefsKey() {
            return "activeExporter_" + mLookupKey.toString();
        }

        private void initListeners() {
            mExporterComboBox.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
                mPreferences.put(getPrefsKey(), n.getName());
                loadExporter(n);
            });
        }

        private void loadExporter(ExportProvider exporter) {
            mCharsetComboBox.setDisable(!exporter.isSupportsEncoding());
            mCooTransComboBox.setDisable(!exporter.isSupportsTransformation());

            mCharsetComboBox.setValue(exporter.getCharset());
            mCooTransComboBox.setValue(exporter.getCooTrans());

            mBorderPane.setCenter(exporter.getNode());
        }
    }
}
