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
package org.mapton.core.api.ui;

import java.io.File;
import java.nio.charset.Charset;
import org.mapton.api.MCooTrans;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Patrik Karlström
 */
public class ExportConfiguration {

    private Charset mCharset;
    private MCooTrans mCooTrans;
    private File mFile;
    private ProgressHandle mProgressHandle;

    public ExportConfiguration() {
    }

    public Charset getCharset() {
        return mCharset;
    }

    public MCooTrans getCooTrans() {
        return mCooTrans;
    }

    public File getFile() {
        return mFile;
    }

    public ProgressHandle getProgressHandle() {
        return mProgressHandle;
    }

    public void setCharset(Charset charset) {
        mCharset = charset;
    }

    public void setCooTrans(MCooTrans cooTrans) {
        mCooTrans = cooTrans;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public void setProgressHandle(ProgressHandle progressHandle) {
        mProgressHandle = progressHandle;
    }
}
