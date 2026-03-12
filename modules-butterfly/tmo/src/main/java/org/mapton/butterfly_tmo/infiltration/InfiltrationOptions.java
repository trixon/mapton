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
package org.mapton.butterfly_tmo.infiltration;

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
public class InfiltrationOptions extends BOptionsBase implements MPresetActions {

    public static final InfiltrationColorBy DEFAULT_COLOR_BY = InfiltrationColorBy.DEFAULT;
    public static final InfiltrationLabelBy DEFAULT_LABEL_BY = InfiltrationLabelBy.NAME;
    public static final InfiltrationPointBy DEFAULT_POINT_BY = InfiltrationPointBy.PIN;
    private final ObjectProperty<InfiltrationColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<InfiltrationLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<InfiltrationPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static InfiltrationOptions getInstance() {
        return Holder.INSTANCE;
    }

    private InfiltrationOptions() {
        initColorProxyProperty(mColorByProperty, InfiltrationColorBy.class);
        initLabelProxyProperty(mLabelByProperty, InfiltrationLabelBy.class);
        initPointProxyProperty(mPointByProperty, InfiltrationPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<InfiltrationColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public InfiltrationColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public InfiltrationLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public InfiltrationPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<InfiltrationLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<InfiltrationPointBy> pointProperty() {
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

        private static final InfiltrationOptions INSTANCE = new InfiltrationOptions();
    }
}
