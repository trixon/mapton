/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.poi_trv_ti;

import java.awt.event.ActionListener;
import java.io.File;
import javafx.util.Duration;
import javax.swing.Timer;
import org.apache.commons.io.FilenameUtils;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class DownloadJob {

    private Timer mTimer;

    public DownloadJob(File file, Duration updateFrequency, Runnable r) {
        final int defaultDelay = (int) updateFrequency.toMillis();
        String fileName = FilenameUtils.getBaseName(file.getAbsolutePath());
        ActionListener actionListener = actionEvent -> {
            new Thread(() -> {
                System.out.println("Start download " + fileName);
                r.run();
                System.out.println("End download " + fileName);
                mTimer.setDelay(defaultDelay);
                mTimer.setInitialDelay(defaultDelay);
                mTimer.restart();
            }).start();
        };

        mTimer = new Timer(defaultDelay, actionListener);

        if (!file.exists() || SystemHelper.age(file.lastModified()) > updateFrequency.toMillis()) {
            actionListener.actionPerformed(null);
        }

        long initialDelay = defaultDelay;
        if (file.exists()) {
            initialDelay = file.lastModified() + defaultDelay - System.currentTimeMillis();
        }

        if (!mTimer.isRunning()) {
            int actualDelay = (int) Math.max(0, initialDelay);
            mTimer.setDelay(actualDelay);
            mTimer.setInitialDelay(actualDelay);
            mTimer.start();
        }
    }
}
