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
package org.mapton.api;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.filesystems.FileChooserBuilder.SelectionApprover;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class FileChooserHelper {

    private static final HashMap<String, FileNameExtensionFilter> sExtensionFilters = new HashMap<>();

    public static File getFileWithProperExt(JFileChooser fileChooser) {
        var file = fileChooser.getSelectedFile();
        var fileNameExtensionFilter = (FileNameExtensionFilter) fileChooser.getFileFilter();

        if (fileNameExtensionFilter.accept(file)) {
            return file;
        } else {
            var extensions = fileNameExtensionFilter.getExtensions();
            String suffix;

            if (file.getName().endsWith(".")) {
                suffix = extensions[0];
            } else {
                suffix = "." + extensions[0];
            }

            file = new File(file.getAbsolutePath() + suffix);
            return file;
        }
    }

    public static HashMap<String, FileNameExtensionFilter> getExtensionFilters() {
        return sExtensionFilters;
    }

    public static SelectionApprover getFileExistOpenSelectionApprover(Component parentComponent) {
        SelectionApprover selectionApprover = (File[] selection) -> {
            return selection != null
                    && selection.length > 0
                    && selection[0].isFile();
        };

        return selectionApprover;
    }

    public static SelectionApprover getFileExistSelectionApprover(Component parentComponent) {
        SelectionApprover selectionApprover = (File[] selection) -> {
            var file = selection[0];
            if (file.exists()) {
                var result = JOptionPane.showConfirmDialog(parentComponent,
                        Dict.Dialog.MESSAGE_FILE_EXISTS.toString().formatted(file.getAbsolutePath()),
                        Dict.Dialog.TITLE_FILE_EXISTS.toString(),
                        JOptionPane.YES_NO_OPTION);

                return result != JOptionPane.NO_OPTION;
            } else {
                return true;
            }
        };

        return selectionApprover;
    }
}
