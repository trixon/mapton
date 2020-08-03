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
package org.mapton.api;

import java.io.File;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MSimpleObjectStorageString extends MSimpleObjectStorage<String> {

    public MSimpleObjectStorageString() {
    }

    public static abstract class ApiKey extends MSimpleObjectStorageString {

        public ApiKey() {
            setPromptText(NbBundle.getMessage(MSimpleObjectStorageString.class, "stringStoragePrompt"));
        }

    }

    public static abstract class Misc extends MSimpleObjectStorageString {

    }

    public static abstract class Path extends MSimpleObjectStorageString {

        private File mFile;
        private FileChooserPane.ObjectMode mObjectMode = FileChooserPane.ObjectMode.FILE;

        public File getFile() {
            return mFile;
        }

        public FileChooserPane.ObjectMode getObjectMode() {
            return mObjectMode;
        }

        public void setFile(File file) {
            mFile = file;
        }

        public void setObjectMode(FileChooserPane.ObjectMode objectMode) {
            mObjectMode = objectMode;
        }

    }

    public static abstract class Url extends MSimpleObjectStorageString {

    }
}
