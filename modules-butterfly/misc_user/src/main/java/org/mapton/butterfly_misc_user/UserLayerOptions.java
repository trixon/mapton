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
package org.mapton.butterfly_misc_user;

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
public class UserLayerOptions extends BLayerOptions implements MPresetActions {

    public static final UserColorBy DEFAULT_COLOR_BY = UserColorBy.DEFAULT;
    public static final UserLabelBy DEFAULT_LABEL_BY = UserLabelBy.MISC_DATE;
    public static final UserPointBy DEFAULT_POINT_BY = UserPointBy.PIN;
    private final ObjectProperty<UserColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<UserLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<UserPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static UserLayerOptions getInstance() {
        return Holder.INSTANCE;
    }

    private UserLayerOptions() {
        initColorProxyProperty(mColorByProperty, UserColorBy.class);
        initLabelProxyProperty(mLabelByProperty, UserLabelBy.class);
        initPointProxyProperty(mPointByProperty, UserPointBy.class);
        setPreferences(getPreferencesForPath());
        disablePlotAlarm();
        disablePlotDebt();
    }

    public ObjectProperty<UserColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public UserColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public UserLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public UserPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<UserLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<UserPointBy> pointProperty() {
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

        private static final UserLayerOptions INSTANCE = new UserLayerOptions();
    }
}
