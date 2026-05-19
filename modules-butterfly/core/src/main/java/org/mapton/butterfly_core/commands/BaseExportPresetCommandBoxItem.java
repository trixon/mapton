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
package org.mapton.butterfly_core.commands;

import java.io.File;
import java.util.ArrayList;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.mapton.butterfly_core.api.BBaseCommandBoxItem;
import org.mapton.core.api.ui.MPresetPopOver;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.modules.Places;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.FileChooserHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseExportPresetCommandBoxItem extends BBaseCommandBoxItem {

    public BaseExportPresetCommandBoxItem() {
    }

    public void export(String key) throws ZipException {
        var zipFile = new ZipFile(getFile(key));

        for (var path : MPresetPopOver.getTypeToItems().getOrDefault(key, new ArrayList<>())) {
            var dir = new File(Places.getUserDirectory(), "config/Preferences" + path);
            if (dir.isDirectory()) {
                zipFile.addFolder(dir);
            }
        }
    }

    @Override
    public String getParent() {
        return "%s/%s".formatted(super.getParent(), Dict.EXPORT.toString());
    }

    private File getFile(String key) {
        var filter = FileChooserHelper.getExtensionFilters().get("zip");
        var file = new FileChooserBuilder("presetExport" + key)
                .addFileFilter(filter)
                .setFileFilter(filter)
                .setSelectionApprover(FileChooserHelper.getFileExistSelectionApprover(Almond.getFrame()))
                .setTitle(Dict.EXPORT.toString())
                .showSaveDialog();

        if (file == null) {
            return file;
        } else if (!Strings.CI.equals(FilenameUtils.getExtension(file.getName()), filter.getExtensions()[0])) {
            var s = "%s.%s".formatted(file.getAbsolutePath(), filter.getExtensions()[0]);
            return new File(s);
        }

        return file;
    }

}
