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
package org.mapton.butterfly_topo.pair;

import java.util.ResourceBundle;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo.TopoFilterFavorite;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class Pair1FilterPopOver extends BaseFilterPopOver<TopoFilterFavorite> {

    private final Pair1Filter mFilter;
    private final ResourceBundle mBundle = NbBundle.getBundle(Pair1PropertiesBuilder.class);
    private final RangeSliderPane mDeltaHRangeSlider = new RangeSliderPane(mBundle.getString("deltaH"), 50.0);
    private final RangeSliderPane mDeltaRRangeSlider = new RangeSliderPane(mBundle.getString("deltaR"), Pair1Manager.MAX_RADIAL_DISTANCE);
    private final RangeSliderPane mDabbaHRangeSlider = new RangeSliderPane(mBundle.getString("dabbaH"), 2.0);
    private final RangeSliderPane mDabbaRRangeSlider = new RangeSliderPane(mBundle.getString("dabbaR"), 2.0);
    private final RangeSliderPane mGradeVerticalRangeSlider = new RangeSliderPane(mBundle.getString("gradeV"), 2.0);
    private final RangeSliderPane mGradeHorizontalRangeSlider = new RangeSliderPane(mBundle.getString("gradeH"), 2.0);

    public Pair1FilterPopOver(Pair1Filter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mDeltaHRangeSlider.clear();
        mDeltaRRangeSlider.clear();
        mDabbaHRangeSlider.clear();
        mDabbaRRangeSlider.clear();
        mGradeVerticalRangeSlider.clear();
        mGradeHorizontalRangeSlider.clear();
    }

    @Override
    public void load(Butterfly butterfly) {
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mDeltaHRangeSlider,
                new Separator(),
                mDeltaRRangeSlider,
                new Separator(),
                mDabbaHRangeSlider,
                new Separator(),
                mDabbaRRangeSlider,
                new Separator(),
                mGradeVerticalRangeSlider,
                new Separator(),
                mGradeHorizontalRangeSlider
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());
        mFilter.mDeltaHSelectedProperty.bind(mDeltaHRangeSlider.selectedProperty());
        mFilter.mDeltaHMinProperty.bind(mDeltaHRangeSlider.minProperty());
        mFilter.mDeltaHMaxProperty.bind(mDeltaHRangeSlider.maxProperty());

        mFilter.mDeltaRSelectedProperty.bind(mDeltaRRangeSlider.selectedProperty());
        mFilter.mDeltaRMinProperty.bind(mDeltaRRangeSlider.minProperty());
        mFilter.mDeltaRMaxProperty.bind(mDeltaRRangeSlider.maxProperty());

        mFilter.initPropertyListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("freeText", mFilter.freeTextProperty());

        mDeltaHRangeSlider.initSession("DeltaH", sessionManager);
        mDeltaRRangeSlider.initSession("DeltaR", sessionManager);
        mDabbaHRangeSlider.initSession("DabbaH", sessionManager);
        mDabbaRRangeSlider.initSession("DabbaR", sessionManager);
        mGradeVerticalRangeSlider.initSession("GradeV", sessionManager);
        mGradeHorizontalRangeSlider.initSession("GradeH", sessionManager);
    }

}
