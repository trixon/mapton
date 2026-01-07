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
package org.mapton.butterfly_rock_earthquake;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mapton.api.ui.MPresetActions;
import org.mapton.butterfly_core.api.BOptionsBase;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class QuakeOptions extends BOptionsBase implements MPresetActions {

    public static final QuakeColorBy DEFAULT_COLOR_BY = QuakeColorBy.MAGNITUDE;
    public static final QuakeLabelBy DEFAULT_LABEL_BY = QuakeLabelBy.NONE;
    public static final QuakePointBy DEFAULT_POINT_BY = QuakePointBy.PIN;
    private final ObjectProperty<QuakeColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<QuakeLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<QuakePointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static QuakeOptions getInstance() {
        return Holder.INSTANCE;
    }

    private QuakeOptions() {
        initColorProxyProperty(mColorByProperty, QuakeColorBy.class);
        initLabelProxyProperty(mLabelByProperty, QuakeLabelBy.class);
        initPointProxyProperty(mPointByProperty, QuakePointBy.class);
        setPreferences(NbPreferences.forModule(QuakeOptions.class));
    }

    public ObjectProperty<QuakeColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public QuakeColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public QuakeLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public QuakePointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<QuakeLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<QuakePointBy> pointProperty() {
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

        private static final QuakeOptions INSTANCE = new QuakeOptions();
    }
}
