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
package org.mapton.butterfly_remote.insar;

import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_remote.insar.graphics.GraphicItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class InsarOptionsView extends BOptionsView {

    private static final ColorBy DEFAULT_COLOR_BY = ColorBy.ALARM;
    private static final InsarLabelBy DEFAULT_LABEL_BY = InsarLabelBy.NONE;
    private static final InsarPointBy DEFAULT_POINT_BY = InsarPointBy.SYMBOL;
    private final SessionComboBox<ColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<InsarPointBy> mPointScb = new SessionComboBox<>();

    public InsarOptionsView(InsarLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_InsarAction());
        setDefaultId(DEFAULT_LABEL_BY);
        createUI();
        initListeners();
        initSession();
    }

    public ObjectProperty<ColorBy> colorByProperty() {
        return mColorScb.valueProperty();
    }

    public ColorBy getColorBy() {
        return mColorScb.valueProperty().get();
    }

    public IndexedCheckModel<GraphicItem> getGraphicCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public InsarPointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    private void createUI() {
        mPointScb.getItems().setAll(InsarPointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);
        mColorScb.getItems().setAll(ColorBy.values());
        mColorScb.setValue(DEFAULT_COLOR_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), InsarLabelBy.values());

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());
        var colorLabel = new Label(Dict.COLOR.toString());
        var graphicLabel = new Label(Dict.GRAPHICS.toString());

//        var box = new VBox(
//                pointLabel,
//                mPointScb,
//                labelLabel,
//                mLabelMenuButton,
//                mGraphicSccb
//        );
//        box.setPadding(FxHelper.getUIScaledInsets(8));
        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
        gp.addRow(row++, pointLabel, colorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, labelLabel);
        gp.add(mLabelMenuButton, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, graphicLabel);
        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);
//                mIndicatorSccb,
        gp.setPadding(FxHelper.getUIScaledInsets(8));
        FxHelper.autoSizeRegionHorizontal(mPointScb, mColorScb, mLabelMenuButton, mGraphicSccb);

        setCenter(gp);
    }

    private void initListeners() {
        initListenersSuper();

        mPointScb.valueProperty().addListener(getChangeListener());
        mColorScb.valueProperty().addListener(getChangeListener());

        Stream.of(
                mGraphicSccb
        )
                .forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register(getKeyOptions("pointBy"), mPointScb.selectedIndexProperty());
        sessionManager.register(getKeyOptions("colorBy"), mColorScb.selectedIndexProperty());
        sessionManager.register(getKeyOptions("labelBy"), labelByIdProperty());
        sessionManager.register(getKeyOptions("checkedGraphics"), mGraphicSccb.checkedStringProperty());
        initSession(sessionManager);

        restoreLabelFromId(InsarLabelBy.class, DEFAULT_LABEL_BY);
    }
}
