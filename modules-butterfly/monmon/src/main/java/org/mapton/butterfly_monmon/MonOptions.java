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
package org.mapton.butterfly_monmon;

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
public class MonOptions extends BOptionsBase implements MPresetActions {

    public static final MonColorBy DEFAULT_COLOR_BY = MonColorBy.DEFAULT;
    public static final MonLabelBy DEFAULT_LABEL_BY = MonLabelBy.NONE;
    public static final MonPointBy DEFAULT_POINT_BY = MonPointBy.NONE;
    private final ObjectProperty<MonColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<MonLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<MonPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static MonOptions getInstance() {
        return Holder.INSTANCE;
    }

    private MonOptions() {
        initColorProxyProperty(mColorByProperty, MonColorBy.class);
        initLabelProxyProperty(mLabelByProperty, MonLabelBy.class);
        initPointProxyProperty(mPointByProperty, MonPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<MonColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public MonColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public MonLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public MonPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<MonLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<MonPointBy> pointProperty() {
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

        private static final MonOptions INSTANCE = new MonOptions();
    }
}
