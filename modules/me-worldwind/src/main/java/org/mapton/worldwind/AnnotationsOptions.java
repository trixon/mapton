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
package org.mapton.worldwind;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.BindingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AnnotationsOptions extends OptionsBase implements MPresetActions {

    public static final int DEFAULT_LIMIT = 3;
    public static final AnnotationLimitMode DEFAULT_LIMIT_MODE = AnnotationLimitMode.TOTAL;
    private final ObjectProperty<AnnotationLimitMode> mLimitModeProperty = new SimpleObjectProperty<>(DEFAULT_LIMIT_MODE);
    private final StringProperty mLimitModeProxyProperty = BindingHelper.createStringEnumProxyProperty(mLimitModeProperty, AnnotationLimitMode.class);
    private final IntegerProperty mLimitProperty = new SimpleIntegerProperty(DEFAULT_LIMIT);

    public static AnnotationsOptions getInstance() {
        return Holder.INSTANCE;
    }

    private AnnotationsOptions() {
        setPreferences(NbPreferences.forModule(AnnotationsOptions.class).node("annotations"));
    }

    public int getLimit() {
        return mLimitProperty.get();
    }

    public AnnotationLimitMode getLimitMode() {
        return mLimitModeProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("limit", mLimitProperty);
        sessionManager.register("limitMode", mLimitModeProxyProperty);
    }

    public ObjectProperty<AnnotationLimitMode> limitModeProperty() {
        return mLimitModeProperty;
    }

    public IntegerProperty limitProperty() {
        return mLimitProperty;
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

    public void reset() {
        mLimitProperty.set(DEFAULT_LIMIT);
        mLimitModeProperty.set(DEFAULT_LIMIT_MODE);
    }

    private static class Holder {

        private static final AnnotationsOptions INSTANCE = new AnnotationsOptions();
    }
}
