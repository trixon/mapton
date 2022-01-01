/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.base.ui.file_drop_switchboard;

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
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MCoordinateFile;
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
        if (mTabPane.getTabs().stream().anyMatch(tab -> (tab instanceof ExtTab))) {
            return true;
        }

        return false;
    }

    public void openFiles() {
        HashMap<MCoordinateFileOpener, ArrayList<MCoordinateFile>> openerToList = new HashMap<>();
        mTabPane.getTabs().stream().filter(tab -> (tab instanceof ExtTab)).forEachOrdered(tab -> {
            ((ExtTab) tab).getItems().forEach(fileOpener -> {
                var fileOpenerFile = new MCoordinateFile();
                fileOpenerFile.setFile(fileOpener.getFile());
                fileOpenerFile.setCooTrans(fileOpener.getCooTrans());
                openerToList.computeIfAbsent(fileOpener.getCoordinateFileOpener(), k -> new ArrayList<>()).add(fileOpenerFile);
            });
        });

        openerToList.entrySet().forEach(entry -> {
            Mapton.getGlobalState().put(entry.getKey().getClass().getName(), entry.getValue());
        });
    }

    private void createUI() {
        setCenter(mTabPane);

        TreeMap<String, ArrayList<MCoordinateFileOpener>> extToFileOpeners = new TreeMap<>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).forEach(fileOpener -> {
            for (String extension : fileOpener.getExtensions()) {
                extToFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(fileOpener);
            }
        });

        extToFileOpeners.values().forEach(fileOpeners -> {
            fileOpeners.sort((MCoordinateFileOpener o1, MCoordinateFileOpener o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        });

        TreeMap<String, ArrayList<File>> extToFile = new TreeMap<>();

        ListView<File> listView = new ListView<>();
        mFiles.forEach(file -> {
            String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.getDefault());
            if (extToFileOpeners.containsKey(extension)) {
                extToFile.computeIfAbsent(extension, k -> new ArrayList<>()).add(file);
            } else {
                listView.getItems().add(file);
            }
        });

        extToFile.entrySet().stream()
                .map(entry -> new ExtTab(entry.getKey(), entry.getValue(), extToFileOpeners.get(entry.getKey())))
                .forEachOrdered(extTab -> {
                    mTabPane.getTabs().add(extTab);
                });

        if (!listView.getItems().isEmpty()) {
            listView.getItems().sort(File::compareTo);
            Tab tab = new Tab(mBundle.getString("unassociated"), listView);
            tab.setClosable(false);
            mTabPane.getTabs().add(tab);
        }
    }
}
