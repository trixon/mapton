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
package org.mapton.butterfly_tmo.tunnelvatten;

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
public class TunnelvattenOptions extends BOptionsBase implements MPresetActions {

    public static final TunnelvattenColorBy DEFAULT_COLOR_BY = TunnelvattenColorBy.DEFAULT;
    public static final TunnelvattenLabelBy DEFAULT_LABEL_BY = TunnelvattenLabelBy.NAME;
    public static final TunnelvattenPointBy DEFAULT_POINT_BY = TunnelvattenPointBy.PIN;
    private final ObjectProperty<TunnelvattenColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<TunnelvattenLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<TunnelvattenPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static TunnelvattenOptions getInstance() {
        return Holder.INSTANCE;
    }

    private TunnelvattenOptions() {
        initColorProxyProperty(mColorByProperty, TunnelvattenColorBy.class);
        initLabelProxyProperty(mLabelByProperty, TunnelvattenLabelBy.class);
        initPointProxyProperty(mPointByProperty, TunnelvattenPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<TunnelvattenColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public TunnelvattenColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public TunnelvattenLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public TunnelvattenPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<TunnelvattenLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<TunnelvattenPointBy> pointProperty() {
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

        private static final TunnelvattenOptions INSTANCE = new TunnelvattenOptions();
    }
}
