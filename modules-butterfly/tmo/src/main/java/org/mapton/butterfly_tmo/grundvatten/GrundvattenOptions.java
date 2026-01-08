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
package org.mapton.butterfly_tmo.grundvatten;

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
public class GrundvattenOptions extends BOptionsBase implements MPresetActions {

    public static final GrundvattenColorBy DEFAULT_COLOR_BY = GrundvattenColorBy.DEFAULT;
    public static final GrundvattenLabelBy DEFAULT_LABEL_BY = GrundvattenLabelBy.NAME;
    public static final GrundvattenPointBy DEFAULT_POINT_BY = GrundvattenPointBy.PIN;
    private final ObjectProperty<GrundvattenColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<GrundvattenLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<GrundvattenPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static GrundvattenOptions getInstance() {
        return Holder.INSTANCE;
    }

    private GrundvattenOptions() {
        initColorProxyProperty(mColorByProperty, GrundvattenColorBy.class);
        initLabelProxyProperty(mLabelByProperty, GrundvattenLabelBy.class);
        initPointProxyProperty(mPointByProperty, GrundvattenPointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
    }

    public ObjectProperty<GrundvattenColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public GrundvattenColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public GrundvattenLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public GrundvattenPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<GrundvattenLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<GrundvattenPointBy> pointProperty() {
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

        private static final GrundvattenOptions INSTANCE = new GrundvattenOptions();
    }
}
