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
package org.mapton.api;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MFileWatcher {

    private final DigestUtils mDigestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);
    private final HashMap<File, String> mFileToDigest = new HashMap<>();
    private final HashMap<File, Boolean> mFileToBoolean = new HashMap<>();

    public void enable(File file) {
        mFileToBoolean.put(file, Boolean.TRUE);
    }

    public void disable(File file) {
        mFileToBoolean.put(file, Boolean.FALSE);
    }

    public static MFileWatcher getInstance() {
        return Holder.INSTANCE;
    }

    private MFileWatcher() {
    }

    public void addWatch(File file, long interval, MFileWatcherListener fileWatcherListener) {
        var directory = file.getParentFile();

        var directoryFilter = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.nameFileFilter(directory.getName()));

        var fileFilter = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.nameFileFilter(file.getName()));

        var filter = FileFilterUtils.or(directoryFilter, fileFilter);

        var observer = new FileAlterationObserver(directory, filter);
        var monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(1), observer);
        var listener = new FileAlterationListenerAdaptor() {
            private final File fileToMonitor = file;

            @Override
            public void onFileChange(File file) {
                if (file.equals(fileToMonitor) && mFileToBoolean.getOrDefault(file, Boolean.TRUE)) {
                    var oldValue = mFileToDigest.get(file);
                    try {
                        var newValue = mDigestUtils.digestAsHex(file);
                        if (!StringUtils.equals(newValue, oldValue)) {
                            mFileToDigest.put(file, newValue);
                            fileWatcherListener.onFileChange(file);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

            @Override
            public void onFileDelete(File file) {
                if (file.equals(fileToMonitor)) {
                    fileWatcherListener.onFileDelete(file);
                }
            }
        };

        new Thread(() -> {
            observer.addListener(listener);
            try {
                monitor.start();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }, "%s: %s".formatted(MFileWatcher.class.getName(), file.getName())).start();
    }

    private static class Holder {

        private static final MFileWatcher INSTANCE = new MFileWatcher();
    }
}
