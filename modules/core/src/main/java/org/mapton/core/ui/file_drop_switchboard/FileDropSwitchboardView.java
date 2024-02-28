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
package org.mapton.core.ui.file_drop_switchboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class FileDropSwitchboardView extends BorderPane {

    private final ResourceBundle mBundle;
    private final List<File> mFiles;
    private final TabPane mTabPane = new TabPane();

    public FileDropSwitchboardView(List<File> files) {
        mFiles = files;
        mBundle = NbBundle.getBundle(FileDropSwitchboardView.class);
        createUI();
    }

    public boolean hasFiles() {
        return mTabPane.getTabs().stream().anyMatch(tab -> (tab instanceof ExtTab));
    }

    public void openFiles() {
        var coordinateFileOpenerToCoordinateFiles = new HashMap<MCoordinateFileOpener, ArrayList<MCoordinateFile>>();
        mTabPane.getTabs().stream()
                .filter(tab -> (tab instanceof ExtTab))
                .map(tab -> (ExtTab) tab)
                .forEachOrdered(tab -> {
                    tab.getItems().forEach(coordinateFileInput -> {
                        var coordinateFileOpener = coordinateFileInput.getCoordinateFileOpener();
                        var coordinateFile = new MCoordinateFile();
                        coordinateFile.setFile(coordinateFileInput.getFile());
                        coordinateFile.setCooTrans(coordinateFileInput.getCooTrans());
                        coordinateFile.setCoordinateFileOpenerName(coordinateFileOpener.getClass().getName());
                        coordinateFileOpenerToCoordinateFiles.computeIfAbsent(coordinateFileOpener, k -> new ArrayList<>()).add(coordinateFile);
                    });
                });

        coordinateFileOpenerToCoordinateFiles.entrySet().forEach(entry -> {
            Mapton.getGlobalState().put(entry.getKey().getClass().getName(), entry.getValue());
        });
    }

    private void createUI() {
        setCenter(mTabPane);

        var extToCoordinateFileOpeners = new TreeMap<String, ArrayList<MCoordinateFileOpener>>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).forEach(coordinateFileOpener -> {
            for (var extension : coordinateFileOpener.getExtensions()) {
                extToCoordinateFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(coordinateFileOpener);
            }
        });

        extToCoordinateFileOpeners.values().forEach(coordinateFileOpener -> {
            coordinateFileOpener.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        });

        var extToFiles = new TreeMap<String, ArrayList<File>>();
        var listView = new ListView<File>();

        mFiles.forEach(file -> {
            var extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.getDefault());
            if (extToCoordinateFileOpeners.containsKey(extension)) {
                extToFiles.computeIfAbsent(extension, k -> new ArrayList<>()).add(file);
            } else {
                listView.getItems().add(file);
            }
        });

        extToFiles.entrySet().stream()
                .map(entry -> new ExtTab(entry.getKey(), entry.getValue(), extToCoordinateFileOpeners.get(entry.getKey())))
                .forEachOrdered(extTab -> {
                    mTabPane.getTabs().add(extTab);
                });

        if (!listView.getItems().isEmpty()) {
            listView.getItems().sort(File::compareTo);
            var tab = new Tab(mBundle.getString("unassociated"), listView);
            tab.setClosable(false);
            mTabPane.getTabs().add(tab);
        }
    }
}
