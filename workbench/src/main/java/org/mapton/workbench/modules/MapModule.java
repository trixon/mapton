/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.workbench.modules;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.mapton.api.MDict;
import org.mapton.api.MWorkbenchModule;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE_TOOLBAR;
import org.mapton.workbench.modules.map.SearchView;
import org.mapton.workbench.modules.map.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MapModule extends MWorkbenchModule {

    private WindowManager mWindowManager;
    private SearchView mSearchView;

    public MapModule() {
        super(Dict.MAP.toString(), MaterialIcon._Maps.MAP.getImageView(ICON_SIZE_MODULE).getImage());
    }

    @Override
    public Node activate() {
        initAccelerators();

        return mWindowManager;
    }

    @Override
    public void deactivate() {
        for (KeyCodeCombination keyCodeCombination : getKeyCodeCombinations()) {
            getAccelerators().remove(keyCodeCombination);
        }
        getKeyCodeCombinations().clear();

        super.deactivate();
    }

    @Override
    public void init(Workbench workbench) {
        super.init(workbench);

        createUI();
        initToolbars();
    }

    private void activateSearch() {
        Platform.runLater(() -> {
            getScene().getWindow().requestFocus();
            mSearchView.getPresenter().requestFocus();
            ((TextField) mSearchView.getPresenter()).clear();
        });
    }

    private void createUI() {
        mSearchView = new SearchView();
        mWindowManager = new WindowManager();
    }

    private void initAccelerators() {
        KeyCodeCombination keyCodeCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(keyCodeCombination);
        getAccelerators().put(keyCodeCombination, () -> {
            activateSearch();
        });
    }

    private void initToolbars() {
        var goHomeToolbarItem = new ToolbarItem(MaterialIcon._Action.HOME.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(goHomeToolbarItem, Dict.HOME.toString());

        var measureToolbarItem = new ToolbarItem(MaterialIcon._Editor.SPACE_BAR.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(measureToolbarItem, Dict.MEASURE.toString());

        var layerToolbarItem = new ToolbarItem(MaterialIcon._Maps.LAYERS.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(layerToolbarItem, Dict.LAYERS.toString());

        var bookmarkToolbarItem = new ToolbarItem(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(bookmarkToolbarItem, Dict.BOOKMARKS.toString());

        var diagramToolbarItem = new ToolbarItem(MaterialIcon._Editor.SHOW_CHART.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(diagramToolbarItem, Dict.CHART.toString());

        var temporalToolbarItem = new ToolbarItem(MaterialIcon._Action.DATE_RANGE.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(temporalToolbarItem, Dict.Time.DATE.toString());

        var styleToolbarItem = new ToolbarItem("OpenStreetMap", MaterialIcon._Image.COLOR_LENS.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(styleToolbarItem, Dict.STYLE.toString());

        var gridToolbarItem = new ToolbarItem(MaterialIcon._Image.GRID_ON.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(gridToolbarItem, MDict.GRIDS.toString());

        var mapOnlyToolbarItem = new ToolbarItem(MaterialIcon._Navigation.FULLSCREEN.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(mapOnlyToolbarItem, Dict.MAP.toString());

        var attributionToolbarItem = new ToolbarItem(MaterialIcon._Action.COPYRIGHT.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(attributionToolbarItem, Dict.COPYRIGHT.toString());

        var toolboxToolbarItem = new ToolbarItem(MaterialIcon._Places.BUSINESS_CENTER.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(toolboxToolbarItem, Dict.TOOLBOX.toString());

        ToolbarItem searchToolbarItem = new ToolbarItem(mSearchView.getPresenter());

        getToolbarControlsLeft().setAll(
                goHomeToolbarItem,
                measureToolbarItem,
                layerToolbarItem,
                bookmarkToolbarItem,
                diagramToolbarItem,
                searchToolbarItem,
                temporalToolbarItem,
                styleToolbarItem
        );

        getToolbarControlsRight().setAll(
                gridToolbarItem,
                //mapOnlyToolbarItem,
                attributionToolbarItem,
                toolboxToolbarItem
        );
    }
}
