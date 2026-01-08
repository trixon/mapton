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
package org.mapton.butterfly_rock_blast;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mapton.api.ui.MPresetActions;
import org.mapton.butterfly_core.api.BOptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class BlastOptions extends BOptionsBase implements MPresetActions {

    public static final BlastColorBy DEFAULT_COLOR_BY = BlastColorBy.DEFAULT;
    public static final BlastLabelBy DEFAULT_LABEL_BY = BlastLabelBy.NAME;
    public static final BlastPointBy DEFAULT_POINT_BY = BlastPointBy.PIN;
    private final ObjectProperty<BlastColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<BlastLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<BlastPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static BlastOptions getInstance() {
        return Holder.INSTANCE;
    }

    private BlastOptions() {
        initColorProxyProperty(mColorByProperty, BlastColorBy.class);
        initLabelProxyProperty(mLabelByProperty, BlastLabelBy.class);
        initPointProxyProperty(mPointByProperty, BlastPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<BlastColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public BlastColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public BlastLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public BlastPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<BlastLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<BlastPointBy> pointProperty() {
        return mPointByProperty;
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

    @Override
    public void reset() {
        super.reset();
        mPointByProperty.set(DEFAULT_POINT_BY);
        mColorByProperty.set(DEFAULT_COLOR_BY);
        mLabelByProperty.set(DEFAULT_LABEL_BY);
    }

    private static class Holder {

        private static final BlastOptions INSTANCE = new BlastOptions();
    }
}
