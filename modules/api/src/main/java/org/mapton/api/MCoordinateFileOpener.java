/*
 * Copyright 2023 Patrik Karlström.
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

/**
 * Used by FileDropSwitchBoard to handle the opening of dropped files. Sub class and add @ServiceProvider(service = MCoordinateFileOpener.class) in order to provide a FileOpener.
 *
 * @author Patrik Karlström
 */
public abstract class MCoordinateFileOpener {

    private boolean mUsedInFiles = true;

    public abstract String getDescription();

    public abstract String[] getExtensions();

    public abstract String getName();

    public boolean isUsedInFiles() {
        return mUsedInFiles;
    }

    public void setUsedInFiles(boolean usedInFiles) {
        mUsedInFiles = usedInFiles;
    }

}
