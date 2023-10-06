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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Direction;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.CheckModelSession;

/**
 *
 * @author Patrik Karlström
 */
public class TopoOptionsView extends MOptionsView<TopoLayerBundle> {

    private final CheckComboBox<Direction> mIndicatorCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mIndicatorCheckModelSession = new CheckModelSession(mIndicatorCheckComboBox.getCheckModel());
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty("NAME");
    private final SimpleObjectProperty<TopoLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();

    public TopoOptionsView(TopoLayerBundle layerBundle) {
        super(layerBundle);
        createUI();
        initListeners();
        initSession();
    }

    public IndexedCheckModel<Direction> getIndicatorCheckModel() {
        return mIndicatorCheckComboBox.getCheckModel();
    }

    public TopoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public SimpleObjectProperty<TopoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    private void createUI() {
        mIndicatorCheckComboBox.setTitle(Dict.INDICATORS.toString());
        mIndicatorCheckComboBox.setShowCheckedCount(true);
        mIndicatorCheckComboBox.getItems().addAll(
                Direction.NORTH,
                Direction.SOUTH,
                Direction.WEST
        );

        mIndicatorCheckComboBox.setConverter(new StringConverter<Direction>() {
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

        var box = new VBox(
                mLabelMenuButton,
                mIndicatorCheckComboBox
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);
    }

    private void initListeners() {
        getSessionManager().register("options.labelBy", mLabelByIdProperty);

        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getName());
            mLabelByIdProperty.set(n.name());
        });

        mLabelByProperty.set(TopoLabelBy.valueOf(mLabelByIdProperty.get()));

        Stream.of(
                mIndicatorCheckComboBox
        ).forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));
    }

    private void initSession() {
        getSessionManager().register("view.checkedIndicators", mIndicatorCheckModelSession.checkedStringProperty());
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
