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
package org.mapton.butterfly_structural.crack;

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
public class CrackOptions extends BOptionsBase implements MPresetActions {

    public static final CrackColorBy DEFAULT_COLOR_BY = CrackColorBy.DEFAULT;
    public static final CrackLabelBy DEFAULT_LABEL_BY = CrackLabelBy.NAME;
    public static final CrackPointBy DEFAULT_POINT_BY = CrackPointBy.PIN;
    private final ObjectProperty<CrackColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<CrackLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<CrackPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static CrackOptions getInstance() {
        return Holder.INSTANCE;
    }

    private CrackOptions() {
        initColorProxyProperty(mColorByProperty, CrackColorBy.class);
        initLabelProxyProperty(mLabelByProperty, CrackLabelBy.class);
        initPointProxyProperty(mPointByProperty, CrackPointBy.class);
        setPreferences(NbPreferences.forModule(CrackOptions.class));
    }

    public ObjectProperty<CrackColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public CrackColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public CrackLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public CrackPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<CrackLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<CrackPointBy> pointProperty() {
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

        private static final CrackOptions INSTANCE = new CrackOptions();
    }
}
