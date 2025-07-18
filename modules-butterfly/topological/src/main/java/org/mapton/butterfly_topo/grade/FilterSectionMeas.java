/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.butterfly_topo.xdev.BSubFilterMeasAlarmLevel;
import org.openide.util.NbBundle;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.control.SliderPane;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    private final GradeFilterConfig mConfig;
    private final RangeSliderPane mDabbaHRangeSlider;
    private final RangeSliderPane mDabbaRRangeSlider;
    private final RangeSliderPane mDeltaHRangeSlider;
    private final RangeSliderPane mDeltaRRangeSlider;
    private final BDimension mDimension;
    private final BSubFilterMeasAlarmLevel<BTopoGrade> mFilterMeasAlarmLevel;
    private final SliderPane mGradeHorizontalSlider;
    private final SliderPane mGradeVerticalSlider;
    private final MeasFilterUI mMeasFilterUI;

    public FilterSectionMeas(GradeFilterConfig config) {
        super(SDict.MEASUREMENTS.toString());
        mConfig = config;
        mDimension = config.getDimension();

        mDeltaHRangeSlider = new RangeSliderPane(mBundle.getString("filterDeltaH"), mConfig.getMinDeltaH(), mConfig.getMaxDeltaH());
        mDeltaRRangeSlider = new RangeSliderPane(mBundle.getString("filterDeltaR"), 0.0, mConfig.getMaxDeltaR());
        mDabbaHRangeSlider = new RangeSliderPane(mBundle.getString("filterDabbaH"), 0.0, mConfig.getMaxDabbaH());
        mDabbaRRangeSlider = new RangeSliderPane(mBundle.getString("filterDabbaR"), 0.0, mConfig.getMaxDabbaR());
        mGradeHorizontalSlider = new SliderPane(mBundle.getString("filterGradeHPerMille"), mConfig.getMinGradeHorizontal());
        mGradeVerticalSlider = new SliderPane(mBundle.getString("filterGradeVPerMille"), mConfig.getMinGradeVertical());
        mFilterMeasAlarmLevel = new BSubFilterMeasAlarmLevel();
        mFilterMeasAlarmLevel.setAlarmLevelFunction(p -> {
            return TopoHelper.getAlarmLevelHeight(p);
        });
        mFilterMeasAlarmLevel.setAlarmAgeFunction(p -> {
            var historicLevels = p.getCommonObservations().entrySet().stream()
                    .mapToInt(entry -> {
                        var o = entry.getValue();
                        var gradeDiff = p.ext().getDiff(p.getFirstObservation(), o);
                        return p.ext().getAlarmLevelHeight(Math.abs(gradeDiff.getZQuota()));
                    })
                    .distinct()
                    .count();

            if (historicLevels < 2) {
                return null;
            }

            var value = p.ext().getAlarmLevelAgeGrade();
            if (value != null) {
                return value.intValue();
            } else {
                return null;
            }
        });

        mMeasFilterUI = new MeasFilterUI();

        //TODO Make this dynamic on actual load
        mFilterMeasAlarmLevel.load(null);

        setContent(mMeasFilterUI.getRoot());
    }

    @Override
    public void clear() {
        super.clear();

        mDeltaHRangeSlider.clear();
        mDeltaRRangeSlider.clear();
        mDabbaHRangeSlider.clear();
        mDabbaRRangeSlider.clear();
        mGradeVerticalSlider.clear();
        mGradeHorizontalSlider.clear();
        mFilterMeasAlarmLevel.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toUpper(), "TODO");
        mFilterMeasAlarmLevel.createInfoContent(map);
//        map.put("Period " + Dict.FROM.toString(), levelPeriodDateLowProperty().get() != null ? levelPeriodDateLowProperty().get().toString() : "");
//        map.put("Period " + Dict.TO.toString(), levelPeriodDateHighProperty().get() != null ? levelPeriodDateHighProperty().get().toString() : "");
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mGradeHorizontalSlider.selectedProperty(),
                mGradeHorizontalSlider.valueProperty(),
                mGradeVerticalSlider.selectedProperty(),
                mGradeVerticalSlider.valueProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));
        List.of(
                mDeltaRRangeSlider,
                mDeltaHRangeSlider,
                mDabbaHRangeSlider,
                mDabbaRRangeSlider
        ).forEach(rangeSlider -> {
            rangeSlider.selectedProperty().addListener(changeListener);
            rangeSlider.maxProperty().addListener(changeListener);
            rangeSlider.minProperty().addListener(changeListener);
        });

        mFilterMeasAlarmLevel.initListeners(changeListener, listChangeListener);
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.meas", selectedProperty());
        mDeltaHRangeSlider.initSession("DeltaH" + mConfig.getKeyPrefix(), sessionManager);
        mDeltaRRangeSlider.initSession("DeltaR" + mConfig.getKeyPrefix(), sessionManager);
        mDabbaHRangeSlider.initSession("DabbaH" + mConfig.getKeyPrefix(), sessionManager);
        mDabbaRRangeSlider.initSession("DabbaR" + mConfig.getKeyPrefix(), sessionManager);
        mGradeVerticalSlider.initSession("GradeV" + mConfig.getKeyPrefix(), sessionManager);
        mGradeHorizontalSlider.initSession("GradeH" + mConfig.getKeyPrefix(), sessionManager);

        mFilterMeasAlarmLevel.initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
        mMeasFilterUI.onShownFirstTime();
        mFilterMeasAlarmLevel.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        if (filterConfig != null) {
            mMeasFilterUI.reset(filterConfig);
        }
    }

    boolean filter(BTopoGrade p) {
        if (isSelected()) {
            return validateRangeSliderPane(mDabbaHRangeSlider, p.ext().getDiff().getPartialDiffZ() * 1000)
                    && validateRangeSliderPane(mDabbaRRangeSlider, p.ext().getDiff().getPartialDiffR() * 1000)
                    && validateRangeSliderPane(mDeltaHRangeSlider, p.getDistanceHeight())
                    && validateRangeSliderPane(mDeltaRRangeSlider, p.getDistancePlane())
                    && validateSliderPane(mGradeHorizontalSlider, Math.abs(p.ext().getDiff().getZPerMille()))
                    && validateSliderPane(mGradeVerticalSlider, Math.abs(p.ext().getDiff().getRPerMille()))
                    && mFilterMeasAlarmLevel.filter(p);
        } else {
            return true;
        }
    }

    public class MeasFilterUI {

        private GridPane mRoot;

        public MeasFilterUI() {
            createUI();
        }

        public void onShownFirstTime() {
        }

        private void reset(PropertiesConfiguration filterConfig) {
        }

        private void createUI() {
            mRoot = new GridPane();
            var leftBox = new VBox(rowGap,
                    mDeltaRRangeSlider,
                    mDeltaHRangeSlider,
                    mGradeHorizontalSlider,
                    mDabbaHRangeSlider
            );

            var rightBox = new VBox(rowGap,
                    mFilterMeasAlarmLevel.getRootBordered()
            );

            if (mDimension != BDimension._1d) {
                leftBox.getChildren().add(mGradeVerticalSlider);
                leftBox.getChildren().add(mDabbaRRangeSlider);
            }
            int row = 1;
            mRoot.addRow(row++, leftBox, rightBox);
//            FxHelper.autoSizeColumn(mRoot, 2);
//            FxHelper.bindWidthForChildrens(leftBox, rightBox);
        }

        public Node getRoot() {
            return mRoot;
        }

    }

}
