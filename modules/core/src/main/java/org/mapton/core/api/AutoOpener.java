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
package org.mapton.core.api;

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.mapton.api.MSimpleObjectStorageManager;
import org.openide.util.Lookup;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AutoOpener {

    public static void openIfActivated(File file) {
        openIfActivated(file.getAbsolutePath());
    }

    public static void openIfActivated(String path) {
        var ext = FilenameUtils.getExtension(path);

        for (var ao : Lookup.getDefault().lookupAll(MSimpleObjectStorageBoolean.AutoOpen.class)) {
            if (ao.getName().equalsIgnoreCase(ext) && MSimpleObjectStorageManager.getInstance().getBoolean(ao.getClass(), true)) {
                SystemHelper.desktopOpen(new File(path));
                break;
            }
        }
    }
}
