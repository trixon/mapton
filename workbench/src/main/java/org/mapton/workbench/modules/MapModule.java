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
import java.util.HashMap;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.PopOver;
import org.mapton.api.MDict;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MKey;
import org.mapton.api.MWorkbenchModule;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE_TOOLBAR;
import org.mapton.workbench.modules.map.AttributionView;
import org.mapton.workbench.modules.map.SearchView;
import org.mapton.workbench.modules.map.StatusBar;
import org.mapton.workbench.window.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MapModule extends MWorkbenchModule {

    private PopOver mAttributionPopOver;
    private AttributionView mAttributionView;
    private ToolbarItem mGoHomeToolbarItem;
    private final HashSet<PopOver> mPopOvers = new HashSet<>();
    private final HashMap<PopOver, Long> mPopoverClosingTimes = new HashMap<>();
    private SearchView mSearchView;
    private WindowManager mWindowManager;

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
        initListeners();
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
        mWindowManager.setBottom(StatusBar.getInstance());

        Platform.runLater(() -> {
            mAttributionPopOver = new PopOver();
            mAttributionView = new AttributionView(mAttributionPopOver);
            initPopOver(mAttributionPopOver, Dict.COPYRIGHT.toString(), mAttributionView);
            mAttributionPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        });
    }

    private void initAccelerators() {
        KeyCodeCombination kcc;
        kcc = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            activateSearch();
        });

        kcc = new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mGoHomeToolbarItem.onClickProperty().get().handle(null);
        });
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                updateDocumentInfo(evt);
            });
        }, MKey.MAP_DOCUMENT_INFO);
    }

    private void initPopOver(PopOver popOver, String title, Node content) {
        popOver.setTitle(title);
        popOver.setContentNode(content);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setCloseButtonEnabled(false);
        popOver.setDetachable(false);
        popOver.setAnimated(false);
        popOver.setOnHiding((windowEvent -> {
            mPopoverClosingTimes.put(popOver, System.currentTimeMillis());
        }));
        mPopOvers.add(popOver);
    }

    private void initToolbars() {
        mGoHomeToolbarItem = new ToolbarItem(MaterialIcon._Action.HOME.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            Mapton.getEngine().goHome();
        });
        setTooltip(mGoHomeToolbarItem, Dict.HOME.toString());

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
            mAttributionPopOver.show((Node) event.getSource());
        });
        setTooltip(attributionToolbarItem, Dict.COPYRIGHT.toString());

        var toolboxToolbarItem = new ToolbarItem(MaterialIcon._Places.BUSINESS_CENTER.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
        });
        setTooltip(toolboxToolbarItem, Dict.TOOLBOX.toString());

        ToolbarItem searchToolbarItem = new ToolbarItem(mSearchView.getPresenter());

        getToolbarControlsLeft().setAll(
                mGoHomeToolbarItem,
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

    private void updateDocumentInfo(GlobalStateChangeEvent evt) {
        MDocumentInfo documentInfo = evt.getValue();
        //aaamAttributionAction.setDisabled(false);
        //mStyleAction.setText(documentInfo.getName());
        mAttributionView.updateDocumentInfo(documentInfo);
    }
}
