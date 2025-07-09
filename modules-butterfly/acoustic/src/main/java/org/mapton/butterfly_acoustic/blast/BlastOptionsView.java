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
package org.mapton.butterfly_acoustic.blast;

import java.util.stream.Stream;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BlastOptionsView extends BOptionsView {

    private static final BlastLabelBy DEFAULT_LABEL_BY = BlastLabelBy.NONE;
    private static final PointBy DEFAULT_POINT_BY = PointBy.PIN;

    private final SessionCheckComboBox<GraphicRendererItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public BlastOptionsView(BlastLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_BlastAction());
        setDefaultId(DEFAULT_LABEL_BY);
        createUI();
        initListeners();
        initSession();
    }

    public IndexedCheckModel<GraphicRendererItem> getGraphicCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    private void createUI() {
        mPointScb.getItems().setAll(PointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicRendererItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), BlastLabelBy.values());

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());

        var box = new VBox(
                pointLabel,
                mPointScb,
                labelLabel,
                mLabelMenuButton,
                mGraphicSccb
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);

    }

    private void initListeners() {
        initListenersSuper();

        mPointScb.valueProperty().addListener(getChangeListener());

        Stream.of(
                mGraphicSccb)
                .forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.blast.pointBy", mPointScb.selectedIndexProperty());
        sessionManager.register("options.blast.labelBy", labelByIdProperty());
        sessionManager.register("options.blast.checkedGraphics", mGraphicSccb.checkedStringProperty());

        restoreLabelFromId(BlastLabelBy.class, DEFAULT_LABEL_BY);
    }
}
