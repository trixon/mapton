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
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MDisruptorManager;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionDisruptor extends MBaseFilterSection {

    private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
    private final DisruptorPane mDisruptorPane = new DisruptorPane();
    private final GridPane mRoot = new GridPane();

    public BFilterSectionDisruptor() {
        super("Störningskällor");
        init();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mDisruptorPane.reset();
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            return validateDisruptor(p.getZeroX(), p.getZeroY());
        } else {
            return true;
        }
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(getTab().getText().toUpperCase(Locale.ROOT), ".");
    }

    public void initListeners(ChangeListener changeListener, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mDisruptorPane.distanceProperty(),
                mDisruptorManager.lastChangedProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListener));

        List.of(
                getDisruptorCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.disruptor", selectedProperty());
        sessionManager.register("filter.checkedDisruptors", mDisruptorPane.checkedStringProperty());
        sessionManager.register("filter.disruptorDistance", mDisruptorPane.distanceProperty());
    }

    public void load() {
        mDisruptorPane.load();
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private IndexedCheckModel<String> getDisruptorCheckModel() {
        return mDisruptorPane.mDisruptorSccb.getCheckModel();
    }

    private void init() {
        mRoot.addRow(0, mDisruptorPane.getRoot());
    }

    private boolean validateDisruptor(Double x, Double y) {
        if (getDisruptorCheckModel().isEmpty()) {
            return true;
        } else {
            return mDisruptorManager.isValidCoordinate(getDisruptorCheckModel(), mDisruptorPane.distanceProperty().getValue(), x, y);
        }
    }

    public class DisruptorPane {

        private final ResourceBundle mBundle = NbBundle.getBundle(DisruptorPane.class);

        private final double mDefaultDisruptorDistance = 75.0;
        private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
        private final SessionCheckComboBox<String> mDisruptorSccb = new SessionCheckComboBox<>();
        private final SessionDoubleSpinner mDisruptorSds = new SessionDoubleSpinner(0, 500.0, mDefaultDisruptorDistance, 5.0);
        private BorderPane mRoot;

        public DisruptorPane() {
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

        public Node getRoot() {
            return mRoot;
        }

        public void load() {
            mDisruptorSccb.loadAndRestoreCheckItems(mDisruptorManager.getCategories().stream());
            mDisruptorSds.load();
        }

        public void reset() {
            mDisruptorSds.getValueFactory().setValue(mDefaultDisruptorDistance);
            mDisruptorSccb.clearChecks();
        }

        private void createUI() {
            mDisruptorSccb.setShowCheckedCount(true);
            mDisruptorSccb.setTitle(mBundle.getString("DisruptorCheckComboBoxTitle"));
            mRoot = new BorderPane(mDisruptorSccb);
            mRoot.setLeft(mDisruptorSds);
            mDisruptorSds.setPadding(FxHelper.getUIScaledInsets(0, 8, 0, 0));

            FxHelper.setEditable(true, mDisruptorSds);
            FxHelper.autoCommitSpinners(mDisruptorSds);
            mDisruptorSds.disableProperty().bind(Bindings.isEmpty(mDisruptorSccb.getCheckModel().getCheckedItems()));
        }

    }
}
