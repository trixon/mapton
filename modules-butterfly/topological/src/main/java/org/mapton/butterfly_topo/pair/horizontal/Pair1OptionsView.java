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
package org.mapton.butterfly_topo.pair.horizontal;

import java.util.LinkedHashMap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.ui.forms.TabOptionsViewProvider;
import org.mapton.butterfly_topo.pair.PairManagerBase;
import org.mapton.worldwind.api.MOptionsView;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = TabOptionsViewProvider.class)
public class Pair1OptionsView extends MOptionsView implements TabOptionsViewProvider {

    private static final GradeHLabelBy DEFAULT_LABEL_BY = GradeHLabelBy.NAME;
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty(DEFAULT_LABEL_BY.name());
    private final SimpleObjectProperty<GradeHLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mLabelMenuButton = new MenuButton();

    public Pair1OptionsView() {
        createUI();
        initListeners();
        initSession();
    }

    public GradeHLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public SimpleObjectProperty<GradeHLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    @Override
    public Node getOvNode() {
        return this;
    }

    @Override
    public String getOvParent() {
        return "TopoOptionsView";
    }

    @Override
    public int getOvPosition() {
        return 2;
    }

    @Override
    public String getOvTitle() {
        return NbBundle.getMessage(PairManagerBase.class, "tilt_h");
    }

    private void createUI() {
        populateLabelMenuButton();

        var labelLabel = new Label(Dict.LABEL.toString());
        int row = 0;
        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
//        gp.addRow(row++, pointLabel, colorLabel);
//        gp.addRow(row++, mPointScb, mColorScb);
        gp.addRow(row++, labelLabel);
        gp.addRow(row++, mLabelMenuButton);
//        gp.addRow(row++, graphicLabel);
//        gp.add(mGraphicSccb, 0, row++, GridPane.REMAINING, 1);
        gp.setPadding(FxHelper.getUIScaledInsets(8));
        FxHelper.autoSizeRegionHorizontal(mLabelMenuButton);

        setCenter(gp);
    }

    private void initListeners() {
        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("options.gradeH.labelBy", mLabelByIdProperty);

        mLabelByProperty.set(GradeHLabelBy.valueOf(mLabelByIdProperty.get()));
    }

    private void populateLabelMenuButton() {
        var categoryToMenu = new LinkedHashMap<String, Menu>();

        for (var topoLabel : GradeHLabelBy.values()) {
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
