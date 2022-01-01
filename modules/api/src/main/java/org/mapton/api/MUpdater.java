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

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import javafx.util.Duration;
import javax.swing.Timer;
import org.apache.commons.lang3.time.FastDateFormat;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MUpdater {

    public static final Duration FREQ_10_MINUTES = Duration.minutes(10);
    public static final Duration FREQ_1_MINUTE = Duration.minutes(1);
    public static final Duration FREQ_1_WEEK = Duration.hours(168);
    public static final Duration FREQ_2_HOURS = Duration.hours(2);
    public static final Duration FREQ_2_MINUTES = Duration.minutes(2);
    public static final Duration FREQ_2_WEEKS = Duration.hours(336);
    public static final Duration FREQ_30_MINUTES = Duration.minutes(30);

    protected MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);
    protected Timer mTimer;
    private Long mAgeLimit;
    private boolean mAutoUpdate;
    private Duration mAutoUpdateInterval;
    private Runnable mAutoUpdatePostRunnable;
    private String mCategory = null;
    private String mComment;
    private boolean mMarkedForUpdate;
    private String mName;
    private Runnable mRunnable;

    public Long getAgeLimit() {
        return mAgeLimit;
    }

    public Duration getAutoUpdateInterval() {
        return mAutoUpdateInterval;
    }

    public Runnable getAutoUpdatePostRunnable() {
        return mAutoUpdatePostRunnable;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getComment() {
        return mComment;
    }

    public abstract String getLastUpdated();

    public String getName() {
        return mName;
    }

    public MPrint getPrint() {
        return mPrint;
    }

    public Runnable getRunnable() {
        return mRunnable;
    }

    public boolean isAutoUpdate() {
        return mAutoUpdate;
    }

    public boolean isAutoUpdateEnabled() {
        return true;
    }

    public boolean isMarkedForUpdate() {
        return mMarkedForUpdate;
    }

    public abstract boolean isOutOfDate();

    public void run() {
        mRunnable.run();
    }

    public void setAgeLimit(Long ageLimit) {
        mAgeLimit = ageLimit;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        mAutoUpdate = autoUpdate;
    }

    public void setAutoUpdateInterval(Duration autoUpdateInterval) {
        mAutoUpdateInterval = autoUpdateInterval;
        setAgeLimit(Math.round(mAutoUpdateInterval.toMillis()));
    }

    public void setAutoUpdatePostRunnable(Runnable autoUpdatePostRunnable) {
        mAutoUpdatePostRunnable = autoUpdatePostRunnable;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setComment(String comment) {
        mComment = comment;
    }

    public void setMarkedForUpdate(boolean markedForUpdate) {
        mMarkedForUpdate = markedForUpdate;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPrint(MPrint print) {
        mPrint = print;
    }

    public void setRunnable(Runnable runnable) {
        mRunnable = runnable;
    }

    public static abstract class ByFile extends MUpdater {

        private File mFile;

        public ByFile() {
        }

        public File getFile() {
            return mFile;
        }

        @Override
        public String getLastUpdated() {
            String lastUpdate = "-";
            if (mFile != null && mFile.isFile()) {
                lastUpdate = FastDateFormat.getInstance("yyyy-MM-dd HH.mm.ss").format(new Date(mFile.lastModified()));
            }

            return String.format(Dict.UPDATED_S.toString(), lastUpdate);
        }

        public void initAutoUpdater() {
            final int defaultDelay = (int) getAutoUpdateInterval().toMillis();
            ActionListener actionListener = actionEvent -> {
                new Thread(() -> {
                    if (isAutoUpdateEnabled()) {
                        mPrint.out(String.format("%s %s/%s", "AutoUpdate", getCategory(), getName()));
                        getRunnable().run();
                        mPrint.out(String.format("%s %s/%s, %s", "AutoUpdate", getCategory(), getName(), Dict.DONE.toString().toLowerCase()));
                        mTimer.setDelay(defaultDelay);
                        mTimer.setInitialDelay(defaultDelay);
                        mTimer.restart();

                        if (getAutoUpdatePostRunnable() != null) {
                            getAutoUpdatePostRunnable().run();
                        }
                    } else {
                        mTimer.setDelay(defaultDelay);
                        mTimer.setInitialDelay(defaultDelay);
                        mTimer.restart();
                    }
                }, getClass().getCanonicalName()).start();
            };

            mTimer = new Timer(defaultDelay, actionListener);

            if (!mFile.exists() || SystemHelper.age(mFile.lastModified()) > defaultDelay) {
                actionListener.actionPerformed(null);
            } else {
                long initialDelay = defaultDelay;
                if (mFile.exists()) {
                    initialDelay = mFile.lastModified() + defaultDelay - System.currentTimeMillis();
                }

                int actualDelay = (int) Math.max(0, initialDelay);
                mTimer.setDelay(actualDelay);
                mTimer.setInitialDelay(actualDelay);
                mTimer.start();
            }
        }

        @Override
        public boolean isOutOfDate() {
            return mFile != null && (!mFile.exists() || SystemHelper.age(mFile.lastModified()) >= getAgeLimit());
        }

        public void setFile(File file) {
            mFile = file;
        }
    }
}
