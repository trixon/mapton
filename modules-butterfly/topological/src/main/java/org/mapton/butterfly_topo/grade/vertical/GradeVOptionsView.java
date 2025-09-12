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
package org.mapton.butterfly_topo.grade.vertical;

import java.util.stream.Stream;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.butterfly_topo.grade.GradePointBy;
import org.mapton.butterfly_topo.grade.vertical.graphics.GraphicItem;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVOptionsView extends BOptionsView {

    private static final GradeVLabelBy DEFAULT_LABEL_BY = GradeVLabelBy.NAME;
    private static final GradePointBy DEFAULT_POINT_BY = GradePointBy.PIN;
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final BooleanProperty mPlotPointProperty = new SimpleBooleanProperty();
    private final SessionComboBox<GradePointBy> mPointScb = new SessionComboBox<>();

    public GradeVOptionsView(GradeVLayerBundle layerBundle) {
        super(layerBundle, NbBundle.getMessage(GradeManagerBase.class, "grade_v"));
        setDefaultId(DEFAULT_LABEL_BY);
        createUI();
        initListeners();
        initSession();
    }

    public IndexedCheckModel<GraphicItem> getComponentCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public GradePointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    public BooleanProperty plotPointProperty() {
        return mPlotPointProperty;
    }

    private void createUI() {
        mPointScb.getItems().setAll(GradePointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), GradeVLabelBy.values());
        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());
        var graphicLabel = new Label(Dict.GRAPHICS.toString());

        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
        gp.addRow(row++, pointLabel);
        gp.addRow(row++, mPointScb);
        gp.addRow(row++, labelLabel);
        gp.addRow(row++, mLabelMenuButton);
        gp.addRow(row++, graphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);
        gp.setPadding(FxHelper.getUIScaledInsets(8));
        FxHelper.autoSizeRegionHorizontal(mPointScb, mLabelMenuButton, mGraphicSccb);

        setCenter(gp);
    }

    private void initListeners() {
        initListenersSuper();
        mPointScb.valueProperty().addListener(getChangeListener());
        Stream.of(
                mGraphicSccb
        ).forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register(getKeyOptions("labelBy"), labelByIdProperty());
        sessionManager.register(getKeyOptions("checkedGraphics"), mGraphicSccb.checkedStringProperty());
        sessionManager.register(getKeyOptions("pointBy"), mPointScb.selectedIndexProperty());
        initSession(sessionManager);

        restoreLabelFromId(GradeVLabelBy.class, DEFAULT_LABEL_BY);
    }
}
