/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly.bcc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly.bcc.helper.BccHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Executor {

    private final CmdConfig mConfig = CmdConfig.getInstance();

    public Executor() {
    }

    public void execute() throws IOException, ClassNotFoundException, InterruptedException {
        Thread.sleep(1000);

        if (!mConfig.isValid()) {
            System.out.println("invalid args");
            return;
        }

        System.out.println("bcc");
        System.out.println(System.getProperty("java.home"));
        var tempPath = Files.createTempDirectory("butterfly");
        System.out.println(tempPath);
        tempPath.toFile().deleteOnExit();
        System.setProperty(BccHelper.WORKING_DIRECTORY_PATH, tempPath.toString());

        executePlugins();

        if (StringUtils.isNotBlank(mConfig.getResourceDir())) {
            copyResources(tempPath.toFile());
        }
        zip(tempPath.toFile());

        System.out.println("Created " + mConfig.getDestFile());
    }

    private void copyResources(File destDir) throws IOException {
        var source = new File(mConfig.getResourceDir());
        for (var file : source.listFiles()) {
            if (file.isFile()) {
                FileUtils.copyFileToDirectory(file, destDir, true);
            } else {
                FileUtils.copyDirectoryToDirectory(file, destDir);
            }
        }
    }

    private void executePlugins() throws ClassNotFoundException {
        for (int i = 0; i < mConfig.getClasses().length; i++) {
            var className = mConfig.getClasses()[i];
            var arg = mConfig.getClassArgs()[i];

            System.out.println("Load: " + className);
            System.out.println("Args: " + arg);
            if (StringUtils.startsWithIgnoreCase(arg, "disabled")) {
                System.out.println("continue");
                continue;
            }
            BccHelper.put(className, arg);
            var c = Class.forName(className);
            var result = BccHelper.get(className);
            System.out.println("Result: " + result);
            System.out.println();
        }
    }

    private void zip(File sourceDir) throws ZipException, IOException {
        var zipParameters = new ZipParameters();
        zipParameters.setCompressionLevel(CompressionLevel.ULTRA);
        zipParameters.setIncludeRootFolder(false);
//        zipParameters.setEncryptFiles(true);
//        zipParameters.setEncryptionMethod(EncryptionMethod.AES);

        var destFile = new File(mConfig.getDestFile());
        if (destFile.isFile()) {
            FileUtils.deleteQuietly(destFile);
        }

        try (var zipFile = new ZipFile(destFile, mConfig.getPassword())) {
            zipFile.addFolder(sourceDir, zipParameters);
        }
    }
}
