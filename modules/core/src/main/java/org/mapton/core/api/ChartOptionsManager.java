/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import org.mapton.api.MAvgPeriod;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.BindingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartOptionsManager {

    private final ObjectProperty<MAvgPeriod> mAvgPeriod1Property = new SimpleObjectProperty<>(MAvgPeriod.NERVOUS);
    private final StringProperty mAvgPeriod1ProxyProperty = BindingHelper.createStringEnumProxyProperty(mAvgPeriod1Property, MAvgPeriod.class);
    private final ObjectProperty<MAvgPeriod> mAvgPeriod2Property = new SimpleObjectProperty<>(MAvgPeriod.CALM);
    private final StringProperty mAvgPeriod2ProxyProperty = BindingHelper.createStringEnumProxyProperty(mAvgPeriod2Property, MAvgPeriod.class);
    private final BooleanProperty mAvgPlotDiffProperty = new SimpleBooleanProperty();
    private final BooleanProperty mAvgPlotPeriod1Property = new SimpleBooleanProperty(true);
    private final BooleanProperty mAvgPlotPeriod2Property = new SimpleBooleanProperty();
    private final BooleanProperty mAvgPlotRawProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty mDateEndTodayProperty = new SimpleBooleanProperty();
    private final ObjectProperty<ChartStartPoint> mDatePeriodProperty = new SimpleObjectProperty<>(ChartStartPoint.ZERO);
    private final StringProperty mDatePeriodProxyProperty = BindingHelper.createStringEnumProxyProperty(mDatePeriodProperty, ChartStartPoint.class);
    private final BooleanProperty mDateResetOnFirstProperty = new SimpleBooleanProperty();
    private final ObjectProperty<ChartMiscLineMode> mMiscLineModeProperty = new SimpleObjectProperty<>(ChartMiscLineMode.EXTRAPOLATE);
    private final StringProperty mMiscLineModeProxyProperty = BindingHelper.createStringEnumProxyProperty(mMiscLineModeProperty, ChartMiscLineMode.class);
    private final Preferences mPreferences = NbPreferences.forModule(ChartOptionsManager.class).node("chart");
    private final SessionManager mSessionManager = new SessionManager(mPreferences);

    public static ChartOptionsManager getInstance() {
        return Holder.INSTANCE;
    }

    private ChartOptionsManager() {
        initListeners();
        initBindings();
    }

    public ObjectProperty<MAvgPeriod> avgPeriod1Property() {
        return mAvgPeriod1Property;
    }

    public ObjectProperty<MAvgPeriod> avgPeriod2Property() {
        return mAvgPeriod2Property;
    }

    public BooleanProperty avgPlotDiffProperty() {
        return mAvgPlotDiffProperty;
    }

    public BooleanProperty avgPlotPeriod1Property() {
        return mAvgPlotPeriod1Property;
    }

    public BooleanProperty avgPlotPeriod2Property() {
        return mAvgPlotPeriod2Property;
    }

    public BooleanProperty avgPlotRawProperty() {
        return mAvgPlotRawProperty;
    }

    public BooleanProperty dateEndTodayProperty() {
        return mDateEndTodayProperty;
    }

    public ObjectProperty<ChartStartPoint> datePeriodProperty() {
        return mDatePeriodProperty;
    }

    public BooleanProperty dateResetOnFirstProperty() {
        return mDateResetOnFirstProperty;
    }

    public MAvgPeriod getAvgPeriod1() {
        return mAvgPeriod1Property.get();
    }

    public MAvgPeriod getAvgPeriod2() {
        return mAvgPeriod2Property.get();
    }

    public ChartStartPoint getDatePeriod() {
        return mDatePeriodProperty.get();
    }

    public ChartMiscLineMode getMiscLineModeProperty() {
        return mMiscLineModeProperty.get();
    }

    public boolean isAvgPlotDiff() {
        return mAvgPlotDiffProperty.get();
    }

    public boolean isAvgPlotPeriod1() {
        return mAvgPlotPeriod1Property.get();
    }

    public boolean isAvgPlotPeriod2() {
        return mAvgPlotPeriod2Property.get();
    }

    public boolean isAvgPlotRaw() {
        return mAvgPlotRawProperty.get();
    }

    public boolean isDateEndTodayProperty() {
        return mDateEndTodayProperty.get();
    }

    public boolean isDateResetOnFirst() {
        return mDateResetOnFirstProperty.get();
    }

    public ObjectProperty<ChartMiscLineMode> miscLineModeProperty() {
        return mMiscLineModeProperty;
    }

    private void initBindings() {
        mSessionManager.register("avgPeriod1", mAvgPeriod1ProxyProperty);
        mSessionManager.register("avgPlotRaw", mAvgPlotRawProperty);
        mSessionManager.register("avgPlotDiff", mAvgPlotDiffProperty);
        mSessionManager.register("avgPlotPeriod1", mAvgPlotPeriod1Property);
        mSessionManager.register("avgPlotPeriod2", mAvgPlotPeriod2Property);
        mSessionManager.register("avgPeriod2", mAvgPeriod2ProxyProperty);
        mSessionManager.register("datePeriod", mDatePeriodProxyProperty);
        mSessionManager.register("dateResetOnFirst", mDateResetOnFirstProperty);
        mSessionManager.register("dateEndToday", mDateEndTodayProperty);

        mSessionManager.register("miscLinemode", mMiscLineModeProxyProperty);
    }

    private void initListeners() {
        var r = (Runnable) () -> {
            Mapton.getGlobalState().put(MKey.OBJECT_RESELECT, System.currentTimeMillis());
        };

        ChangeListener listener = (p, o, n) -> {
            r.run();
        };

        mDateResetOnFirstProperty.addListener(listener);
        mAvgPlotRawProperty.addListener(listener);
        mAvgPlotDiffProperty.addListener(listener);
        mAvgPlotPeriod1Property.addListener(listener);
        mAvgPlotPeriod2Property.addListener(listener);
        mDateEndTodayProperty.addListener(listener);
        mDatePeriodProperty.addListener(listener);
        mAvgPeriod1Property.addListener(listener);
        mAvgPeriod2Property.addListener(listener);
        mMiscLineModeProperty.addListener(listener);
    }

    private static class Holder {

        private static final ChartOptionsManager INSTANCE = new ChartOptionsManager();
    }
}
