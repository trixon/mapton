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
package org.mapton.butterfly_hydro.waterlevel;

import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_hydro.waterlevel.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class WaterLevelOptionsView extends BOptionsView {

    private final SessionComboBox<WaterLevelColorBy> mColorScb = new SessionComboBox<>();
    private final WaterLevelOptions mOptions = WaterLevelOptions.getInstance();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<WaterLevelPointBy> mPointScb = new SessionComboBox<>();

    public WaterLevelOptionsView(WaterLevelLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_WaterLevelAction(), WaterLevelOptions.getInstance(), "groundwater");
        createUI();

        initListerners();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(WaterLevelPointBy.values());
        mColorScb.getItems().setAll(WaterLevelColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, mOptions.labelByOperationProperty(), WaterLevelLabelBy.values());
        mLabelMenuButton.setText(mOptions.getLabelFromId(WaterLevelLabelBy.class, mOptions.labelByProperty().get().name(), WaterLevelOptions.DEFAULT_LABEL_BY));

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

    private void initListerners() {
        mOptions.labelByOperationProperty().addListener((p, o, n) -> {
            var n2 = (LabelBy.Operations) n;
            for (var labelBy : WaterLevelLabelBy.values()) {
                if (Strings.CS.equals(n2.getName(), labelBy.getName())) {
                    mOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        mOptions.labelByProperty().addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
        });
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mOptions.graphicsProperty());

        initSession(mOptions);
    }
}
