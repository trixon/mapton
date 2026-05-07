/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_topo.heightpair;

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
public class HeightPairOptions extends BOptionsBase implements MPresetActions {

    public static final HeightPairColorBy DEFAULT_COLOR_BY = HeightPairColorBy.DEFAULT;
    public static final HeightPairLabelBy DEFAULT_LABEL_BY = HeightPairLabelBy.NAME;
    public static final HeightPairPointBy DEFAULT_POINT_BY = HeightPairPointBy.PIN;
    private final ObjectProperty<HeightPairColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<HeightPairLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<HeightPairPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static HeightPairOptions getInstance() {
        return Holder.INSTANCE;
    }

    private HeightPairOptions() {
        initColorProxyProperty(mColorByProperty, HeightPairColorBy.class);
        initLabelProxyProperty(mLabelByProperty, HeightPairLabelBy.class);
        initPointProxyProperty(mPointByProperty, HeightPairPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<HeightPairColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public HeightPairColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public HeightPairLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public HeightPairPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<HeightPairLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<HeightPairPointBy> pointProperty() {
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

        private static final HeightPairOptions INSTANCE = new HeightPairOptions();
    }
}
