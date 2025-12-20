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
package org.mapton.butterfly_core.api;

import java.io.File;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.mapton.butterfly_core.loader.ButterflyOpener;
import org.mapton.butterfly_format.BundleMode;
import org.mapton.butterfly_format.ButterflyLoader;
import org.openide.util.Exceptions;
import se.trixon.almond.util.swing.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyMonitor {

    private final ButterflyLoader mButterflyLoader = ButterflyLoader.getInstance();
    private final FileAlterationListener mFileAlterationListener;
    private final FileAlterationMonitor mMonitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10));
    private FileAlterationObserver mObserver;
    private boolean mRunning;

    public ButterflyMonitor() {
        mFileAlterationListener = new FileAlterationListenerAdaptor() {
            private File mFile;
            private final DelayedResetRunner mDelayedResetRunner = new DelayedResetRunner(10 * 1000, () -> {
                if (mRunning) {
                    System.out.format("%s ButterflyMonitor: Change detected in %s\n",
                            LocalTime.now(),
                            mFile.toString()
                    );
                    ButterflyOpener.getInstance().restore();
                }
            });

            @Override
            public void onFileChange(File file) {
                load(file);
            }

            @Override
            public void onFileCreate(File file) {
                load(file);
            }

            private void load(File file) {
                if (validForReload(file)) {
                    mFile = file;
                    mDelayedResetRunner.reset();
                }
            }

            private boolean validForReload(File file) {
                if (mButterflyLoader.getBundleMode() == BundleMode.DIR) {
                    return true;
                } else {
                    return file.equals(ButterflyManager.getInstance().getSource());
                }

            }
        };
    }

    public void start() {
        mRunning = true;
        var directory = mButterflyLoader.getSource().getParentFile();
        IOFileFilter filter;
        if (mButterflyLoader.getBundleMode() == BundleMode.DIR) {
            filter = FileFilterUtils.trueFileFilter();
        } else {
            filter = FileFilterUtils.suffixFileFilter(".bfz");
        }

        mObserver = new FileAlterationObserver(directory, filter, IOCase.INSENSITIVE);
        mObserver.addListener(mFileAlterationListener);
        mMonitor.addObserver(mObserver);

        try {
            mMonitor.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void stop() {
        mRunning = false;
        try {
            mObserver.destroy();
            mMonitor.stop();
        } catch (Exception ex) {
            // nvm was not running
        }
        mMonitor.removeObserver(mObserver);
    }

}
