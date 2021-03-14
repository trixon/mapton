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
package org.mapton.maintenance.settings21;

import java.io.File;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.NotifyDescriptor;
import org.openide.modules.OnStart;
import org.openide.modules.Places;
import se.trixon.almond.nbp.dialogs.NbOptionalDialog;
import se.trixon.almond.util.SnapHelper;

/**
 * This class will notify the user of a change in user config/cache paths i.e. 2.1
 *
 * @author Patrik Karlström
 */
@OnStart
public class Settings21 implements Runnable {

    private final String mCache;
    private final String mIntro;
    private final String mUser;

    public Settings21() {
        mIntro = "First off, sorry about the inconvenience.\n\n"
                + "The settings and cache directories has changed and you might want to take action.\n"
                + "Close Mapton before any operations on the directories, and remember, backups are good.\n\n"
                + "Your choices are:\n"
                + " 1 Do nothing, old data will be kept but not used.\n"
                + " 2 Delete OLD directories and start afresh.\n"
                + " 3 a) Clear the contents of NEW, except OLD. b) Move the contents from OLD to NEW. c) Delete OLD.\n\n";

        mUser = "Settings OLD: %s\nSettings NEW: %s\n";
        mCache = "\nCache OLD: %s\nCache NEW: %s\n";
    }

    @Override
    public void run() {
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED, () -> {
            File oldUserDir;
            File oldCacheDir;
            File newUserDir = Places.getUserDirectory();
            File newCacheDir = Places.getCacheDirectory();

            if (SnapHelper.isSnap()) {
                oldUserDir = new File(System.getenv("SNAP_USER_DATA"), ".mapton/2.1");
                oldCacheDir = new File(System.getenv("SNAP_USER_DATA"), ".cache/mapton/2.1");
            } else {
                oldUserDir = new File(newUserDir, "2.1");
                oldCacheDir = new File(newCacheDir, "2.1");
            }

            String message = mIntro;
            if (oldUserDir.isDirectory()) {
                message += String.format(mUser, oldUserDir, newUserDir);
            }

            if (oldCacheDir.isDirectory()) {
                message += String.format(mCache, oldCacheDir, newCacheDir);
            }

            if (!message.equals(mIntro)) {
                NbOptionalDialog.requestShowDialog(
                        Settings21.class,
                        "0",
                        NotifyDescriptor.WARNING_MESSAGE,
                        "IMPORTANT INFORMATION",
                        message,
                        null
                );
            }
        });
    }
}
