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
package org.mapton.butterfly_geo.inclinometer;

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
public class InclinoOptions extends BOptionsBase implements MPresetActions {

    public static final InclinoColorBy DEFAULT_COLOR_BY = InclinoColorBy.DEFAULT;
    public static final InclinoLabelBy DEFAULT_LABEL_BY = InclinoLabelBy.NAME;
    public static final InclinoPointBy DEFAULT_POINT_BY = InclinoPointBy.PIN;
    private final ObjectProperty<InclinoColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<InclinoLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<InclinoPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static InclinoOptions getInstance() {
        return Holder.INSTANCE;
    }

    private InclinoOptions() {
        initColorProxyProperty(mColorByProperty, InclinoColorBy.class);
        initLabelProxyProperty(mLabelByProperty, InclinoLabelBy.class);
        initPointProxyProperty(mPointByProperty, InclinoPointBy.class);
        setPreferences(NbPreferences.forModule(InclinoOptions.class));
    }

    public ObjectProperty<InclinoColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public InclinoColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public InclinoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public InclinoPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<InclinoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<InclinoPointBy> pointProperty() {
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

        private static final InclinoOptions INSTANCE = new InclinoOptions();
    }
}
