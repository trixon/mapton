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
package org.mapton.butterfly_structural.tilt;

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
public class TiltOptions extends BOptionsBase implements MPresetActions {

    public static final TiltColorBy DEFAULT_COLOR_BY = TiltColorBy.DEFAULT;
    public static final TiltLabelBy DEFAULT_LABEL_BY = TiltLabelBy.NAME;
    public static final TiltPointBy DEFAULT_POINT_BY = TiltPointBy.PIN;
    private final ObjectProperty<TiltColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<TiltLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<TiltPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static TiltOptions getInstance() {
        return Holder.INSTANCE;
    }

    private TiltOptions() {
        initColorProxyProperty(mColorByProperty, TiltColorBy.class);
        initLabelProxyProperty(mLabelByProperty, TiltLabelBy.class);
        initPointProxyProperty(mPointByProperty, TiltPointBy.class);
        setPreferences(NbPreferences.forModule(TiltOptions.class));
    }

    public ObjectProperty<TiltColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public TiltColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public TiltLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public TiltPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<TiltLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<TiltPointBy> pointProperty() {
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

        private static final TiltOptions INSTANCE = new TiltOptions();
    }
}
