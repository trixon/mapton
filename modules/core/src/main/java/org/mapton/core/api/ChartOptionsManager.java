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
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.BindingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ChartOptionsManager {

    private final BooleanProperty mDateEndTodayProperty = new SimpleBooleanProperty();
    private final ObjectProperty<ChartStartPoint> mDatePeriodProperty = new SimpleObjectProperty<>(ChartStartPoint.ZERO);
    private final StringProperty mDatePeriodProxyProperty = BindingHelper.createStringEnumProxyProperty(mDatePeriodProperty, ChartStartPoint.class);
    private final BooleanProperty mDateResetOnFirst = new SimpleBooleanProperty();
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

    public BooleanProperty dateEndTodayProperty() {
        return mDateEndTodayProperty;
    }

    public ObjectProperty<ChartStartPoint> datePeriodProperty() {
        return mDatePeriodProperty;
    }

    public BooleanProperty dateResetOnFirst() {
        return mDateResetOnFirst;
    }

    public ChartStartPoint getDatePeriod() {
        return mDatePeriodProperty.get();
    }

    public ChartMiscLineMode getMiscLineModeProperty() {
        return mMiscLineModeProperty.get();
    }

    public boolean isDateEndTodayProperty() {
        return mDateEndTodayProperty.get();
    }

    public boolean isDateResetOnFirst() {
        return mDateResetOnFirst.get();
    }

    public ObjectProperty<ChartMiscLineMode> miscLineModeProperty() {
        return mMiscLineModeProperty;
    }

    private void initBindings() {
        mSessionManager.register("datePeriod", mDatePeriodProxyProperty);
        mSessionManager.register("dateResetOnFirst", mDateResetOnFirst);
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

        mDateResetOnFirst.addListener(listener);
        mDateEndTodayProperty.addListener(listener);
        mDatePeriodProperty.addListener(listener);
        mMiscLineModeProperty.addListener(listener);
    }

    private static class Holder {

        private static final ChartOptionsManager INSTANCE = new ChartOptionsManager();
    }
}
