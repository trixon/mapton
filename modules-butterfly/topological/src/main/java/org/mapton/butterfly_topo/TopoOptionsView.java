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

import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MRunnable;
import org.mapton.butterfly_core.api.BOptionsView;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_topo.graphics.GraphicItem;
import org.mapton.butterfly_topo.shared.ColorBy;
import org.mapton.butterfly_topo.shared.PointBy;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Direction;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class TopoOptionsView extends BOptionsView implements MRunnable {

    private static final ColorBy DEFAULT_COLOR_BY = ColorBy.ALARM;
    private static final TopoLabelBy DEFAULT_LABEL_BY = TopoLabelBy.NAME;
    private static final PointBy DEFAULT_POINT_BY = PointBy.AUTO;

    private final SessionComboBox<ColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<Direction> mIndicatorSccb = new SessionCheckComboBox<>();
    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public TopoOptionsView(TopoLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_ControlPointAction());
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

    public IndexedCheckModel<GraphicItem> getComponentCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public IndexedCheckModel<Direction> getIndicatorCheckModel() {
        return mIndicatorSccb.getCheckModel();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    @Override
    public void run() {
    }

    @Override
    public void runOnce() {
        FxHelper.setVisibleRowCount(99, mGraphicSccb);
    }

    private void createUI() {
        mPointScb.getItems().setAll(PointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);
        mColorScb.getItems().setAll(ColorBy.values());
        mColorScb.setValue(DEFAULT_COLOR_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicItem.values());
        mIndicatorSccb.setTitle(Dict.INDICATORS.toString());
        mIndicatorSccb.setShowCheckedCount(true);
        mIndicatorSccb.getItems().addAll(
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST
        );

        mIndicatorSccb.setConverter(new StringConverter<Direction>() {
            @Override
            public Direction fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
            }

            @Override
            public String toString(Direction direction) {
                var base = direction.getName() + "\t ";

                switch (direction) {
                    case NORTH -> {
                        return base + Dict.Geometry.HEIGHT.toString();
                    }
                    case SOUTH -> {
                        return base + Dict.Geometry.PLANE.toString();
                    }
                    case WEST -> {
                        return base + getBundle().getString("nextMeasCheckComboBoxTitle");
                    }

                    default ->
                        throw new AssertionError();
                }
            }
        });

        LabelBy.populateMenuButton(mLabelMenuButton, labelByProperty(), TopoLabelBy.values());
        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var colorLabel = new Label(Dict.COLOR.toString());
        var labelLabel = new Label(Dict.LABEL.toString());
        var graphicLabel = new Label(Dict.GRAPHICS.toString());

        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
        gp.addRow(row++, pointLabel, colorLabel);
        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, labelLabel);
        gp.addRow(row++, mLabelMenuButton);
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
                mIndicatorSccb,
                mGraphicSccb
        ).forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register(getKeyOptions("pointBy"), mPointScb.selectedIndexProperty());
        sessionManager.register(getKeyOptions("colorBy"), mColorScb.selectedIndexProperty());
        sessionManager.register(getKeyOptions("labelBy"), labelByIdProperty());
        sessionManager.register(getKeyOptions("checkedGraphics"), mGraphicSccb.checkedStringProperty());
        sessionManager.register(getKeyOptions("checkedIndicators"), mIndicatorSccb.checkedStringProperty());
        initSession(sessionManager);

        restoreLabelFromId(TopoLabelBy.class, DEFAULT_LABEL_BY);
    }
}
