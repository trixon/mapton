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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.RangeSliderPane;
import se.trixon.almond.util.fx.control.SliderPane;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionMisc<T extends BXyzPoint> extends MBaseFilterSection {

    private final ResourceBundle mBundle = NbBundle.getBundle(BFilterSectionMisc.class);
    private final CheckBox mClusterCheckbox = new CheckBox("Autokluster");
    private RangeSliderPane mDeltaHRangeSlider;
    private RangeSliderPane mDeltaRRangeSlider;
    private SliderPane mDeltaRSlider;
    private final DistanceMeasure mDistanceMeasure;
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionMisc() {
        super(Dict.MISCELLANEOUS.toString());
        mDistanceMeasure = (DistanceMeasure) (double[] a, double[] b) -> {
            var plane = Math.hypot(b[1] - a[1], b[0] - a[0]);
            var height = Math.abs(b[2] - a[2]);
            var hMin = mDeltaHRangeSlider.minProperty().get();
            var hMax = mDeltaHRangeSlider.maxProperty().get();

            if (MathHelper.isBetween(hMin, hMax, height)) {
                return plane;
            } else {
                return Double.MAX_VALUE;
            }
        };

        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mDeltaHRangeSlider.clear();
        mDeltaRSlider.clear();
        FxHelper.setSelected(false,
                mInvertCheckbox,
                mClusterCheckbox
        );
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.MISCELLANEOUS.toUpper(), "TODO");
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            var valid = true;

            return valid;
        } else {
            return true;
        }
    }

    public List<T> filterCluster(List<T> filteredItems) {
        if (!isSelected() || !mClusterCheckbox.isSelected()) {
            return filteredItems;
        }
        var filteredPoints = filteredItems.stream()
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .toList();

        if (filteredPoints.isEmpty()) {
            return List.of();
        }

        var epsilon = mDeltaRSlider.valueProperty().get();
//        var allItems = new ArrayList<>(getPoints(filteredItems, mDeltaRRangeSlider.maxProperty().get()));
//        var excludedItems = getPoints(filteredItems, mDeltaRRangeSlider.minProperty().get());
//        allItems.removeAll(excludedItems);
//
//        return allItems;

        return getPoints(filteredItems, epsilon);
    }

    public CheckBox getInvertCheckbox() {
        return mInvertCheckbox;
    }

    public Node getInvertCheckboxToolBarItem() {
        mInvertCheckbox.setText(mBundle.getString("invertCheckBoxText"));
        var internalBox = new HBox(FxHelper.getUIScaled(8.0), mInvertCheckbox);
        internalBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8.0));
        internalBox.setAlignment(Pos.CENTER_LEFT);

        return internalBox;
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                //
                mClusterCheckbox.selectedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

//        List.of(
//                getDateFromToCheckModel()
//        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
        List.of(
                mDeltaHRangeSlider,
                mDeltaRRangeSlider
        ).forEach(rangeSlider -> {
            rangeSlider.selectedProperty().addListener(changeListener);
            rangeSlider.maxProperty().addListener(changeListener);
            rangeSlider.minProperty().addListener(changeListener);
        });
        List.of(
                mDeltaRSlider
        ).forEach(slider -> {
            slider.selectedProperty().addListener(changeListener);
            slider.valueProperty().addListener(changeListener);
        });
    }

    public void initListeners(BFilterSectionMiscProvider filter) {
        filter.invertProperty().bind(invertSelectionProperty());
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        mDeltaHRangeSlider.initSession("xxDeltaH", sessionManager);
        mDeltaRRangeSlider.initSession("xxDeltaR", sessionManager);
        mDeltaRSlider.initSession("xxyDeltaR", sessionManager);

        sessionManager.register("filter.invert", invertSelectionProperty());
    }

    public BooleanProperty invertSelectionProperty() {
        return mInvertCheckbox.selectedProperty();
    }

    public void load() {
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private void createUI() {
        mDeltaHRangeSlider = new RangeSliderPane("Höjd, intervall", 0, 20);
        mDeltaRRangeSlider = new RangeSliderPane("Plan, intervall", 0, 20);
        mDeltaRSlider = new SliderPane("Plan, maxavstånd", 20);

        int row = 0;
        mRoot.addRow(row++, mClusterCheckbox);
        mRoot.addRow(row++, mDeltaRSlider);
//        mRoot.addRow(row++, mDeltaRRangeSlider);
        mRoot.addRow(row++, mDeltaHRangeSlider);

        mDeltaHRangeSlider.disableProperty().bind(mClusterCheckbox.selectedProperty().not());
        mDeltaRRangeSlider.disableProperty().bind(mClusterCheckbox.selectedProperty().not());
        mDeltaRSlider.disableProperty().bind(mClusterCheckbox.selectedProperty().not());
    }

    private List<T> getPoints(List<T> items, double epsilon) {
        var minPoints = 1;
        var dbscan = new DBSCANClusterer<BXyzPoint>(epsilon, minPoints, mDistanceMeasure);
        //Calculate and subtract min on order to use dbscan.
        var minX = items.stream().mapToDouble(p -> p.getZeroX()).min().getAsDouble();
        var minY = items.stream().mapToDouble(p -> p.getZeroY()).min().getAsDouble();
        var minZ = items.stream().mapToDouble(p -> p.getZeroZ()).min().getAsDouble();

        items.forEach(p -> {
            p.setZeroXScaled(p.getZeroX() - minX);
            p.setZeroYScaled(p.getZeroY() - minY);
            p.setZeroZScaled(p.getZeroZ() - minZ);
        });

        var clusters = dbscan.cluster((Collection<BXyzPoint>) items);

        return (List<T>) clusters.stream()
                .flatMap(cluster -> cluster.getPoints().stream())
                .toList();

    }

}
