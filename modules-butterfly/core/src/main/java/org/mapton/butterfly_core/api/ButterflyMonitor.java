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
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.mapton.butterfly_core.loader.ButterflyOpener;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyMonitor {

    private boolean started;

    public ButterflyMonitor() {
    }

    public void start() {
        //TODO call loader for info on what to monitor
        //TODO Handle "Open" new project
        File file = new File("xxxxxxxx");
        if (!file.isDirectory() || started) {
            return;
        }

        var filter = FileFilterUtils.nameFileFilter("butterfly.properties");
        var observer = new FileAlterationObserver(file, filter);
        var monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10));
        var listener = new FileAlterationListenerAdaptor() {

            @Override
            public void onFileCreate(File file) {
                load();
            }

            @Override
            public void onFileChange(File file) {
                load();
            }

            private void load() {
                ButterflyOpener.getInstance().restore();
            }
        };

        observer.addListener(listener);
        monitor.addObserver(observer);

        try {
            monitor.start();
            started = true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
