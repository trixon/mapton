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
package org.mapton.butterfly_remote.insar;

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
public class InsarOptions extends BOptionsBase implements MPresetActions {

    public static final InsarColorBy DEFAULT_COLOR_BY = InsarColorBy.DISPLACEMENT;
    public static final InsarLabelBy DEFAULT_LABEL_BY = InsarLabelBy.NONE;
    public static final InsarPointBy DEFAULT_POINT_BY = InsarPointBy.PIN;
    private final ObjectProperty<InsarColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<InsarLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<InsarPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static InsarOptions getInstance() {
        return Holder.INSTANCE;
    }

    private InsarOptions() {
        initColorProxyProperty(mColorByProperty, InsarColorBy.class);
        initLabelProxyProperty(mLabelByProperty, InsarLabelBy.class);
        initPointProxyProperty(mPointByProperty, InsarPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<InsarColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public InsarColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public InsarLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public InsarPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<InsarLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<InsarPointBy> pointProperty() {
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

        private static final InsarOptions INSTANCE = new InsarOptions();
    }
}
