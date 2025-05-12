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
package org.mapton.butterfly_acoustic.blast;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDate.DateElement;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BFilterSectionPoint.PointElement;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.FilterSectionMisc;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.control.RangeSliderPane;

/**
 *
 * @author Patrik Karlström
 */
public class BlastFilterPopOver extends BaseTabbedFilterPopOver {

    private final RangeSliderPane mAltitudeRangeSlider = new RangeSliderPane("Z", -100.0, 100.0, false);
    private final BlastFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final FilterSectionMisc mFilterSectionMisc;
    private final BFilterSectionPoint mFilterSectionPoint;
    private final BlastManager mManager = BlastManager.getInstance();

    public BlastFilterPopOver(BlastFilter filter) {
        mFilterSectionPoint = new BFilterSectionPoint();
        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionMisc = new FilterSectionMisc();

        mFilter = filter;
        mFilter.setFilterSection(mFilterSectionPoint);
        mFilter.setFilterSection(mFilterSectionDate);

        setFilter(filter);
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()).node(getClass().getSimpleName()));

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        mFilterSectionPoint.clear();
        mFilterSectionDate.clear();
        mFilterSectionMisc.clear();

        mAltitudeRangeSlider.clear();
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        var blasts = butterfly.noise().getBlasts();

        mFilterSectionPoint.load(blasts);
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();

        var minZ = blasts.stream().filter(b -> b.getZeroZ() != null).mapToDouble(b -> b.getZeroZ()).min().orElse(-100d);
        var maxZ = blasts.stream().filter(b -> b.getZeroZ() != null).mapToDouble(b -> b.getZeroZ()).max().orElse(100d);
        mAltitudeRangeSlider.setMinMaxValue(minZ - 1, maxZ + 1);
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        mFilterSectionPoint.onShownFirstTime();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");

        mFilterSectionPoint.reset(null);
        mFilterSectionMisc.reset(null);
    }

    private void createUI() {
        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        populateToolBar(mFilterSectionMisc.getInvertCheckboxToolBarItem());

        getTabPane().getTabs().addAll(
                mFilterSectionPoint.getTab(),
                mFilterSectionDate.getTab()
        );

        setContentNode(root);

        mFilterSectionPoint.disable(
                PointElement.ALARM,
                PointElement.CATEGORY,
                PointElement.FREQUENCY,
                PointElement.FREQUENCY_DEFAULT,
                PointElement.FREQUENCY_DEFAULT_STAT,
                PointElement.MEAS_MODE,
                PointElement.MEAS_NEXT,
                PointElement.STATUS
        );

        mFilterSectionDate.disable(
                DateElement.FIRST,
                DateElement.HAS_FROM_TO
        );
    }

    private void initListeners() {
        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
        });

        mFilterSectionMisc.initListeners(mFilter);

        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.initCheckModelListeners();

        mFilter.mAltitudeSelectedProperty.bind(mAltitudeRangeSlider.selectedProperty());
        mFilter.mAltitudeMinProperty.bind(mAltitudeRangeSlider.minProperty());
        mFilter.mAltitudeMaxProperty.bind(mAltitudeRangeSlider.maxProperty());

        mFilter.initCheckModelListeners();
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        mAltitudeRangeSlider.initSession("altitude", sessionManager);
        sessionManager.register("filter.blast.freeText", mFilter.freeTextProperty());

        return sessionManager;
    }

}
