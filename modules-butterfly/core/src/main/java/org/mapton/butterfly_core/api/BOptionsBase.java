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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.mapton.worldwind.api.LayerBundle;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BOptionsBase extends OptionsBase {

    public static final String DEFAULT_GRAPHICS = "";
    public static final boolean DEFAULT_PLOT_DEBT = false;
    public static final int DEFAULT_PLOT_DISTANCE = 15;
    public static final boolean DEFAULT_PLOT_SELECTED = false;
    public static final boolean DEFAULT_PLOT_SELECTED_PLUS = false;
    private StringProperty mColorByProxyProperty;
    private final StringProperty mGraphicsProperty = new SimpleStringProperty(DEFAULT_GRAPHICS);
    private StringProperty mLabelByProxyProperty;
    private final BooleanProperty mPlotDebtProperty = new SimpleBooleanProperty(DEFAULT_PLOT_DEBT);
    private final IntegerProperty mPlotDistanceProperty = new SimpleIntegerProperty(DEFAULT_PLOT_DISTANCE);
    private final BooleanProperty mPlotSelectedPlusProperty = new SimpleBooleanProperty(DEFAULT_PLOT_SELECTED_PLUS);
    private final BooleanProperty mPlotSelectedProperty = new SimpleBooleanProperty(DEFAULT_PLOT_SELECTED);
    private StringProperty mPointByProxyProperty;

    public BOptionsBase() {
    }

    public StringProperty getColorByProxyProperty() {
        return mColorByProxyProperty;
    }

    public String getGraphics() {
        return mGraphicsProperty.get();
    }

    public StringProperty getLabelByProxyProperty() {
        return mLabelByProxyProperty;
    }

    public int getPlotDistance() {
        return mPlotDistanceProperty.get();
    }

    public StringProperty getPointByProxyProperty() {
        return mPointByProxyProperty;
    }

    public StringProperty graphicsProperty() {
        return mGraphicsProperty;
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register(getKeyOptions("pointBy"), mPointByProxyProperty);
        sessionManager.register(getKeyOptions("colorBy"), mColorByProxyProperty);
        sessionManager.register(getKeyOptions("labelBy"), mLabelByProxyProperty);
        sessionManager.register(getKeyOptions("graphics"), mGraphicsProperty);
        sessionManager.register(getKeyOptions("plotDebt"), mPlotDebtProperty);
        sessionManager.register(getKeyOptions("plotSelected"), mPlotSelectedProperty);
        sessionManager.register(getKeyOptions("plotSelectedPlus"), mPlotSelectedPlusProperty);
        sessionManager.register(getKeyOptions("plotSelectedDistance"), mPlotDistanceProperty);
    }

    public boolean isPlotDebt() {
        return mPlotDebtProperty.get();
    }

    public boolean isPlotSelected() {
        return mPlotSelectedProperty.get();
    }

    public boolean isPlotSelectedPlus() {
        return mPlotSelectedPlusProperty.get();
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
        mPlotDebtProperty.set(DEFAULT_PLOT_DEBT);
        mPlotSelectedProperty.set(DEFAULT_PLOT_SELECTED);
        mPlotSelectedPlusProperty.set(DEFAULT_PLOT_SELECTED_PLUS);
        mPlotDistanceProperty.set(DEFAULT_PLOT_DISTANCE);
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

}
