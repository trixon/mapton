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
package org.mapton.butterfly_tmo.tunnelvatten;

import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BLayerOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_tmo.tunnelvatten.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SelectionModelSession;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class TunnelvattenLayerOptionsView extends BLayerOptionsView {

    private static final TunnelvattenLabelBy DEFAULT_LABEL_BY = TunnelvattenLabelBy.NAME;
    private final SessionComboBox<TunnelvattenColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final TunnelvattenLayerOptions mLayerOptions = TunnelvattenLayerOptions.getInstance();
    private final ComboBox<TunnelvattenPointBy> mPointComboBox = new ComboBox<>();
    private final SessionComboBox<TunnelvattenPointBy> mPointScb = new SessionComboBox<>();
    private final SelectionModelSession mPointSelectionModelSession = new SelectionModelSession(mPointComboBox.getSelectionModel());

    public TunnelvattenLayerOptionsView(TunnelvattenLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_TunnelvattenAction(), TunnelvattenLayerOptions.getInstance(), "tunnelvatten");
        setDefaultId(TunnelvattenLayerOptions.DEFAULT_LABEL_BY);

        createUI();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getGraphicsCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    private void createUI() {
        mPointScb.getItems().setAll(TunnelvattenPointBy.values());
        mColorScb.getItems().setAll(TunnelvattenColorBy.values());
        mColorScb.setDisable(true);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), TunnelvattenLabelBy.values());

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
        mPointScb.valueProperty().bindBidirectional(mLayerOptions.pointProperty());
        mColorScb.valueProperty().bindBidirectional(mLayerOptions.colorByProperty());
        mGraphicSccb.checkedStringProperty().bindBidirectional(mLayerOptions.graphicsProperty());

        initSession(mLayerOptions);

        restoreLabelFromId(TunnelvattenLabelBy.class, mLayerOptions.labelByProperty().get().name(), TunnelvattenLayerOptions.DEFAULT_LABEL_BY);
        labelByProperty().addListener((p, o, n) -> {
            for (var labelBy : TunnelvattenLabelBy.values()) {
                if (Strings.CS.equals(n.getName(), labelBy.getName())) {
                    mLayerOptions.labelByProperty().set(labelBy);
                    break;
                }
            }
        });

        initListenersSuper();
    }
}
