/*
 * Copyright 2019 Patrik Karlström.
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
import java.text.SimpleDateFormat;
import java.util.Date;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MUpdater {

    protected MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);

    private String mCategory = null;
    private String mComment;
    private boolean mMarkedForUpdate;
    private String mName;
    private Runnable mRunnable;

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

    public boolean isMarkedForUpdate() {
        return mMarkedForUpdate;
    }

    public abstract boolean isOutOfDate();

    public void run() {
        mRunnable.run();
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

        public abstract Long getAgeLimit();

        public File getFile() {
            return mFile;
        }

        @Override
        public String getLastUpdated() {
            String lastUpdate = "-";
            if (mFile != null && mFile.isFile()) {
                lastUpdate = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date(mFile.lastModified()));
            }

            return String.format(Dict.UPDATED_S.toString(), lastUpdate);
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
