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
package org.mapton.butterfly_topo.grade.distance;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mapton.api.ui.MPresetActions;
import org.mapton.butterfly_core.api.BLayerOptions;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.grade.GradePointBy;

/**
 *
 * @author Patrik Karlström
 */
public class DistanceLayerOptions extends BLayerOptions implements MPresetActions {

    public static final DistanceColorBy DEFAULT_COLOR_BY = DistanceColorBy.DEFAULT;
    public static final BDimension DEFAULT_DISTANCE_MODE = BDimension._1d;
    public static final DistanceLabelBy DEFAULT_LABEL_BY = DistanceLabelBy.NAME;
    public static final GradePointBy DEFAULT_POINT_BY = GradePointBy.PIN;
    private final ObjectProperty<DistanceColorBy> mColorByProperty = new SimpleObjectProperty<>(DEFAULT_COLOR_BY);
    private final ObjectProperty<BDimension> mDistanceModeProperty = new SimpleObjectProperty<>(DEFAULT_DISTANCE_MODE);
    private final ObjectProperty<DistanceLabelBy> mLabelByProperty = new SimpleObjectProperty<>(DEFAULT_LABEL_BY);
    private final ObjectProperty<GradePointBy> mPointByProperty = new SimpleObjectProperty<>(DEFAULT_POINT_BY);

    public static DistanceLayerOptions getInstance() {
        return Holder.INSTANCE;
    }

    private DistanceLayerOptions() {
        initColorProxyProperty(mColorByProperty, DistanceColorBy.class);
        initLabelProxyProperty(mLabelByProperty, DistanceLabelBy.class);
        initPointProxyProperty(mPointByProperty, GradePointBy.class);
        setPreferences(getPreferencesForPath("optionPresets"));
        disablePlotDebt();
    }

    public ObjectProperty<DistanceColorBy> colorByProperty() {
        return mColorByProperty;
    }

    public ObjectProperty<BDimension> distanceModeProperty() {
        return mDistanceModeProperty;
    }

    public DistanceColorBy getColorBy() {
        return mColorByProperty.get();
    }

    public BDimension getDistanceMode() {
        return mDistanceModeProperty.get();
    }

    public DistanceLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public GradePointBy getPointBy() {
        return mPointByProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
//        sessionManager.register(getKeyOptions("distanceMode"), mDistanceModeProperty);

    }

    public ObjectProperty<DistanceLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    public ObjectProperty<GradePointBy> pointProperty() {
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

        private static final DistanceLayerOptions INSTANCE = new DistanceLayerOptions();
    }
}
