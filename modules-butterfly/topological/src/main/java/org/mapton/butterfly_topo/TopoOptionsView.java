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

import java.util.LinkedHashMap;
import java.util.stream.Stream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_topo.shared.ColorBy;
import org.mapton.butterfly_topo.shared.PointBy;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Direction;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class TopoOptionsView extends MOptionsView<TopoLayerBundle> {

    private static final ColorBy DEFAULT_COLOR_BY = ColorBy.ALARM;
    private static final TopoLabelBy DEFAULT_LABEL_BY = TopoLabelBy.NAME;
    private static final PointBy DEFAULT_POINT_BY = PointBy.AUTO;

    private final SessionComboBox<ColorBy> mColorScb = new SessionComboBox<>();
    private final SessionCheckComboBox<GraphicRendererItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<Direction> mIndicatorSccb = new SessionCheckComboBox<>();
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty(DEFAULT_LABEL_BY.name());
    private final SimpleObjectProperty<TopoLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();
    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public TopoOptionsView(TopoLayerBundle layerBundle) {
        super(layerBundle);
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

    public IndexedCheckModel<GraphicRendererItem> getComponentCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public IndexedCheckModel<Direction> getIndicatorCheckModel() {
        return mIndicatorSccb.getCheckModel();
    }

    public TopoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    public SimpleObjectProperty<TopoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    private void createUI() {
        mPointScb.getItems().setAll(PointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);
        mColorScb.getItems().setAll(ColorBy.values());
        mColorScb.setValue(DEFAULT_COLOR_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicRendererItem.values());

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

        populateLabelMenuButton();
        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var colorLabel = new Label(Dict.COLOR.toString());
        var labelLabel = new Label(Dict.LABEL.toString());
        var box = new VBox(
                pointLabel,
                mPointScb,
                colorLabel,
                mColorScb,
                labelLabel,
                mLabelMenuButton,
                mIndicatorSccb,
                mGraphicSccb
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);
    }

    private void initListeners() {
        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });

        mPointScb.valueProperty().addListener(getChangeListener());
        mColorScb.valueProperty().addListener(getChangeListener());

        Stream.of(
                mIndicatorSccb,
                mGraphicSccb
        ).forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.pointBy", mPointScb.selectedIndexProperty());
        sessionManager.register("options.colorBy", mColorScb.selectedIndexProperty());
        sessionManager.register("options.labelBy", mLabelByIdProperty);
        sessionManager.register("options.checkedGraphics", mGraphicSccb.checkedStringProperty());
        sessionManager.register("options.checkedIndicators", mIndicatorSccb.checkedStringProperty());

        mLabelByProperty.set(TopoLabelBy.valueOf(mLabelByIdProperty.get()));
    }

    private void populateLabelMenuButton() {
        var categoryToMenu = new LinkedHashMap<String, Menu>();

        for (var topoLabel : TopoLabelBy.values()) {
            var menu = categoryToMenu.computeIfAbsent(topoLabel.getCategory(), k -> {
                return new Menu(k);
            });

            var menuItem = new MenuItem(topoLabel.getName());
            menuItem.setOnAction(actionEvent -> {
                mLabelByProperty.set(topoLabel);
            });
            menu.getItems().add(menuItem);
        }

        mLabelMenuButton.getItems().addAll(categoryToMenu.get("").getItems());
        mLabelMenuButton.getItems().add(new SeparatorMenuItem());

        for (var entry : categoryToMenu.entrySet()) {
            if (StringUtils.isNotBlank(entry.getKey())) {
                mLabelMenuButton.getItems().add(entry.getValue());
            }
        }
    }
}
