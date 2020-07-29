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
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MStringStorage {

    private String mDefaultValue = null;
    private String mGroup = Dict.MISCELLANEOUS.toString();
    private String mName;
    private String mPromptText;
    private String mTooltipText;
//    private boolean mUseDefaultValue;
    private String mValue;

    public MStringStorage() {
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getName() {
        return mName;
    }

    public String getPromptText() {
        return mPromptText;
    }

    public String getTooltipText() {
        return mTooltipText;
    }

    public String getValue() {
        return mValue;
    }

//    public boolean isUseDefaultValue() {
//        return mUseDefaultValue;
//    }
    public void setDefaultValue(String defaultValue) {
        mDefaultValue = defaultValue;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPromptText(String promptText) {
        mPromptText = promptText;
    }

    public void setTooltipText(String tooltipText) {
        mTooltipText = tooltipText;
    }

//    public void setUseDefaultValue(boolean useDefaultValue) {
//        mUseDefaultValue = useDefaultValue;
//    }
    public void setValue(String value) {
        mValue = value;
    }

    public static abstract class ApiKey extends MStringStorage {

        public ApiKey() {
            setPromptText(NbBundle.getMessage(MStringStorage.class, "stringStoragePrompt"));
        }

    }

    public static abstract class Misc extends MStringStorage {

    }

    public static abstract class Path extends MStringStorage {

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

    public static abstract class Url extends MStringStorage {

    }
}
