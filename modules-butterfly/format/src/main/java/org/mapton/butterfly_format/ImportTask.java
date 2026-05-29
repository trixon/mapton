/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_format;

import java.io.File;
import java.util.ArrayList;
import org.mapton.butterfly_format.io.ImportFromCsv;

/**
 *
 * @author Patrik Karlström
 */
public class ImportTask<T> {

    private final Class<T> mClazz;
    private final String mFileName;
    private final ArrayList<T> mTargetList;
    private final int mInitialCapacity;

    public ImportTask(Class<T> clazz, String fileName, ArrayList<T> targetList) {
        this(clazz, fileName, targetList, -1);
    }

    public ImportTask(Class<T> clazz, String fileName, ArrayList<T> targetList, int initialCapacity) {
        mClazz = clazz;
        mFileName = fileName;
        mTargetList = targetList;
        mInitialCapacity = initialCapacity;
    }

    public void execute(File sourceDir) {
        if (mInitialCapacity > 0) {
            mTargetList.ensureCapacity(mInitialCapacity);
        }
        new ImportFromCsv<T>(mClazz) {
        }.load(sourceDir, mFileName, mTargetList);
    }

}
