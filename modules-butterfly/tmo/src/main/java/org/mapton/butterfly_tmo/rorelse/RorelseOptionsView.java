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
package org.mapton.butterfly_tmo.rorelse;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SelectionModelSession;

/**
 *
 * @author Patrik Karlström
 */
public class RorelseOptionsView extends BOptionsView {

    private static final RorelseLabelBy DEFAULT_LABEL_BY = RorelseLabelBy.NAME;

    private final ComboBox<PointBy> mPointComboBox = new ComboBox<>();
    private final SelectionModelSession mPointSelectionModelSession = new SelectionModelSession(mPointComboBox.getSelectionModel());

    public RorelseOptionsView(RorelseLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_RorelseAction());
        setDefaultId(DEFAULT_LABEL_BY);
        createUI();
        initListeners();
        initSession();
    }

    public PointBy getPointBy() {
        return mPointComboBox.valueProperty().get();
    }

    private void createUI() {
        mPointComboBox.getItems().setAll(PointBy.values());
        mPointComboBox.setValue(PointBy.NONE);

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), RorelseLabelBy.values());

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());

        var box = new VBox(
                pointLabel,
                mPointComboBox,
                labelLabel,
                mLabelMenuButton
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);

    }

    private void initListeners() {
        initListenersSuper();

        mPointComboBox.valueProperty().addListener(getChangeListener());
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.rorelse.pointBy", mPointSelectionModelSession.selectedIndexProperty());
        sessionManager.register("options.rorelse.labelBy", labelByIdProperty());
        restoreLabelFromId(RorelseLabelBy.class, DEFAULT_LABEL_BY);
    }
}
