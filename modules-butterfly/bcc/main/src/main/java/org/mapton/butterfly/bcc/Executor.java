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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.mapton.butterfly.bcc.helper.BccHelper;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class Executor {

    private final CmdConfig mConfig = CmdConfig.getInstance();
    private File mShaFile;

    public Executor() {
    }

    public void execute() throws IOException, ClassNotFoundException, InterruptedException {
        Thread.sleep(1000);
        var startTime = LocalTime.now();
        if (!mConfig.isValid()) {
            System.out.println("invalid args");
            return;
        }

        System.out.println("bcc");
        System.out.println(System.getProperty("java.home"));

        File workingDir;
        if (StringUtils.isBlank(mConfig.getWorkingDir())) {
            workingDir = Files.createTempDirectory("butterfly").toFile();
            workingDir.deleteOnExit();
        } else {
            workingDir = new File(mConfig.getWorkingDir());
            if (workingDir.isFile()) {
                throw new IOException("workingDir can't be a file");
            }
        }

        System.out.println(workingDir);
        System.setProperty(BccHelper.WORKING_DIRECTORY_PATH, workingDir.toString());

        executePlugins();

        if (StringUtils.isNotBlank(mConfig.getResourceDir())) {
            copyResources(workingDir);
        }

        addMetaData(workingDir);
        zip(workingDir);

        System.out.println("Created " + mConfig.getDestFile());

        if (ObjectUtils.allNotNull(mConfig.getFtpPassword(), mConfig.getFtpPath(), mConfig.getFtpServer(), mConfig.getFtpUser())) {
            System.out.println("*** FTP UPLOAD BEG ***");
            upload();
            System.out.println("*** FTP UPLOAD END ***");
        } else {
            System.out.println("Skipping ftp upload");
        }

        var endTime = LocalTime.now();
        System.out.println("\nBCC completed");
        System.out.println("BEG: " + startTime);
        System.out.println("END: " + endTime);
        System.out.println("TOT: " + Duration.between(startTime, endTime).abs());
    }

    private void addMetaData(File dir) {
        try {
            var config = new PropertiesConfiguration();
            config.setProperty(Butterfly.KEY_TIMESTAMP, LocalDateTime.now());
            config.setProperty(Butterfly.KEY_FORMAT, Butterfly.FORMAT);
            var fileHandler = new FileHandler(config);
            fileHandler.setFile(new File(dir, Butterfly.VERSION_FILE));
            fileHandler.save();
        } catch (ConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
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
            System.err.println("...");
            System.out.flush();
            System.err.flush();
            System.gc();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            var className = mConfig.getClasses()[i];
            var arg = mConfig.getClassArgs()[i];
            System.out.println("");
            System.out.println("Load: " + className);
            System.out.println("Args: " + arg);
            if (Strings.CI.startsWith(arg, "disabled")) {
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

    private void upload() {
        System.out.println("server: " + mConfig.getFtpServer());
        System.out.println("path  : " + mConfig.getFtpPath());

        var ftp = new FTPClient();
        try {
            System.out.println("connecting...");
            ftp.connect(mConfig.getFtpServer(), 21);

            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                System.out.println(ftp.getReplyString());
                ftp.disconnect();
                return;
            }

            System.out.println("connected: " + ftp.getReplyString());

            if (ftp.login(mConfig.getFtpUser(), mConfig.getFtpPassword())) {
                ftp.enterLocalPassiveMode();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.changeWorkingDirectory(mConfig.getFtpPath());

                var bfzFile = new File(mConfig.getDestFile());

                for (var f : List.of(bfzFile, mShaFile)) {
                    if (!f.isFile()) {
                        System.out.println("File not found: " + f);
                        continue;
                    }
                    try (var inputStream = new FileInputStream(f)) {
                        System.out.println("uploading... " + f);
                        ftp.storeFile(f.getName(), inputStream);
                        System.out.println("uploaded");
                    }
                }
                ftp.logout();
                ftp.disconnect();
            } else {
                System.out.println("Login failed " + ftp.getStatus());
                System.out.println(ftp.getReplyString());
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    // nvm
                }
            }
        }
    }

    private void zip(File sourceDir) throws ZipException, IOException {
        var zipParameters = new ZipParameters();
        zipParameters.setCompressionLevel(CompressionLevel.ULTRA);
        zipParameters.setIncludeRootFolder(false);

        var password = ArrayUtils.addAll(mConfig.getPassword(), mConfig.getPassword());
        if (password != null && password.length > 0) {
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        }

        var destFile = new File(mConfig.getDestFile());
        if (destFile.isFile()) {
            FileUtils.deleteQuietly(destFile);
        }

        try (var zipFile = new ZipFile(destFile, password)) {
            zipFile.addFolder(sourceDir, zipParameters);
        }

        mShaFile = new File(destFile.toString() + ".sha256");

        try (var fis = new FileInputStream(destFile)) {
            var hash = DigestUtils.sha256Hex(fis);
            Files.writeString(mShaFile.toPath(), "%s %s".formatted(hash, destFile.getName()));
        }
    }
}
