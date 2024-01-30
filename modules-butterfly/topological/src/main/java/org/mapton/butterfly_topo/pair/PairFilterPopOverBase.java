/*
 * Copyright 2024 Patrik Karlström.
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
public abstract class PairFilterPopOverBase extends BaseFilterPopOver<TopoFilterFavorite> {

    private final ResourceBundle mBundle = NbBundle.getBundle(PairManagerBase.class);
    protected final PairFilterConfig mConfig;
    protected final RangeSliderPane mDabbaHRangeSlider;
    protected final RangeSliderPane mDabbaRRangeSlider;
    protected final RangeSliderPane mDeltaHRangeSlider;
    protected final RangeSliderPane mDeltaRRangeSlider;
    protected final PairFilterBase mFilter;
    protected final RangeSliderPane mGradeHorizontalRangeSlider;
    protected final RangeSliderPane mGradeVerticalRangeSlider;

    public PairFilterPopOverBase(PairFilterBase filter, PairFilterConfig config) {
        mFilter = filter;
        mConfig = config;

        mDeltaHRangeSlider = new RangeSliderPane(mBundle.getString("deltaH"), mConfig.getMaxDeltaH());
        mDeltaRRangeSlider = new RangeSliderPane(mBundle.getString("deltaR"), mConfig.getMaxDeltaR());
        mDabbaHRangeSlider = new RangeSliderPane(mBundle.getString("dabbaH"), mConfig.getMaxDabbaH());
        mDabbaRRangeSlider = new RangeSliderPane(mBundle.getString("dabbaR"), mConfig.getMaxDabbaR());
        mGradeVerticalRangeSlider = new RangeSliderPane(mBundle.getString("gradeV"), mConfig.getMaxGradeVertical());
        mGradeHorizontalRangeSlider = new RangeSliderPane(mBundle.getString("gradeH"), mConfig.getMaxGradeHorizontal());

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

        mFilter.mDabbaHSelectedProperty.bind(mDabbaHRangeSlider.selectedProperty());
        mFilter.mDabbaHMinProperty.bind(mDabbaHRangeSlider.minProperty());
        mFilter.mDabbaHMaxProperty.bind(mDabbaHRangeSlider.maxProperty());

        mFilter.mDabbaRSelectedProperty.bind(mDabbaRRangeSlider.selectedProperty());
        mFilter.mDabbaRMinProperty.bind(mDabbaRRangeSlider.minProperty());
        mFilter.mDabbaRMaxProperty.bind(mDabbaRRangeSlider.maxProperty());

        mFilter.mGradeHorizontalSelectedProperty.bind(mGradeHorizontalRangeSlider.selectedProperty());
        mFilter.mGradeHorizontalMinProperty.bind(mGradeHorizontalRangeSlider.minProperty());
        mFilter.mGradeHorizontalMaxProperty.bind(mGradeHorizontalRangeSlider.maxProperty());

        mFilter.mGradeVerticalSelectedProperty.bind(mGradeVerticalRangeSlider.selectedProperty());
        mFilter.mGradeVerticalMinProperty.bind(mGradeVerticalRangeSlider.minProperty());
        mFilter.mGradeVerticalMaxProperty.bind(mGradeVerticalRangeSlider.maxProperty());

        mFilter.initPropertyListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("freeText", mFilter.freeTextProperty());

        mDeltaHRangeSlider.initSession("DeltaH" + mConfig.getKeyPrefix(), sessionManager);
        mDeltaRRangeSlider.initSession("DeltaR" + mConfig.getKeyPrefix(), sessionManager);
        mDabbaHRangeSlider.initSession("DabbaH" + mConfig.getKeyPrefix(), sessionManager);
        mDabbaRRangeSlider.initSession("DabbaR" + mConfig.getKeyPrefix(), sessionManager);
        mGradeVerticalRangeSlider.initSession("GradeV" + mConfig.getKeyPrefix(), sessionManager);
        mGradeHorizontalRangeSlider.initSession("GradeH" + mConfig.getKeyPrefix(), sessionManager);
    }

}