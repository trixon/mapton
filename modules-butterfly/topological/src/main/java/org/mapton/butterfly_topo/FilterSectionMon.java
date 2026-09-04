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
package org.mapton.butterfly_topo;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMon extends MBaseFilterSection {

    private RangeSliderPane mAngleHRangeSlider;
    private RangeSliderPane mAngleVRangeSlider;

    private RangeSliderPane mDelta1dRangeSlider;
    private RangeSliderPane mDelta2dRangeSlider;
    private RangeSliderPane mDelta3dRangeSlider;
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public FilterSectionMon() {
        super("Längd & Vinkel");
        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mDelta1dRangeSlider.clear();
        mDelta2dRangeSlider.clear();
        mDelta3dRangeSlider.clear();
        mAngleVRangeSlider.clear();
        mAngleHRangeSlider.clear();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

    }

    public boolean filter(BTopoControlPoint p) {
        if (isSelected()) {
            var valid = true
                    && validate(p)
                    && true;
            return valid;
        } else {
            return true;
        }
    }

    public Node getRoot() {
        return mRoot;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        List.of(
                mDelta1dRangeSlider,
                mDelta2dRangeSlider,
                mDelta3dRangeSlider,
                mAngleHRangeSlider,
                mAngleVRangeSlider
        ).forEach(rangeSlider -> {
            rangeSlider.selectedProperty().addListener(changeListener);
            rangeSlider.invertedProperty().addListener(changeListener);
            rangeSlider.maxProperty().addListener(changeListener);
            rangeSlider.minProperty().addListener(changeListener);
        });
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        mDelta1dRangeSlider.initSession(getKeyFilter("distance1d"), sessionManager);
        mDelta2dRangeSlider.initSession(getKeyFilter("distance2d"), sessionManager);
        mDelta3dRangeSlider.initSession(getKeyFilter("distance3d"), sessionManager);
        mAngleVRangeSlider.initSession(getKeyFilter("angleV"), sessionManager);
        mAngleHRangeSlider.initSession(getKeyFilter("angleH"), sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private void createUI() {
        mDelta1dRangeSlider = new RangeSliderPane("Vertikal längd", -50, 50);
        mDelta2dRangeSlider = new RangeSliderPane("Horisontal längd", 0, 500);
        mDelta3dRangeSlider = new RangeSliderPane("Lutande längd", 0, 500);
        mAngleVRangeSlider = new RangeSliderPane("Vertikalvinkel", 0, 200);
        mAngleHRangeSlider = new RangeSliderPane("Horisontalvinkel", -100, 400);

        var rangeSliders = List.of(
                mDelta1dRangeSlider,
                mDelta2dRangeSlider,
                mDelta3dRangeSlider,
                mAngleHRangeSlider,
                mAngleVRangeSlider
        );

        rangeSliders.forEach(rangeSlider -> {
            rangeSlider.setInvertIncluded(true);
            FxHelper.autoSizeRegionHorizontal(rangeSlider);
        });

        mRoot.addColumn(0,
                rangeSliders.toArray(RangeSliderPane[]::new)
        );
    }

    private boolean validate(BTopoControlPoint p) {
        if (!mDelta1dRangeSlider.selectedProperty()
                .or(mDelta2dRangeSlider.selectedProperty())
                .or(mDelta3dRangeSlider.selectedProperty())
                .or(mAngleHRangeSlider.selectedProperty())
                .or(mAngleVRangeSlider.selectedProperty())
                .get()) {
            return true;
        }

        var delta = p.<Vector3D>getValue("MONMON");
        if (delta == null) {
            return false;
        }
        var dist2d = Math.hypot(delta.getX(), delta.getY());
        var ha = MathHelper.convertCcwGonToCw(MathHelper.azimuthToGon(delta.getX(), delta.getY()));
        var validNegHA = false;

        if (ha >= 300) {
            var negHv = ha - 400;
            var min = mAngleHRangeSlider.minProperty().get();
            var max = mAngleHRangeSlider.maxProperty().get();
            validNegHA = negHv >= min && negHv <= max;
        }

        var vaGon = 100 - Math.atan2(delta.getZ(), dist2d) * MathHelper.RADIANS_TO_GON;

        var valid
                = mDelta1dRangeSlider.isValueValid(delta.getZ())
                && mDelta2dRangeSlider.isValueValid(dist2d)
                && mDelta3dRangeSlider.isValueValid(delta.norm())
                && (mAngleHRangeSlider.isValueValid(ha) || validNegHA)
                && mAngleVRangeSlider.isValueValid(vaGon);

        return valid;
    }

}
