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
package org.mapton.butterfly_structural.strain;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import org.mapton.api.ui.forms.MFilterSectionDate;
import org.mapton.api.ui.forms.MFilterSectionDisruptor;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.FilterSectionMisc;
import org.mapton.butterfly_core.api.FilterSectionPoint;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class StrainFilterPopOver extends BaseTabbedFilterPopOver {

    private final ResourceBundle mBundle = NbBundle.getBundle(StrainFilterPopOver.class);
    private final StrainFilter mFilter;
    private final MFilterSectionDate mFilterSectionDate;
    private final MFilterSectionDisruptor mFilterSectionDisruptor;
    private final FilterSectionMisc mFilterSectionMisc;
    private final FilterSectionPoint mFilterSectionPoint;
    private final StrainManager mManager = StrainManager.getInstance();

    public StrainFilterPopOver(StrainFilter filter) {
        mFilterSectionPoint = new FilterSectionPoint();
        mFilterSectionDate = new MFilterSectionDate();
        mFilterSectionDisruptor = new MFilterSectionDisruptor();
        mFilterSectionMisc = new FilterSectionMisc();

        mFilter = filter;
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
        mFilterSectionDisruptor.clear();
        mFilterSectionMisc.clear();
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
        //mDateRangePane.reset();
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.structural().getStrainPoints();

        mFilterSectionPoint.load(items);
        mFilterSectionDisruptor.load();
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();
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
                mFilterSectionDate.getTab(),
                mFilterSectionDisruptor.getTab()
        );

        setContentNode(root);
    }

    private void initListeners() {
        activateCopyNames(actionEvent -> {
            var names = mManager.getTimeFilteredItems().stream().map(o -> o.getName()).toList();
            copyNames(names);
        });

        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
        });

        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilterSectionPoint.initListeners(mFilter);
        mFilterSectionDate.initListeners(mFilter);
        mFilterSectionDisruptor.initListeners(mFilter);
        mFilterSectionMisc.initListeners(mFilter);

        mFilter.sectionPointProperty().bind(mFilterSectionPoint.selectedProperty());
        mFilter.sectionDateProperty().bind(mFilterSectionDate.selectedProperty());
        mFilter.sectionDisruptorProperty().bind(mFilterSectionDisruptor.selectedProperty());

        mFilter.initCheckModelListeners();
//                mFilter.sectionDateProperty().bind(mFilterSectionDate.selectedProperty());
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionDisruptor.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        sessionManager.register("filter.measPoint.freeText", mFilter.freeTextProperty());

        return sessionManager;
    }

}
