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
package org.mapton.butterfly_topo;

import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MRunnable;
import org.mapton.butterfly_core.api.BLayerOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_topo.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class TopoLayerOptionsView extends BLayerOptionsView implements MRunnable {

    private final SessionComboBox<TopoColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final TopoLayerOptions mLayerOptions = TopoLayerOptions.getInstance();
    private final SessionComboBox<TopoPointBy> mPointScb = new SessionComboBox<>();

    public TopoLayerOptionsView(TopoLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_ControlPointAction(), TopoLayerOptions.getInstance(), "topo");
        setGraphicsModel(mGraphicSccb.getCheckModel());
        createUI();

        initListerners();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    @Override
    public void run() {
    }

    @Override
    public void runOnce() {
        FxHelper.setVisibleRowCount(99, mGraphicSccb);
    }

    private void createUI() {
        mPointScb.getItems().setAll(TopoPointBy.values());
        mColorScb.getItems().setAll(TopoColorBy.values());

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, mLayerOptions.labelByOperationProperty(), TopoLabelBy.values());
        mLabelMenuButton.setText(mLayerOptions.getLabelFromId(TopoLabelBy.class, mLayerOptions.labelByProperty().get().name(), TopoLayerOptions.DEFAULT_LABEL_BY));

        int row = 0;
        var gp = createGridPane();
        gp.addRow(row++, mPointLabel, mColorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, mLabelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, mGraphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);
        gp.add(getClearGraphicsButton(), 0, row++, GridPane.REMAINING, 1);
        gp.add(mTrendGridPane, 0, row++, GridPane.REMAINING, 1);

        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb, getClearGraphicsButton());
        activateAnnotation();

        setCenter(gp);
    }

    private void initListerners() {
        mLayerOptions.labelByOperationProperty().addListener((p, o, n) -> {
            var n2 = (LabelBy.Operations) n;
            for (var labelBy : TopoLabelBy.values()) {
                if (Strings.CS.equals(n2.getName(), labelBy.getName())) {
                    mLayerOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        mLayerOptions.labelByProperty().addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
        });
    }

    private void initSession() {
        mPointScb.valueProperty().bindBidirectional(mLayerOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mLayerOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mLayerOptions.graphicsProperty());

        initSession(mLayerOptions);
    }
}
