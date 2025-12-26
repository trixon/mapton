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
package org.mapton.worldwind;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class BackgroundImageOptions extends OptionsBase implements MPresetActions {

    public static final double DEFAULT_OPACITY = 0.9;
    private final DoubleProperty mOpacityProperty = new SimpleDoubleProperty(DEFAULT_OPACITY);

    public static BackgroundImageOptions getInstance() {
        return Holder.INSTANCE;
    }

    private BackgroundImageOptions() {
        setPreferences(NbPreferences.forModule(BackgroundImageOptions.class).node("backgroundImage"));
    }

    public double getOpacity() {
        return mOpacityProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("opacity", mOpacityProperty);
    }

    public DoubleProperty opacityProperty() {
        return mOpacityProperty;
    }

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
        mOpacityProperty.set(DEFAULT_OPACITY);
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        initSession(sessionManager);

        return sessionManager;
    }

    private static class Holder {

        private static final BackgroundImageOptions INSTANCE = new BackgroundImageOptions();
    }
}
