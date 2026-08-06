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
package org.mapton.butterfly_topo.monmon;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_format.types.topo.BTopoMonmon;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final double mDefault1 = 0.0;
    private final double mDefault14 = 0.8;
    private final double mDefault7 = 0.5;
    private final CheckBox mLatest14Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest14Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault14, 0.05);
    private final CheckBox mLatest1Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest1Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault1, 0.05);
    private final CheckBox mLatest7Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest7Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault7, 0.05);
    private final MeasFilterUI mMeasFilterUI;
    private final GridPane mRoot = new GridPane(GAP_H, GAP_V * 4);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mMeasFilterUI = new MeasFilterUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mLatest1Checkbox.setSelected(false);
        mLatest1Sds.getValueFactory().setValue(mDefault1);
        mLatest7Checkbox.setSelected(false);
        mLatest7Sds.getValueFactory().setValue(mDefault7);
        mLatest14Checkbox.setSelected(false);
        mLatest14Sds.getValueFactory().setValue(mDefault14);
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toString(), ".");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register("filter.latest1", mLatest1Checkbox.selectedProperty());
        sessionManager.register("filter.latest7", mLatest7Checkbox.selectedProperty());
        sessionManager.register("filter.latest14", mLatest14Checkbox.selectedProperty());
        sessionManager.register("filter.latestValue1", mLatest1Sds.sessionValueProperty());
        sessionManager.register("filter.latestValue7", mLatest7Sds.sessionValueProperty());
        sessionManager.register("filter.latestValue14", mLatest14Sds.sessionValueProperty());
    }

    @Override
    public void onShownFirstTime() {
        mMeasFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    boolean filter(BTopoMonmon p) {
        if (isSelected()) {
            return validateQuota(mLatest1Sds, p.getQuota(1))
                    && validateQuota(mLatest7Sds, p.getQuota(7))
                    && validateQuota(mLatest14Sds, p.getQuota(14))
                    && true;
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        mLatest1Sds.disableProperty().bind(mLatest1Checkbox.selectedProperty().not());
        mLatest7Sds.disableProperty().bind(mLatest7Checkbox.selectedProperty().not());
        mLatest14Sds.disableProperty().bind(mLatest14Checkbox.selectedProperty().not());
        List.of(
                selectedProperty(),
                //
                mLatest1Sds.sessionValueProperty(),
                mLatest1Sds.disabledProperty(),
                mLatest7Sds.sessionValueProperty(),
                mLatest7Sds.disabledProperty(),
                mLatest14Sds.sessionValueProperty(),
                mLatest14Sds.disabledProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load() {
        mLatest1Sds.load();
        mLatest7Sds.load();
        mLatest14Sds.load();
    }

    private boolean validateQuota(SessionDoubleSpinner sds, double quota) {
        if (!sds.isDisabled()) {
            double lim = sds.valueProperty().get();
            double value = Math.abs(quota);

            if (lim == 0) {
                return value == 0;
            } else if (lim < 0) {
                return value <= Math.abs(lim);
            } else {
                return value >= lim;
            }
        } else {
            return true;
        }
    }

    public class MeasFilterUI {

        public MeasFilterUI() {
            createUI();
        }

        public void onShownFirstTime() {
        }

        private void createUI() {
            mLatest1Checkbox.setText("Senaste dygnet");
            mLatest7Checkbox.setText("Senaste veckan");
            mLatest14Checkbox.setText("Senaste två veckorna");
            mLatest1Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());
            mLatest7Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());
            mLatest14Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());

            int titleGap = SwingHelper.getUIScaled(4);

            var vBox = new VBox(GAP,
                    new VBox(titleGap,
                            mLatest1Checkbox,
                            mLatest1Sds
                    ),
                    new VBox(titleGap,
                            mLatest7Checkbox,
                            mLatest7Sds
                    ),
                    new VBox(titleGap,
                            mLatest14Checkbox,
                            mLatest14Sds
                    )
            );

            FxHelper.setEditable(true, mLatest1Sds, mLatest7Sds, mLatest14Sds);
            FxHelper.autoCommitSpinners(mLatest1Sds, mLatest7Sds, mLatest14Sds);

            autoSize(vBox);
            mRoot.addRow(0, vBox);
//
            FxHelper.autoSizeColumn(mRoot, 2);
            mRoot.setMaxWidth(getMaxWidth());
        }
    }
}
