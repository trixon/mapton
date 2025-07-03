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
package org.mapton.butterfly_tmo.vattenkemi;

import java.util.LinkedHashMap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SelectionModelSession;

/**
 *
 * @author Patrik Karlström
 */
public class VattenkemiOptionsView extends MOptionsView {

    private static final VattenkemiLabelBy DEFAULT_LABEL_BY = VattenkemiLabelBy.NAME;

    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty(DEFAULT_LABEL_BY.name());
    private final SimpleObjectProperty<VattenkemiLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();
    private final ComboBox<PointBy> mPointComboBox = new ComboBox<>();
    private final SelectionModelSession mPointSelectionModelSession = new SelectionModelSession(mPointComboBox.getSelectionModel());

    public VattenkemiOptionsView(VattenkemiLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_VattenkemiAction());
        createUI();
        initListeners();
        initSession();
    }

    public VattenkemiLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public PointBy getPointBy() {
        return mPointComboBox.valueProperty().get();
    }

    public SimpleObjectProperty<VattenkemiLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    private void createUI() {
        mPointComboBox.getItems().setAll(PointBy.values());
        mPointComboBox.setValue(PointBy.NONE);

        populateLabelMenuButton();

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());

        var box = new VBox(
                pointLabel,
                mPointComboBox,
                labelLabel,
                mLabelMenuButton
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);

    }

    private void initListeners() {
        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });

        mPointComboBox.valueProperty().addListener(getChangeListener());

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.vattenkemi.pointBy", mPointSelectionModelSession.selectedIndexProperty());
        sessionManager.register("options.vattenkemi.labelBy", mLabelByIdProperty);

        try {
            mLabelByProperty.set(VattenkemiLabelBy.valueOf(mLabelByIdProperty.get()));
        } catch (IllegalArgumentException e) {
            mLabelByProperty.set(DEFAULT_LABEL_BY);
        }
    }

    private void populateLabelMenuButton() {
        var categoryToMenu = new LinkedHashMap<String, Menu>();

        for (var topoLabel : VattenkemiLabelBy.values()) {
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
