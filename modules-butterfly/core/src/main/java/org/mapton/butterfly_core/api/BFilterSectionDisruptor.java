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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MDisruptorManager;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BXyzPoint;
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

    private final DisruptorFilterUI mDisruptorFilterUI = new DisruptorFilterUI();
    private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();

    public BFilterSectionDisruptor() {
        super("Störningskällor");
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
        map.put(getTab().getText().toUpperCase(Locale.ROOT), ".");
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            return validateDisruptor(p.getZeroX(), p.getZeroY());
        } else {
            return true;
        }
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDisruptorFilterUI.distanceProperty(),
                mDisruptorManager.lastChangedProperty(),
                mDisruptorFilterUI.mDisruptorGtLtScb.getSelectionModel().selectedItemProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        List.of(
                getDisruptorCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.disruptor", selectedProperty());
        sessionManager.register("filter.checkedDisruptors", mDisruptorFilterUI.checkedStringProperty());
        sessionManager.register("filter.disruptorDistance", mDisruptorFilterUI.distanceProperty());
        sessionManager.register("filter.disruptor.gtlt", mDisruptorFilterUI.mDisruptorGtLtScb.selectedIndexProperty());
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

    private IndexedCheckModel<String> getDisruptorCheckModel() {
        return mDisruptorFilterUI.mDisruptorSccb.getCheckModel();
    }

    private boolean validateDisruptor(Double x, Double y) {
        if (getDisruptorCheckModel().isEmpty()) {
            return true;
        } else {
            var min = mDisruptorFilterUI.mDisruptorGtLtScb.getSelectionModel().getSelectedIndex() == 0;
            return mDisruptorManager.isValidDistance(
                    getDisruptorCheckModel(),
                    min,
                    mDisruptorFilterUI.distanceProperty().getValue(),
                    x, y);
        }
    }

    public class DisruptorFilterUI {

        private final ResourceBundle mBundle = NbBundle.getBundle(DisruptorFilterUI.class);
        private final double mDefaultDisruptorDistance = 75.0;
        private final SessionComboBox<String> mDisruptorGtLtScb = new SessionComboBox<>();
        private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
        private final SessionCheckComboBox<String> mDisruptorSccb = new SessionCheckComboBox<>();
        private final SessionDoubleSpinner mDisruptorSds = new SessionDoubleSpinner(0, 500.0, mDefaultDisruptorDistance, 5.0);
        private HBox mRoot;

        public DisruptorFilterUI() {
            createUI();
        }

        public SimpleStringProperty checkedStringProperty() {
            return mDisruptorSccb.checkedStringProperty();
        }

        public SimpleDoubleProperty distanceProperty() {
            return mDisruptorSds.sessionValueProperty();
        }

        public IndexedCheckModel<String> getCheckModel() {
            return mDisruptorSccb.getCheckModel();
        }

        public void load() {
            mDisruptorSccb.loadAndRestoreCheckItems(mDisruptorManager.getCategories().stream());
            mDisruptorSds.load();
            mDisruptorGtLtScb.load();
        }

        public void reset() {
            mDisruptorSds.getValueFactory().setValue(mDefaultDisruptorDistance);
            mDisruptorSccb.clearChecks();
            mDisruptorGtLtScb.getSelectionModel().selectFirst();
        }

        private void createUI() {
            mDisruptorSccb.setShowCheckedCount(true);
            mDisruptorSccb.setTitle(mBundle.getString("DisruptorCheckComboBoxTitle"));
            mRoot = new HBox(FxHelper.getUIScaled(8), new Label("Avstånd"), mDisruptorGtLtScb, mDisruptorSds, mDisruptorSccb);
            mDisruptorGtLtScb.getItems().setAll("<=", ">=");
            FxHelper.setEditable(true, mDisruptorSds);
            FxHelper.autoCommitSpinners(mDisruptorSds);
            mDisruptorGtLtScb.getSelectionModel().selectFirst();

            mDisruptorSds.disableProperty().bind(Bindings.isEmpty(mDisruptorSccb.getCheckModel().getCheckedItems()));
            mDisruptorGtLtScb.disableProperty().bind(Bindings.isEmpty(mDisruptorSccb.getCheckModel().getCheckedItems()));
        }
    }
}
