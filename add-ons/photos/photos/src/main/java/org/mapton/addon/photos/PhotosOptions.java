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
package org.mapton.addon.photos;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import org.mapton.addon.photos.api.SplitBy;
import org.mapton.api.ui.MPresetActions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.almond.util.fx.BindingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class PhotosOptions extends OptionsBase implements MPresetActions {

    public static final Color DEFAULT_GAP_COLOR = Color.BLACK;
    public static final boolean DEFAULT_PLOT_GAP = true;
    public static final boolean DEFAULT_PLOT_TRACK = true;
    public static final SplitBy DEFAULT_SPLIT_BY = SplitBy.MONTH;
    public static final Color DEFAULT_TRACK_COLOR = Color.RED;
    public static final double DEFAULT_WIDTH = 2.0;
    private final ObjectProperty<Color> mGapColorProperty = new SimpleObjectProperty<>(DEFAULT_GAP_COLOR);
    private final BooleanProperty mPlotGapProperty = new SimpleBooleanProperty(DEFAULT_PLOT_GAP);
    private final BooleanProperty mPlotTrackProperty = new SimpleBooleanProperty(DEFAULT_PLOT_TRACK);
    private final ObjectProperty<SplitBy> mSplitByProperty = new SimpleObjectProperty<>(DEFAULT_SPLIT_BY);
    private final StringProperty mSplitByProxyProperty = BindingHelper.createStringEnumProxyProperty(mSplitByProperty, SplitBy.class);
    private final ObjectProperty<Color> mTrackColorProperty = new SimpleObjectProperty<>(DEFAULT_TRACK_COLOR);
    private final DoubleProperty mWidthProperty = new SimpleDoubleProperty(DEFAULT_WIDTH);

    public static PhotosOptions getInstance() {
        return Holder.INSTANCE;
    }

    private PhotosOptions() {
        setPreferences(NbPreferences.forModule(PhotosOptions.class));
    }

    public ObjectProperty<Color> gapColorProperty() {
        return mGapColorProperty;
    }

    public Color getGapColor() {
        return mGapColorProperty.get();
    }

    public SplitBy getSplitBy() {
        return mSplitByProperty.get();
    }

    public Color getTrackColor() {
        return mTrackColorProperty.get();
    }

    public double getWidth() {
        return mWidthProperty.get();
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("plotTrack", mPlotTrackProperty);
        sessionManager.register("plotGap", mPlotGapProperty);
        sessionManager.register("width", mWidthProperty);
        sessionManager.register("gapColor", mGapColorProperty);
        sessionManager.register("trackColor", mTrackColorProperty);
        sessionManager.register("splitBy", mSplitByProxyProperty);
    }

    public boolean isPlotGap() {
        return mPlotGapProperty.get();
    }

    public boolean isPlotTrack() {
        return mPlotTrackProperty.get();
    }

    public BooleanProperty plotGapProperty() {
        return mPlotGapProperty;
    }

    public BooleanProperty plotTrackProperty() {
        return mPlotTrackProperty;
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
        mPlotGapProperty.set(DEFAULT_PLOT_GAP);
        mPlotTrackProperty.set(DEFAULT_PLOT_TRACK);
        mWidthProperty.set(DEFAULT_WIDTH);
        mSplitByProperty.set(DEFAULT_SPLIT_BY);
        mGapColorProperty.set(DEFAULT_GAP_COLOR);
        mTrackColorProperty.set(DEFAULT_TRACK_COLOR);
    }

    public ObjectProperty<SplitBy> splitByProperty() {
        return mSplitByProperty;
    }

    public ObjectProperty<Color> trackColorProperty() {
        return mTrackColorProperty;
    }

    public DoubleProperty widthProperty() {
        return mWidthProperty;
    }

    private static class Holder {

        private static final PhotosOptions INSTANCE = new PhotosOptions();
    }
}
