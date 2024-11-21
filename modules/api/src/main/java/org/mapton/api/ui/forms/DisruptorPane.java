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
package org.mapton.api.ui.forms;

import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MDisruptorManager;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class DisruptorPane {

    private final double mDefaultDisruptorDistance = 75.0;
    private final SessionDoubleSpinner mDisruptorSds = new SessionDoubleSpinner(0, 500.0, mDefaultDisruptorDistance, 5.0);
    private final SessionCheckComboBox<String> mDisruptorSccb = new SessionCheckComboBox<>();
    private final MDisruptorManager mDisruptorManager = MDisruptorManager.getInstance();
    private BorderPane mRoot;
    private final ResourceBundle mBundle = NbBundle.getBundle(DisruptorPane.class);

    public DisruptorPane() {
        createUI();
    }

    public Node getRoot() {
        return mRoot;
    }

    public IndexedCheckModel<String> getCheckModel() {
        return mDisruptorSccb.getCheckModel();
    }

    public SimpleDoubleProperty distanceProperty() {
        return mDisruptorSds.sessionValueProperty();
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

    public SimpleStringProperty checkedStringProperty() {
        return mDisruptorSccb.checkedStringProperty();
    }

}
