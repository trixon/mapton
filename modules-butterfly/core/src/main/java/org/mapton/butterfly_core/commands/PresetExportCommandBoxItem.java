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
import javafx.util.Duration;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.action.Action;
import org.mapton.api.MCommandBoxItem;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BBaseCommandBoxItem;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.FileChooserHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCommandBoxItem.class)
public class PresetExportCommandBoxItem extends BBaseCommandBoxItem {

    public PresetExportCommandBoxItem() {
    }

    public void export(String key) throws ZipException {
    }

    @Override
    public Action getAction() {
        return new Action("Förinställningar", actionEvent -> {
            try {
                var zipFile = new ZipFile(getFile(getClass().getSimpleName()));
                var dir = new File(Places.getUserDirectory(), "config/Preferences/org/mapton/butterfly");
                if (dir.isDirectory()) {
                    zipFile.addFolder(dir);
                }
                Mapton.notification(MKey.NOTIFICATION_FX_INFORMATION, "Export slutförd", zipFile.getFile().toString(), Duration.seconds(5), (Action) null);
            } catch (ZipException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
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
