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

import java.util.HashMap;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Patrik Karlström
 */
public class MaptonNb {

    private static final HashMap<String, ProgressHandle> mDisplayNameToProgressHandle = new HashMap<>();

    public static void progressClear() {
        for (var progressHandle : mDisplayNameToProgressHandle.values()) {
            progressHandle.finish();
        }

        mDisplayNameToProgressHandle.clear();
    }

    public static ProgressHandle progressStart(String displayName) {
        if (!mDisplayNameToProgressHandle.containsKey(displayName)) {
            var progressHandle = ProgressHandle.createHandle(displayName);
            mDisplayNameToProgressHandle.put(displayName, progressHandle);
            progressHandle.start();
            progressHandle.switchToIndeterminate();
            return progressHandle;
        } else {
            return mDisplayNameToProgressHandle.get(displayName);
        }
    }

    public static void progressStop(String displayName) {
        if (mDisplayNameToProgressHandle.containsKey(displayName)) {
            mDisplayNameToProgressHandle.remove(displayName).finish();
        }
    }
}
