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
package org.mapton.butterfly_topo;

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
public class TopoOptions extends BOptionsBase implements MPresetActions {

    public static final TopoColorBy DEFAULT_COLOR_BY = TopoColorBy.ALARM;
    public static final TopoLabelBy DEFAULT_LABEL_BY = TopoLabelBy.NAME;
    public static final TopoPointBy DEFAULT_POINT_BY = TopoPointBy.PIN;
    private final ObjectProperty<TopoColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<TopoLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<TopoPointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static TopoOptions getInstance() {
        return Holder.INSTANCE;
    }

    private TopoOptions() {
        setPreferences(NbPreferences.forModule(TopoOptions.class));
    }

    public ObjectProperty<TopoColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public TopoColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public TopoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public TopoPointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        initColorProxyProperty(mColorByProperty, TopoColorBy.class);
        initLabelProxyProperty(mLabelByProperty, TopoLabelBy.class);
        initPointProxyProperty(mPointByProperty, TopoPointBy.class);
        super.initSession(sessionManager);
    }

    public ObjectProperty<TopoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<TopoPointBy> pointProperty() {
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

        private static final TopoOptions INSTANCE = new TopoOptions();
    }
}
