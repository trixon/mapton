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
package org.mapton.butterfly_tmo.infiltration;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_tmo.infiltration.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SelectionModelSession;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class InfiltrationOptionsView extends BOptionsView {

    private static final InfiltrationLabelBy DEFAULT_LABEL_BY = InfiltrationLabelBy.NAME;

    private final ComboBox<InfiltrationPointBy> mPointComboBox = new ComboBox<>();
    private final SelectionModelSession mPointSelectionModelSession = new SelectionModelSession(mPointComboBox.getSelectionModel());
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<InfiltrationPointBy> mPointScb = new SessionComboBox<>();
    private final SessionComboBox<InfiltrationColorBy> mColorScb = new SessionComboBox<>();
    private final InfiltrationOptions mOptions = InfiltrationOptions.getInstance();

    public InfiltrationOptionsView(InfiltrationLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_InfiltrationAction(), InfiltrationOptions.getInstance(), "infiltration");
        setDefaultId(InfiltrationOptions.DEFAULT_LABEL_BY);

        createUI();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(InfiltrationPointBy.values());
        mColorScb.getItems().setAll(InfiltrationColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), InfiltrationLabelBy.values());

        int row = 0;
        var gp = createGridPane();
        gp.addRow(row++, mPointLabel, mColorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, mLabelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mGraphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);

        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb);
//        activateAnnotation();

        setCenter(gp);
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mOptions.graphicsProperty());

        initSession(mOptions);

        restoreLabelFromId(InfiltrationLabelBy.class, mOptions.labelByProperty().get().name(), InfiltrationOptions.DEFAULT_LABEL_BY);
        labelByProperty().addListener((p, o, n) -> {
            for (var labelBy : InfiltrationLabelBy.values()) {
                if (Strings.CS.equals(n.getName(), labelBy.getName())) {
                    mOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        initListenersSuper();
    }
}
