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

import com.dlsc.gemsfx.util.SessionManager;
import java.util.LinkedHashMap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoOptionsView extends BorderPane {

    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty("NAME");
    private final SimpleObjectProperty<TopoLabelBy> mLabelByProperty = new SimpleObjectProperty<>();
    private final MenuButton mMenuButton = new MenuButton();
    private SessionManager mSessionManager;

    public TopoOptionsView() {
        createUI();
        initListeners();
    }

    public TopoLabelBy getLabelBy() {
        return mLabelByProperty.get();
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(NbPreferences.forModule(getClass()));
        }

        return mSessionManager;
    }

    public SimpleObjectProperty<TopoLabelBy> labelByProperty() {
        return mLabelByProperty;
    }

    private void createUI() {
        populateMenuButton();
        var box = new VBox(
                mMenuButton
        );
        box.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(box);
    }

    private void initListeners() {
        getSessionManager().register("options.labelBy", mLabelByIdProperty);

        mLabelByProperty.addListener((p, o, n) -> {
            mMenuButton.setText(n.getName());
            mLabelByIdProperty.set(n.name());
        });

        mLabelByProperty.set(TopoLabelBy.valueOf(mLabelByIdProperty.get()));
    }

    private void populateMenuButton() {
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

        mMenuButton.getItems().addAll(categoryToMenu.get("").getItems());
        mMenuButton.getItems().add(new SeparatorMenuItem());

        for (var entry : categoryToMenu.entrySet()) {
            if (StringUtils.isNotBlank(entry.getKey())) {
                mMenuButton.getItems().add(entry.getValue());
            }
        }
    }
}
