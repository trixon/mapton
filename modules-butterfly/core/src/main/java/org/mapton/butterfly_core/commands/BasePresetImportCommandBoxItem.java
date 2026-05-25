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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import javafx.util.Duration;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.UnzipParameters;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BBaseCommandBoxItem;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.modules.Places;
import se.trixon.almond.nbp.FileChooserHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BasePresetImportCommandBoxItem extends BBaseCommandBoxItem {

    private String mDuplicateIndicator;

    public BasePresetImportCommandBoxItem() {
    }

    @Override
    public String getParent() {
        return "%s/%s".formatted(super.getParent(), Dict.IMPORT.toString());
    }

    public void importx(String key) throws ZipException, IOException {
        mDuplicateIndicator = "_KOPIA_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        var file = getFile(key);
        if (file != null && file.isFile()) {
            var tempDir = Files.createTempDirectory("maptonButterflyImport_" + key);
            var zipFile = new ZipFile(file);
            System.out.println(tempDir);
            var params = new UnzipParameters();
            zipFile.extractAll(tempDir.toString(), params);
            var destDir = new File(Places.getUserDirectory(), "config/Preferences/org/mapton/");

            try {
                copyPropertiesWithStructure(tempDir, destDir.toPath(), key);
            } catch (IOException e) {
                System.err.println("Ett fel uppstod: " + e.getMessage());
            }

            FileUtils.forceDeleteOnExit(tempDir.toFile());
            var message = "%s\n\n%s".formatted(file.getAbsolutePath(), "Starta om Mapton för att verkställa");
            Mapton.notification(MKey.NOTIFICATION_FX_INFORMATION, "Import slutförd", message, Duration.seconds(5), (Action) null);
        }
    }

    private void copyPropertiesWithStructure(Path sourceDir, Path targetDir, String filter) throws IOException {
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().contains(filter))
                    .filter(p -> p.toString().endsWith(".properties"))
                    .forEach(sourceFile -> {
                        try {
                            var relativePath = sourceDir.relativize(sourceFile);
                            var targetFileParent = targetDir.resolve(relativePath.getParent() != null ? relativePath.getParent() : Paths.get(""));
                            Files.createDirectories(targetFileParent);
                            var destinationFile = resolveUniqueTargetName(targetFileParent, sourceFile.getFileName().toString());
                            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            System.err.println("Kunde inte kopiera " + sourceFile + ": " + e.getMessage());
                        }
                    });
        }
    }

    private File getFile(String key) {
        var filter = FileChooserHelper.getExtensionFilters().get("zip");
        var file = new FileChooserBuilder("presetImport" + key)
                .addFileFilter(filter)
                .setFileFilter(filter)
                .setTitle(Dict.IMPORT.toString())
                .showOpenDialog();

        return file;
    }

    private Path resolveUniqueTargetName(Path targetSubDir, String originalFileName) {
        var targetFile = targetSubDir.resolve(originalFileName);

        if (!Files.exists(targetFile)) {
            return targetFile;
        }

        var baseName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        var extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        int counter = 1;
        while (Files.exists(targetFile)) {
            var newName = (counter == 1) ? baseName + mDuplicateIndicator + extension : baseName + mDuplicateIndicator + counter + extension;
            targetFile = targetSubDir.resolve(newName);
            counter++;
        }

        return targetFile;
    }
}
