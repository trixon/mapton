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
package org.mapton.butterfly_rock_extensometer;

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
public class ExtensoOptions extends BOptionsBase implements MPresetActions {

    public static final ExtensoColorBy DEFAULT_COLOR_BY = ExtensoColorBy.DEFAULT;
    public static final ExtensoLabelBy DEFAULT_LABEL_BY = ExtensoLabelBy.NAME;
    public static final ExtensoPointBy DEFAULT_POINT_BY = ExtensoPointBy.PIN;
    private final ObjectProperty<ExtensoColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<ExtensoLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<ExtensoPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static ExtensoOptions getInstance() {
        return Holder.INSTANCE;
    }

    private ExtensoOptions() {
        initColorProxyProperty(mColorByProperty, ExtensoColorBy.class);
        initLabelProxyProperty(mLabelByProperty, ExtensoLabelBy.class);
        initPointProxyProperty(mPointByProperty, ExtensoPointBy.class);
        setPreferences(NbPreferences.forModule(ExtensoOptions.class));
    }

    public ObjectProperty<ExtensoColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public ExtensoColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public ExtensoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public ExtensoPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<ExtensoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<ExtensoPointBy> pointProperty() {
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

        private static final ExtensoOptions INSTANCE = new ExtensoOptions();
    }
}
