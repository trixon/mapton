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
package org.mapton.addon.photos;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class PhotosOptions extends OptionsBase implements MPresetActions {

    public static final String KEY_SETTINGS = "settings";

    public static PhotosOptions getInstance() {
        return Holder.INSTANCE;
    }

    private PhotosOptions() {
        setPreferences(NbPreferences.forModule(PhotosOptions.class));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
//        sessionManager.register("opacity", mOpacityProperty);
    }

//    public DoubleProperty opacityProperty() {
//        return mOpacityProperty;
//    }
    @Override
    public void presetRestore(Preferences preferences) {
        presetStore(preferences);
    }

    @Override
    public void presetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    public void reset() {
//        mOpacityProperty.set(DEFAULT_OPACITY);
    }

    private static class Holder {

        private static final PhotosOptions INSTANCE = new PhotosOptions();
    }
}
