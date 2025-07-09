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
package org.mapton.butterfly_activities;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class ActOptionsView extends BOptionsView {

    private static final ActLabelBy DEFAULT_LABEL_BY = ActLabelBy.NONE;
    private static final PointBy DEFAULT_POINT_BY = PointBy.PIN;

    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public ActOptionsView(ActLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_ActAction());
        setDefaultId(DEFAULT_LABEL_BY);
        createUI();
        initListeners();
        initSession();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    private void createUI() {
        mPointScb.getItems().setAll(PointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), ActLabelBy.values());

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());

        var box = new VBox(
                pointLabel,
                mPointScb,
                labelLabel,
                mLabelMenuButton
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);

    }

    private void initListeners() {
        initListenersSuper();

        mPointScb.valueProperty().addListener(getChangeListener());
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.pointBy", mPointScb.selectedIndexProperty());
        sessionManager.register("options.labelBy", labelByIdProperty());

        restoreLabelFromId(ActLabelBy.class, DEFAULT_LABEL_BY);
    }
}
