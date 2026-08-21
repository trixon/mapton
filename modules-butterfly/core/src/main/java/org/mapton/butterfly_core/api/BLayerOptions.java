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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BLayerOptions<T> extends OptionsBase {

    public static final String DEFAULT_GRAPHICS = "";
    public static final boolean DEFAULT_PLOT_ALARM = false;
    public static final boolean DEFAULT_PLOT_ANNOTATION = false;
    public static final boolean DEFAULT_PLOT_DEBT = false;
    public static final int DEFAULT_PLOT_DISTANCE = 15;
    public static final boolean DEFAULT_PLOT_SELECTED = false;
    public static final boolean DEFAULT_PLOT_SELECTED_PLUS = false;
    public static final boolean DEFAULT_PLOT_WATCHLIST_CHANGES = false;
    public static final boolean DEFAULT_PLOT_WATCHLIST_MEMBER = false;
    public static final BTrendPeriod DEFAULT_TREND_PERIOD_A = BTrendPeriod.MONTH;
    public static final BTrendPeriod DEFAULT_TREND_PERIOD_B = BTrendPeriod.ZERO;
    public static final BTrendPeriod DEFAULT_TREND_PERIOD_C = BTrendPeriod.QUARTER;
    private StringProperty mColorByProxyProperty;
    private final StringProperty mGraphicsProperty = new SimpleStringProperty(DEFAULT_GRAPHICS);
    private final SimpleObjectProperty<LabelBy.Operations> mLabelByOperationProperty = new SimpleObjectProperty<>();
    private StringProperty mLabelByProxyProperty;
    private BooleanProperty mPlotAlarmProperty = new SimpleBooleanProperty(DEFAULT_PLOT_ALARM);
    private final BooleanProperty mPlotAnnotationProperty = new SimpleBooleanProperty(DEFAULT_PLOT_ANNOTATION);
    private BooleanProperty mPlotDebtProperty = new SimpleBooleanProperty(DEFAULT_PLOT_DEBT);
    private final IntegerProperty mPlotDistanceProperty = new SimpleIntegerProperty(DEFAULT_PLOT_DISTANCE);
    private final BooleanProperty mPlotSelectedPlusProperty = new SimpleBooleanProperty(DEFAULT_PLOT_SELECTED_PLUS);
    private final BooleanProperty mPlotSelectedProperty = new SimpleBooleanProperty(DEFAULT_PLOT_SELECTED);
    private BooleanProperty mPlotWatchlistChangesProperty = new SimpleBooleanProperty(DEFAULT_PLOT_WATCHLIST_CHANGES);
    private BooleanProperty mPlotWatchlistMemberProperty = new SimpleBooleanProperty(DEFAULT_PLOT_WATCHLIST_MEMBER);
    private StringProperty mPointByProxyProperty;
    private final ObjectProperty<BTrendPeriod> mTrendPeriodAProperty = new SimpleObjectProperty<>(DEFAULT_TREND_PERIOD_A);
    private StringProperty mTrendPeriodAProxyProperty;
    private final ObjectProperty<BTrendPeriod> mTrendPeriodBProperty = new SimpleObjectProperty<>(DEFAULT_TREND_PERIOD_B);
    private StringProperty mTrendPeriodBProxyProperty;
    private final ObjectProperty<BTrendPeriod> mTrendPeriodCProperty = new SimpleObjectProperty<>(DEFAULT_TREND_PERIOD_C);
    private StringProperty mTrendPeriodCProxyProperty;

    public BLayerOptions() {
        initTrendPeriodProxyProperties();
    }

    public StringProperty colorByProxyProperty() {
        return mColorByProxyProperty;
    }

    public void disablePlotAlarm() {
        mPlotAlarmProperty = null;
    }

    public void disablePlotDebt() {
        mPlotDebtProperty = null;
    }

    public void disablePlotWatchlistChanges() {
        mPlotWatchlistChangesProperty = null;
    }

    public void disablePlotWatchlistMember() {
        mPlotWatchlistMemberProperty = null;
    }

    public String getGraphics() {
        return mGraphicsProperty.get();
    }

    public String getLabelFromId(Class enumClass, String enumName, LabelBy.Operations defaultValue) {
        try {
            Enum a = Enum.valueOf(enumClass, enumName);
            LabelBy.Operations b = (LabelBy.Operations) a;
            return b.getFullName();
        } catch (IllegalArgumentException e) {
            return defaultValue.getFullName();
        }
    }

    public int getPlotDistance() {
        return mPlotDistanceProperty.get();
    }

    public Preferences getPreferencesForPath(String subModule) {
        return NbPreferences.forModule(getClass()).node("layerOptionPresets" + subModule);
    }

    public Preferences getPreferencesForPath() {
        return getPreferencesForPath("");
    }

    public BTrendPeriod getTrendPeriodA() {
        return mTrendPeriodAProperty.get();
    }

    public BTrendPeriod getTrendPeriodB() {
        return mTrendPeriodBProperty.get();
    }

    public BTrendPeriod getTrendPeriodCX() {
        return mTrendPeriodCProperty.get();
    }

    public StringProperty graphicsProperty() {
        return mGraphicsProperty;
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        var prefix = "common.";
        sessionManager.register(prefix + "pointBy", mPointByProxyProperty);
        sessionManager.register(prefix + "colorBy", mColorByProxyProperty);
        sessionManager.register(prefix + "labelBy", mLabelByProxyProperty);
        sessionManager.register(prefix + "graphics", mGraphicsProperty);
        sessionManager.register(prefix + "trendPeriondA", mTrendPeriodAProxyProperty);
        sessionManager.register(prefix + "trendPeriondB", mTrendPeriodBProxyProperty);
        sessionManager.register(prefix + "trendPeriondC", mTrendPeriodCProxyProperty);
        sessionManager.register(prefix + "plotAnnotatiion", mPlotAnnotationProperty);
        sessionManager.register(prefix + "plotSelected", mPlotSelectedProperty);
        sessionManager.register(prefix + "plotSelectedPlus", mPlotSelectedPlusProperty);
        sessionManager.register(prefix + "plotSelectedDistance", mPlotDistanceProperty);

        if (mPlotDebtProperty != null) {
            sessionManager.register(prefix + "plotDebt", mPlotDebtProperty);
        }
        if (mPlotWatchlistMemberProperty != null) {
            sessionManager.register(prefix + "plotWatchlistMember", mPlotWatchlistMemberProperty);
        }
        if (mPlotWatchlistChangesProperty != null) {
            sessionManager.register(prefix + "plotWatchlistChanges", mPlotWatchlistChangesProperty);
        }
        if (mPlotAlarmProperty != null) {
            sessionManager.register(prefix + "plotAlarm", mPlotAlarmProperty);
        }
    }

    public boolean isPlotAlarm() {
        return mPlotAlarmProperty != null && mPlotAlarmProperty.get();
    }

    public boolean isPlotAnnotation() {
        return mPlotAnnotationProperty.get();
    }

    public boolean isPlotDebt() {
        return mPlotDebtProperty != null && mPlotDebtProperty.get();
    }

    public boolean isPlotSelected() {
        return mPlotSelectedProperty.get();
    }

    public boolean isPlotSelectedPlus() {
        return mPlotSelectedPlusProperty.get();
    }

    public boolean isPlotWatchlistChanges() {
        return mPlotWatchlistChangesProperty != null && mPlotWatchlistChangesProperty.get();
    }

    public boolean isPlotWatchlistMember() {
        return mPlotWatchlistMemberProperty != null && mPlotWatchlistMemberProperty.get();
    }

    public SimpleObjectProperty<LabelBy.Operations> labelByOperationProperty() {
        return mLabelByOperationProperty;
    }

    public StringProperty labelByProxyProperty() {
        return mLabelByProxyProperty;
    }

    public BooleanProperty plotAlarmProperty() {
        return mPlotAlarmProperty;
    }

    public BooleanProperty plotAnnotationProperty() {
        return mPlotAnnotationProperty;
    }

    public BooleanProperty plotDebtProperty() {
        return mPlotDebtProperty;
    }

    public IntegerProperty plotDistanceProperty() {
        return mPlotDistanceProperty;
    }

    public BooleanProperty plotSelectedPlusProperty() {
        return mPlotSelectedPlusProperty;
    }

    public BooleanProperty plotSelectedProperty() {
        return mPlotSelectedProperty;
    }

    public BooleanProperty plotWatchlistChanges() {
        return mPlotWatchlistChangesProperty;
    }

    public BooleanProperty plotWatchlistMember() {
        return mPlotWatchlistMemberProperty;
    }

    public StringProperty pointByProxyProperty() {
        return mPointByProxyProperty;
    }

    public void registerLayerBundle(LayerBundle layerBundle) {
        getPreferences().addPreferenceChangeListener(pce -> {
            SwingHelper.runLaterDelayed(50, () -> {
                layerBundle.resetPaintDelayedResetRunner();
//                layerBundle.repaint();
            });
        });
    }

    public void reset() {
        mGraphicsProperty.set(DEFAULT_GRAPHICS);
        mPlotSelectedProperty.set(DEFAULT_PLOT_SELECTED);
        mPlotSelectedPlusProperty.set(DEFAULT_PLOT_SELECTED_PLUS);
        mPlotDistanceProperty.set(DEFAULT_PLOT_DISTANCE);
        if (mPlotAnnotationProperty != null) {
            mPlotAnnotationProperty.set(DEFAULT_PLOT_ANNOTATION);
        }
        if (mPlotWatchlistMemberProperty != null) {
            mPlotWatchlistMemberProperty.set(DEFAULT_PLOT_WATCHLIST_MEMBER);
        }
        if (mPlotWatchlistChangesProperty != null) {
            mPlotWatchlistChangesProperty.set(DEFAULT_PLOT_WATCHLIST_CHANGES);
        }
        if (mPlotDebtProperty != null) {
            mPlotDebtProperty.set(DEFAULT_PLOT_DEBT);
        }
        if (mPlotAlarmProperty != null) {
            mPlotAlarmProperty.set(DEFAULT_PLOT_ALARM);
        }
    }

    public ObjectProperty<BTrendPeriod> trendPeriodAProperty() {
        return mTrendPeriodAProperty;
    }

    public ObjectProperty<BTrendPeriod> trendPeriodBProperty() {
        return mTrendPeriodBProperty;
    }

    public ObjectProperty<BTrendPeriod> trendPeriodCProperty() {
        return mTrendPeriodCProperty;
    }

    protected <E extends Enum<E>> void initColorProxyProperty(ObjectProperty<E> objectProperty, Class<E> enumClass) {
        mColorByProxyProperty = BindingHelper.createStringEnumProxyProperty(objectProperty, enumClass);
    }

    protected <E extends Enum<E>> void initLabelProxyProperty(ObjectProperty<E> objectProperty, Class<E> enumClass) {
        mLabelByProxyProperty = BindingHelper.createStringEnumProxyProperty(objectProperty, enumClass);
    }

    protected <E extends Enum<E>> void initPointProxyProperty(ObjectProperty<E> objectProperty, Class<E> enumClass) {
        mPointByProxyProperty = BindingHelper.createStringEnumProxyProperty(objectProperty, enumClass);
    }

    protected void initTrendPeriodProxyProperties() {
        mTrendPeriodAProxyProperty = BindingHelper.createStringEnumProxyProperty(mTrendPeriodAProperty, BTrendPeriod.class);
        mTrendPeriodBProxyProperty = BindingHelper.createStringEnumProxyProperty(mTrendPeriodBProperty, BTrendPeriod.class);
        mTrendPeriodCProxyProperty = BindingHelper.createStringEnumProxyProperty(mTrendPeriodCProperty, BTrendPeriod.class);
    }
}
