/*
 * Copyright 2021 Patrik Karlström.
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

import java.awt.Frame;
import java.util.HashMap;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.windows.WindowManager;

/**
 *
 * @author Patrik Karlström
 */
public class MaptonNb {

    private static final HashMap<String, ProgressHandle> mDisplayNameToProgressHandle = new HashMap<>();

    public static Frame getFrame() {
        return WindowManager.getDefault().getMainWindow();
    }

    public static void progressClear() {
        for (var progressHandle : mDisplayNameToProgressHandle.values()) {
            progressHandle.finish();
        }

        mDisplayNameToProgressHandle.clear();
    }

    public static ProgressHandle progressStart(String displayName) {
        if (!mDisplayNameToProgressHandle.containsKey(displayName)) {
            ProgressHandle progressHandle = ProgressHandleFactory.createSystemHandle(displayName);
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
