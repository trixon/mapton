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
package org.mapton.butterfly_misc_xyz;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mapton.api.ui.MPresetActions;
import org.mapton.butterfly_core.api.BLayerOptions;

/**
 *
 * @author Patrik Karlström
 */
public class XyzLayerOptions extends BLayerOptions implements MPresetActions {

    public static final XyzColorBy DEFAULT_COLOR_BY = XyzColorBy.DEFAULT;
    public static final XyzLabelBy DEFAULT_LABEL_BY = XyzLabelBy.MISC_DATE;
    public static final XyzPointBy DEFAULT_POINT_BY = XyzPointBy.PIN;
    private final ObjectProperty<XyzColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<XyzLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<XyzPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static XyzLayerOptions getInstance() {
        return Holder.INSTANCE;
    }

    private XyzLayerOptions() {
        initColorProxyProperty(mColorByProperty, XyzColorBy.class);
        initLabelProxyProperty(mLabelByProperty, XyzLabelBy.class);
        initPointProxyProperty(mPointByProperty, XyzPointBy.class);
        setPreferences(getPreferencesForPath());
        disablePlotAlarm();
        disablePlotDebt();
    }

    public ObjectProperty<XyzColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public XyzColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public XyzLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public XyzPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<XyzLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<XyzPointBy> pointProperty() {
        return mPointByProperty;
    }

    @Override
    public void presetRestore(Preferences preferences) {
        reset();
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

        private static final XyzLayerOptions INSTANCE = new XyzLayerOptions();
    }
}
