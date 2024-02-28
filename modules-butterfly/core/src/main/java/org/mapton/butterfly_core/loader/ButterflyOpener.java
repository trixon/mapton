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
package org.mapton.butterfly_core.loader;

import java.io.File;
import java.util.prefs.Preferences;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.openide.LifecycleManager;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyOpener {

    public static final String KEY_PASSWORDS = "passwords";
    public static final String KEY_PROJECT_FILE = "openProject";
    private final Preferences mPreferences = NbPreferences.forModule(ButterflyOpener.class);
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();

    public static ButterflyOpener getInstance() {
        return Holder.INSTANCE;
    }

    private ButterflyOpener() {
    }

    public void close() {
        if (mPreferences.get(KEY_PROJECT_FILE, null) != null) {
            mPreferences.remove(KEY_PROJECT_FILE);

            var lifecycleManager = LifecycleManager.getDefault();
            lifecycleManager.markForRestart();
            lifecycleManager.exit();
        }
    }

    public void open(File file) {
        mPreferences.put(KEY_PROJECT_FILE, file.toString());
        mButterflyManager.load(file);
    }

    public void restore() {
        var s = mPreferences.get(KEY_PROJECT_FILE, null);

        if (s != null) {
            var file = new File(s);
            if (file.isFile()) {
                open(file);
            } else {
                NbMessage.error(Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), Dict.Dialog.MESSAGE_FILE_NOT_FOUND.toString().formatted(s));
                mPreferences.remove(KEY_PROJECT_FILE);
            }
        } else {
            System.out.println("Nothing to RESTORE");
        }
    }

    private static class Holder {

        private static final ButterflyOpener INSTANCE = new ButterflyOpener();
    }
}
