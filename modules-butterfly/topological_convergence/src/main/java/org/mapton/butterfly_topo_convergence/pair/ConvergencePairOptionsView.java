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
package org.mapton.butterfly_topo_convergence.pair;

import java.util.LinkedHashMap;
import java.util.stream.Stream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairOptionsView extends MOptionsView {

    private static final ConvergencePairLabelBy DEFAULT_LABEL_BY = ConvergencePairLabelBy.NAME;
    private static final PointBy DEFAULT_POINT_BY = PointBy.PIN;

    private final SessionCheckComboBox<GraphicRendererItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty(DEFAULT_LABEL_BY.name());
    private final SimpleObjectProperty<ConvergencePairLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();
    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public ConvergencePairOptionsView(ConvergencePairLayerBundle layerBundle) {
        super(layerBundle, Bundle.CTL_ConvergencePairAction());
        createUI();
        initListeners();
        initSession();
    }

    public IndexedCheckModel<GraphicRendererItem> getGraphicCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public ConvergencePairLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    public SimpleObjectProperty<ConvergencePairLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    private void createUI() {
        mPointScb.getItems().setAll(PointBy.values());
        mPointScb.setValue(DEFAULT_POINT_BY);

        mGraphicSccb.setTitle(Dict.GRAPHICS.toString());
        mGraphicSccb.setShowCheckedCount(true);
        mGraphicSccb.getItems().setAll(GraphicRendererItem.values());

        populateLabelMenuButton();

        var pointLabel = new Label(Dict.Geometry.POINT.toString());
        var labelLabel = new Label(Dict.LABEL.toString());

        var box = new VBox(
                pointLabel,
                mPointScb,
                labelLabel,
                mLabelMenuButton,
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

        Stream.of(
                mGraphicSccb)
                .forEachOrdered(ccb -> ccb.getCheckModel().getCheckedItems().addListener(getListChangeListener()));

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.convergence.pair.pointBy", mPointScb.selectedIndexProperty());
        sessionManager.register("options.convergence.pair.labelBy", mLabelByIdProperty);
        sessionManager.register("options.convergence.pair.checkedGraphics", mGraphicSccb.checkedStringProperty());

        try {
            mLabelByProperty.set(ConvergencePairLabelBy.valueOf(mLabelByIdProperty.get()));
        } catch (IllegalArgumentException e) {
            mLabelByProperty.set(DEFAULT_LABEL_BY);
        }
    }

    private void populateLabelMenuButton() {
        var categoryToMenu = new LinkedHashMap<String, Menu>();

        for (var topoLabel : ConvergencePairLabelBy.values()) {
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
