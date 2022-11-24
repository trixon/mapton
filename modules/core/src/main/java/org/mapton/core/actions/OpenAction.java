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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.api.FileChooserHelper;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.core.api.BaseAction;
import org.mapton.core.api.BaseToolBar;
import org.mapton.core.ui.file_drop_switchboard.FileDropSwitchboard;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.FileHelper;

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

    public OpenAction() {
        mBundle = NbBundle.getBundle(BaseToolBar.class);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        open();
    }

    private void open() {
        var extToCoordinateFileOpeners = new TreeMap<String, ArrayList<MCoordinateFileOpener>>();
        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class)
                .forEach(coordinateFileOpener -> {
                    for (var extension : coordinateFileOpener.getExtensions()) {
                        extToCoordinateFileOpeners.computeIfAbsent(extension.toLowerCase(Locale.getDefault()), k -> new ArrayList<>()).add(coordinateFileOpener);
                    }
                });

        if (extToCoordinateFileOpeners.isEmpty()) {
            NbMessage.warning(Dict.WARNING.toString(), mBundle.getString("no_file_openers"));
        } else {
            var fileNameExtensionFilters = new TreeSet<FileNameExtensionFilter>(Comparator.comparing(FileNameExtensionFilter::getDescription));

            var fileChooser = new FileChooserBuilder(OpenAction.class)
                    .setDefaultWorkingDirectory(FileHelper.getDefaultDirectory())
                    .setFilesOnly(true)
                    .setSelectionApprover(FileChooserHelper.getFileExistOpenSelectionApprover(Almond.getFrame()))
                    .setTitle(Dict.OPEN.toString())
                    .createFileChooser();

            fileChooser.setMultiSelectionEnabled(true);

            extToCoordinateFileOpeners.entrySet().stream()
                    .map(entry -> {
                        entry.getValue().sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                        return entry;
                    }).forEachOrdered(entry -> {
                entry.getValue().forEach(coordinateFileOpener -> {
                    fileNameExtensionFilters.add(new FileNameExtensionFilter("%s (%s)".formatted(coordinateFileOpener.getName(), entry.getKey()), entry.getKey()));
                });
            });

            for (var fileNameExtensionFilter : fileNameExtensionFilters) {
                fileChooser.addChoosableFileFilter(fileNameExtensionFilter);
            }

            if (fileChooser.showOpenDialog(Almond.getFrame()) == JFileChooser.APPROVE_OPTION) {
                new FileDropSwitchboard(Arrays.asList(fileChooser.getSelectedFiles()));
            }
        }
    }
}
