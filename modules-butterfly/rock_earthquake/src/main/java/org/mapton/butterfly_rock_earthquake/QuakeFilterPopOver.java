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
package org.mapton.butterfly_rock_earthquake;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDate.DateElement;
import org.mapton.butterfly_core.api.BFilterSectionMisc;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BFilterSectionPoint.PointElement;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class QuakeFilterPopOver extends BaseTabbedFilterPopOver {

    private final QuakeFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final BFilterSectionMisc mFilterSectionMisc;
    private final BFilterSectionPoint mFilterSectionPoint;
    private final QuakeManager mManager = QuakeManager.getInstance();

    public QuakeFilterPopOver(QuakeFilter filter) {
        mFilterSectionPoint = new BFilterSectionPoint();
        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionMisc = new BFilterSectionMisc(filter);

        mFilter = filter;
        mFilter.setFilterSection(mFilterSectionPoint);
        mFilter.setFilterSection(mFilterSectionDate);

        setFilter(filter);
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()).node(getClass().getSimpleName()));

        populate();

        load(mManager.butterflyProperty().get());
        mManager.butterflyProperty().addListener((p, o, n) -> {
            load(n);
        });
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        mFilterSectionPoint.clear();
        mFilterSectionDate.clear();
        mFilterSectionMisc.clear();
    }

    @Override
    public void presetRestore(Preferences preferences) {
        clear();
        presetStore(preferences);
    }

    @Override
    public void presetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            var quakes = butterfly.rock().getEarthquakes();
            mFilterSectionPoint.load(quakes);
            mFilterSectionDate.load(mManager.getTemporalRange());
            mFilterSectionMisc.load();
        } catch (Exception e) {
            System.out.println("QuakeFilterPopOver: Butterfly==null");
        }
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
        if (getFilterPresetPopOver().restoreDefaultIfExists()) {
            //
        } else {
            mFilterSectionPoint.reset(null);
            mFilterSectionMisc.reset(null);
        }
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
                PointElement.FREQUENCY_DEFAULT,
                PointElement.FREQUENCY_DEFAULT_STAT,
                PointElement.FREQUENCY_INTENSE,
                PointElement.FREQUENCY_INTENSE_STAT,
                PointElement.ALARM_STAT,
                PointElement.MEAS_MODE,
                PointElement.MEAS_MODE_SUB,
                PointElement.UNIT_DIFF,
                PointElement.FORMULA_ROLLING,
                PointElement.FORMULA_SPARSE,
                PointElement.MEAS_NEXT
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
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        return sessionManager;
    }

}
