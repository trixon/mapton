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
package org.mapton.butterfly_composite;

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
public class CompositeLayerOptions extends BLayerOptions implements MPresetActions {

    public static final CompositeColorBy DEFAULT_COLOR_BY = CompositeColorBy.DEFAULT;
    public static final CompositeLabelBy DEFAULT_LABEL_BY = CompositeLabelBy.NAME;
    public static final CompositePointBy DEFAULT_POINT_BY = CompositePointBy.PIN;
    private final ObjectProperty<CompositeColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<CompositeLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<CompositePointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static CompositeLayerOptions getInstance() {
        return Holder.INSTANCE;
    }

    private CompositeLayerOptions() {
        initColorProxyProperty(mColorByProperty, CompositeColorBy.class);
        initLabelProxyProperty(mLabelByProperty, CompositeLabelBy.class);
        initPointProxyProperty(mPointByProperty, CompositePointBy.class);
        setPreferences(getPreferencesForPath());
    }

    public ObjectProperty<CompositeColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public CompositeColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public CompositeLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public CompositePointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    public ObjectProperty<CompositeLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<CompositePointBy> pointProperty() {
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

        private static final CompositeLayerOptions INSTANCE = new CompositeLayerOptions();
    }
}
