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
package org.mapton.worldwind.ruler;

import gov.nasa.worldwind.WorldWindow;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.MKmlCreator;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTabPane extends TabPane {

    private File mDestination;
    private SimpleDateFormat mSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private int mTabCounter = 0;
    private WorldWindow mWorldWindow;

    public static RulerTabPane getInstance() {
        return Holder.INSTANCE;
    }

    private RulerTabPane() {
        createUI();
        initListeners();
    }

    public void refresh(WorldWindow worldWindow) {
        mWorldWindow = worldWindow;
        addTab();
    }

    void save() {
        FileChooser.ExtensionFilter mExtKml = new FileChooser.ExtensionFilter("Keyhole Markup Language (*.kml)", "*.kml");

        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(new FileChooser.ExtensionFilter(Dict.ALL_FILES.toString(), "*"));
        SimpleDialog.addFilter(mExtKml);
        SimpleDialog.setFilter(mExtKml);
        SimpleDialog.setTitle(String.format("%s %s", Dict.SAVE.toString(), Dict.Geometry.GEOMETRIES.toString().toLowerCase()));

        String epoch = mSdf.format(new Date());

        SimpleDialog.setSelectedFile(new File(String.format("%s_%s", Dict.Geometry.GEOMETRIES.toString(), epoch)));
        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
        }

        if (SimpleDialog.saveFile(new String[]{"kml"})) {
            mDestination = SimpleDialog.getPath();
            new Thread(() -> {
                try {
                    switch (FilenameUtils.getExtension(mDestination.getName())) {
                        case "kml":
                            new ExporterKml(epoch);
                            break;

                        default:
                            throw new AssertionError();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }).start();

        }
    }

    private void addTab() {
        Platform.runLater(() -> {
            RulerTab rulerTab = new RulerTab(Integer.toString(++mTabCounter), mWorldWindow);

            getTabs().add(rulerTab);
            getSelectionModel().select(rulerTab);
        });
    }

    private void createUI() {
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        getTabs().add(plusTab);
    }

    private void initListeners() {
        getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) -> {
            if (oldTab instanceof RulerTab) {
                ((RulerTab) oldTab).getMeasureTool().setArmed(false);
            }

            if (getSelectionModel().getSelectedIndex() == 0) {
                addTab();
            }
        });
    }

    private static class Holder {

        private static final RulerTabPane INSTANCE = new RulerTabPane();
    }

    private class ExporterKml extends MKmlCreator {

        ExporterKml(String epoch) throws IOException {
            mDocument.setName(String.format("%s_%s", Dict.Geometry.GEOMETRIES.toString(), epoch));

            for (Tab tab : getTabs()) {
                if (tab instanceof RulerTab) {
                    mDocument.addToFeature(((RulerTab) tab).getFeature());
                }
            }

            save(mDestination, true, true);
            SystemHelper.desktopOpen(mDestination);
        }
    }
}
