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
package org.mapton.butterfly_acoustic.vibration;

import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_acoustic.vibration.graphics.GraphicItem;
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
public class VibrationOptionsView extends BOptionsView {

    private final SessionComboBox<VibrationColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final VibrationOptions mOptions = VibrationOptions.getInstance();
    private final SessionComboBox<VibrationPointBy> mPointScb = new SessionComboBox<>();

    public VibrationOptionsView(VibrationLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_VibrationAction(), VibrationOptions.getInstance(), "vibration");
        setDefaultId(VibrationOptions.DEFAULT_LABEL_BY);

        createUI();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(VibrationPointBy.values());
        mColorScb.getItems().setAll(VibrationColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), VibrationLabelBy.values());

        int row = 0;
        var gp = createGridPane();
        gp.addRow(row++, mPointLabel, mColorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, mLabelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mGraphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);

        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb);

        setCenter(gp);
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mOptions.graphicsProperty());

        initSession(mOptions);

        restoreLabelFromId(VibrationLabelBy.class, mOptions.labelByProperty().get().name(), VibrationOptions.DEFAULT_LABEL_BY);
        labelByProperty().addListener((p, o, n) -> {
            for (var labelBy : VibrationLabelBy.values()) {
                if (Strings.CS.equals(n.getName(), labelBy.getName())) {
                    mOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        initListenersSuper();
    }
}
