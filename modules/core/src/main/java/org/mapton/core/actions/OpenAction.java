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
package org.mapton.core.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.core.api.BaseAction;
import org.mapton.core.ui.BaseToolBar;
import org.mapton.core.ui.FileDropSwitchboard;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.OpenAction"
)
@ActionRegistration(
        displayName = "#CTL_OpenAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 0),
    @ActionReference(path = "Shortcuts", name = "D-O")
})
@NbBundle.Messages("CTL_OpenAction=&Open...")
public final class OpenAction extends BaseAction {

    private final ResourceBundle mBundle;
    private File mFile;

    public OpenAction() {
        mBundle = NbBundle.getBundle(BaseToolBar.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        open();
    }

    private void open() {
        TreeMap<String, ArrayList<MCoordinateFileOpener>> extToCoordinateFileOpeners = new TreeMap<>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).forEach(coordinateFileOpener -> {
            for (String extension : coordinateFileOpener.getExtensions()) {
                extToCoordinateFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(coordinateFileOpener);
            }
        });

        if (extToCoordinateFileOpeners.isEmpty()) {
            NbMessage.warning(Dict.WARNING.toString(), mBundle.getString("no_file_openers"));
        } else {
            ArrayList<FileNameExtensionFilter> fileNameExtensionFilters = new ArrayList<>();
            SimpleDialog.setTitle(Dict.OPEN.toString());
            SimpleDialog.clearFilters();
            extToCoordinateFileOpeners.entrySet().stream().map(entry -> {
                entry.getValue().sort((MCoordinateFileOpener o1, MCoordinateFileOpener o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                return entry;
            }).forEachOrdered(entry -> {
                entry.getValue().forEach(coordinateFileOpener -> {
                    SimpleDialog.addFilter(new FileNameExtensionFilter(String.format("%s (%s)", coordinateFileOpener.getName(), entry.getKey()), entry.getKey()));
                });
            });

            if (mFile == null) {
                SimpleDialog.setPath(FileUtils.getUserDirectory());
            } else {
                SimpleDialog.setPath(mFile.getParentFile());
                SimpleDialog.setSelectedFile(new File(""));
            }

            if (SimpleDialog.openFile(true)) {
                mFile = SimpleDialog.getPaths()[0];
                new FileDropSwitchboard(Arrays.asList(SimpleDialog.getPaths()));
            }
        }
    }
}
