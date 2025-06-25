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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.MDisruptorManager;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionDisruptor extends MBaseFilterSection {

    private final double mDefaultDisruptorDistance = 75.0;
    private final DisruptorFilterUI mDisruptorFilterUI;
    private final SessionComboBox<String> mDisruptorGtLtScb = new SessionComboBox<>();
    private final SessionComboBox<String> mDisruptorHighFrequencyModeScb = new SessionComboBox<>();
    private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
    private final SessionCheckComboBox<String> mDisruptorSccb = new SessionCheckComboBox<>();
    private final SessionDoubleSpinner mDisruptorSds = new SessionDoubleSpinner(0, 500.0, mDefaultDisruptorDistance, 5.0);

    public BFilterSectionDisruptor() {
        super("Störningskällor");
        mDisruptorFilterUI = new DisruptorFilterUI();
        setContent(mDisruptorFilterUI.mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mDisruptorFilterUI.reset();
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(getTab().getText().toUpperCase(Locale.ROOT), ".TODO");
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            var validStep1 = validateDisruptor(p.getZeroX(), p.getZeroY());
            var selectedIndex = mDisruptorHighFrequencyModeScb.getSelectionModel().getSelectedIndex();
            if (validStep1 && selectedIndex > 0) {
                var validStep2 = false;
                if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
                    validStep2 = validateFreqParameter(p.getZeroX(), p.getZeroY(), ext.getFrequenceIntenseBuffer(), selectedIndex);
                }
                return validStep1 && validStep2;
            } else {
                return validStep1;
            }
        } else {
            return true;
        }
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDisruptorSds.sessionValueProperty(),
                mDisruptorManager.lastChangedProperty(),
                mDisruptorGtLtScb.getSelectionModel().selectedItemProperty(),
                mDisruptorHighFrequencyModeScb.getSelectionModel().selectedItemProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        List.of(
                mDisruptorSccb.getCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.disruptor", selectedProperty());
        sessionManager.register("filter.checkedDisruptors", mDisruptorSccb.checkedStringProperty());
        sessionManager.register("filter.disruptorDistance", mDisruptorSds.sessionValueProperty());
        sessionManager.register("filter.disruptor.gtlt", mDisruptorGtLtScb.selectedIndexProperty());
        sessionManager.register("filter.disruptor.highFrequencyMode", mDisruptorHighFrequencyModeScb.selectedIndexProperty());
    }

    public void load() {
        mDisruptorFilterUI.load();
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private boolean validateDisruptor(Double x, Double y) {
        if (mDisruptorSccb.getCheckModel().isEmpty()) {
            return true;
        } else {
            var min = mDisruptorGtLtScb.getSelectionModel().getSelectedIndex() == 0;
            return mDisruptorManager.isValidDistance(
                    mDisruptorSccb.getCheckModel(),
                    min,
                    mDisruptorSds.sessionValueProperty().getValue(),
                    x, y);
        }
    }

    private boolean validateFreqParameter(Double x, Double y, Double buffer, int selectedIndex) {
        if (buffer == null) {
            return false;
        } else {
            var valid = mDisruptorManager.isValidDistance(
                    mDisruptorSccb.getCheckModel(),
                    selectedIndex == 1,
                    buffer,
                    x, y);
            return valid;
        }
    }

    public class DisruptorFilterUI {

        private final ResourceBundle mBundle = NbBundle.getBundle(DisruptorFilterUI.class);
        private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
        private GridPane mRoot = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(8));

        public DisruptorFilterUI() {
            createUI();
        }

        public void load() {
            mDisruptorSccb.loadAndRestoreCheckItems(mDisruptorManager.getCategories().stream());
            mDisruptorSds.load();
            mDisruptorGtLtScb.load();
            mDisruptorHighFrequencyModeScb.load();
        }

        public void reset() {
            mDisruptorSds.getValueFactory().setValue(mDefaultDisruptorDistance);
            mDisruptorSccb.clearChecks();
            mDisruptorGtLtScb.getSelectionModel().selectFirst();
            mDisruptorHighFrequencyModeScb.getSelectionModel().selectFirst();
        }

        private void createUI() {
            mDisruptorSccb.setShowCheckedCount(true);
            mDisruptorSccb.setTitle(mBundle.getString("DisruptorCheckComboBoxTitle"));
            var hbox = new HBox(FxHelper.getUIScaled(8), new Label("Avstånd"), mDisruptorGtLtScb, mDisruptorSds);
            hbox.setAlignment(Pos.CENTER_LEFT);
            var leftBox = new VBox(rowGap,
                    mDisruptorSccb
            );
            var rightBox = new VBox(rowGap,
                    mDisruptorHighFrequencyModeScb
            );
            FxHelper.bindWidthForChildrens(leftBox, rightBox);
            mRoot.addRow(0, leftBox, rightBox);
            mRoot.add(hbox, 0, 1, 2, 1);
            FxHelper.autoSizeColumn(mRoot, 2);
            mDisruptorGtLtScb.getItems().setAll("<=", ">=");
            mDisruptorHighFrequencyModeScb.getItems().setAll("Bortse från hög frekvens", "Har utlösare", "Saknar utlösare");
            FxHelper.setEditable(true, mDisruptorSds);
            FxHelper.autoCommitSpinners(mDisruptorSds);
            mDisruptorGtLtScb.getSelectionModel().selectFirst();
            mDisruptorHighFrequencyModeScb.getSelectionModel().selectFirst();

            var empty = Bindings.isEmpty(mDisruptorSccb.getCheckModel().getCheckedItems());
            mDisruptorSds.disableProperty().bind(empty);
            mDisruptorGtLtScb.disableProperty().bind(empty);
            mDisruptorHighFrequencyModeScb.disableProperty().bind(empty);
        }
    }
}
