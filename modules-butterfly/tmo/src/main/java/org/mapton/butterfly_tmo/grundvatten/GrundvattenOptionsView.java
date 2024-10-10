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
package org.mapton.butterfly_tmo.grundvatten;

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
public class GrundvattenOptionsView extends MOptionsView {

    private static final GrundvattenLabelBy DEFAULT_LABEL_BY = GrundvattenLabelBy.NAME;
    private static final PointBy DEFAULT_POINT_BY = PointBy.PIN;
    private final SessionCheckComboBox<GraphicRendererItem> mGraphicSccb = new SessionCheckComboBox<>();
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty(DEFAULT_LABEL_BY.name());
    private final SimpleObjectProperty<GrundvattenLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();
    private final SessionComboBox<PointBy> mPointScb = new SessionComboBox<>();

    public GrundvattenOptionsView(GrundvattenLayerBundle layerBundle) {
        super(layerBundle);
        createUI();
        initListeners();
        initSession();
    }

    public IndexedCheckModel<GraphicRendererItem> getGraphicCheckModel() {
        return mGraphicSccb.getCheckModel();
    }

    public GrundvattenLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public PointBy getPointBy() {
        return mPointScb.valueProperty().get();
    }

    public SimpleObjectProperty<GrundvattenLabelBy> labelByProperty() {
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
        sessionManager.register("options.grundvatten.pointBy", mPointScb.selectedIndexProperty());
        sessionManager.register("options.grundvatten.labelBy", mLabelByIdProperty);
        sessionManager.register("options.grundvatten.checkedGraphics", mGraphicSccb.checkedStringProperty());

        try {
            mLabelByProperty.set(GrundvattenLabelBy.valueOf(mLabelByIdProperty.get()));
        } catch (IllegalArgumentException e) {
            mLabelByProperty.set(GrundvattenLabelBy.NAME);
        }
    }

    private void populateLabelMenuButton() {
        var categoryToMenu = new LinkedHashMap<String, Menu>();

        for (var topoLabel : GrundvattenLabelBy.values()) {
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
