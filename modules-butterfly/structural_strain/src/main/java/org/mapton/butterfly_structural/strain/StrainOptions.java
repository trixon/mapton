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
package org.mapton.butterfly_structural.strain;

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
public class StrainOptions extends BOptionsBase implements MPresetActions {

    public static final StrainColorBy DEFAULT_COLOR_BY = StrainColorBy.DEFAULT;
    public static final StrainLabelBy DEFAULT_LABEL_BY = StrainLabelBy.NAME;
    public static final StrainPointBy DEFAULT_POINT_BY = StrainPointBy.PIN;
    private final ObjectProperty<StrainColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<StrainLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<StrainPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static StrainOptions getInstance() {
        return Holder.INSTANCE;
    }

    private StrainOptions() {
        initColorProxyProperty(mColorByProperty, StrainColorBy.class);
        initLabelProxyProperty(mLabelByProperty, StrainLabelBy.class);
        initPointProxyProperty(mPointByProperty, StrainPointBy.class);
        setPreferences(NbPreferences.forModule(StrainOptions.class));
    }

    public ObjectProperty<StrainColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public StrainColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public StrainLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public StrainPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<StrainLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<StrainPointBy> pointProperty() {
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

        private static final StrainOptions INSTANCE = new StrainOptions();
    }
}
