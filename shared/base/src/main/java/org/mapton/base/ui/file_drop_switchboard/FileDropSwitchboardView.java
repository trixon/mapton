/*
 * Copyright 2020 Patrik Karlström.
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
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.MFileOpener;
import org.mapton.api.MFileOpenerFile;
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
        HashMap<MFileOpener, ArrayList<MFileOpenerFile>> openerToList = new HashMap<>();
        mTabPane.getTabs().stream().filter(tab -> (tab instanceof ExtTab)).forEachOrdered(tab -> {
            ((ExtTab) tab).getItems().forEach(fileOpenerInput -> {
                var fileOpenerResult = new MFileOpenerFile();
                fileOpenerResult.setFile(fileOpenerInput.getFile());
                fileOpenerResult.setCooTrans(fileOpenerInput.getCooTrans());
                openerToList.computeIfAbsent(fileOpenerInput.getFileOpener(), k -> new ArrayList<>()).add(fileOpenerResult);
            });
        });

        openerToList.entrySet().forEach(entry -> {
            entry.getKey().open(entry.getValue());
        });
    }

    private void createUI() {
        setCenter(mTabPane);

        TreeMap<String, ArrayList<MFileOpener>> extToFileOpeners = new TreeMap<>();
        Lookup.getDefault().lookupAll(MFileOpener.class).forEach(fileOpener -> {
            for (String extension : fileOpener.getExtensions()) {
                extToFileOpeners.computeIfAbsent(extension, k -> new ArrayList<>()).add(fileOpener);
            }
        });

        extToFileOpeners.values().forEach(fileOpeners -> {
            fileOpeners.sort((MFileOpener o1, MFileOpener o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        });

        TreeMap<String, ArrayList<File>> extToFile = new TreeMap<>();

        ListView<File> listView = new ListView<>();
        mFiles.forEach(file -> {
            String extension = FilenameUtils.getExtension(file.getName());
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
