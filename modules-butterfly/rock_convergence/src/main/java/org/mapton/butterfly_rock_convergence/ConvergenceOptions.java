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
package org.mapton.butterfly_rock_convergence;

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
public class ConvergenceOptions extends BOptionsBase implements MPresetActions {

    public static final ConvergenceColorBy DEFAULT_COLOR_BY = ConvergenceColorBy.DEFAULT;
    public static final ConvergenceLabelBy DEFAULT_LABEL_BY = ConvergenceLabelBy.NAME;
    public static final ConvergencePointBy DEFAULT_POINT_BY = ConvergencePointBy.PIN;
    private final ObjectProperty<ConvergenceColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<ConvergenceLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<ConvergencePointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static ConvergenceOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceOptions() {
        initColorProxyProperty(mColorByProperty, ConvergenceColorBy.class);
        initLabelProxyProperty(mLabelByProperty, ConvergenceLabelBy.class);
        initPointProxyProperty(mPointByProperty, ConvergencePointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<ConvergenceColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public ConvergenceColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public ConvergenceLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public ConvergencePointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<ConvergenceLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<ConvergencePointBy> pointProperty() {
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

        private static final ConvergenceOptions INSTANCE = new ConvergenceOptions();
    }
}
