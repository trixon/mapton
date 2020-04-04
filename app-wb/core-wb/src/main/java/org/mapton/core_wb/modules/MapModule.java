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
package org.mapton.core_wb.modules;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MActivatable;
import org.mapton.api.MDict;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import org.mapton.base.ui.AttributionView;
import org.mapton.base.ui.SearchView;
import org.mapton.base.ui.StatusBarView;
import org.mapton.base.ui.TemporalView;
import org.mapton.base.ui.bookmark.BookmarksView;
import org.mapton.base.ui.grid.GridView;
import org.mapton.base.ui.grid.LocalGridsView;
import org.mapton.core_wb.MaptonApplication;
import org.mapton.core_wb.TitledDrawerContent;
import org.mapton.core_wb.api.MWorkbenchModule;
import org.mapton.core_wb.modules.map.LocalGridEditor;
import org.mapton.core_wb.modules.map.MapWindow;
import org.mapton.core_wb.modules.map.ToolboxView;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.windowsystemfx.WindowManager;

/**
 *
 * @author Patrik Karlström
 */
public class MapModule extends MWorkbenchModule {

    private ToolbarItem mAttributionToolbarItem;
    private AttributionView mAttributionView;
    private ToolbarItem mBookmarkToolbarItem;
    private TitledDrawerContent mBookmarksDrawerContent;
    private Node mDrawerContent;
    private ToolbarItem mGoHomeToolbarItem;
    private TitledDrawerContent mGridDrawerContent;
    private ToolbarItem mGridToolbarItem;
    private ToolbarItem mLayerToolbarItem;
    private TitledDrawerContent mLayersDrawerContent;
    private ToolbarItem mMapOnlyToolbarItem;
    private final HashSet<PopOver> mPopOvers = new HashSet<>();
    private final BorderPane mRoot;
    private PopOver mRulerPopOver;
    private ToolbarItem mRulerToolbarItem;
    private SearchView mSearchView;
    private ToolbarItem mStyleToolbarItem;
    private PopOver mTemporalPopOver;
    private ToolbarItem mTemporalToolbarItem;
    private TitledDrawerContent mToolDrawerContent;
    private ToolbarItem mToolboxToolbarItem;
    private WindowManager mWindowManager = WindowManager.getInstance();
    private ToolbarItem mWindowToolbarItem;
    private WorkbenchDialog mWorkbenchDialog;

    public MapModule() {
        super(Dict.MAP.toString(), MaterialDesignIcon.MAP);
        MaskerPane maskerPane = new MaskerPane();
        maskerPane.setText(NbBundle.getMessage(MaptonApplication.class, "loading_map"));
        mRoot = new BorderPane(maskerPane);
        mWorkbenchDialog = WorkbenchDialog.builder("", new Label(), WorkbenchDialog.Type.INFORMATION).build();
        mAttributionView = new AttributionView();
        mWindowManager.init();

        new Thread(() -> {
            createUI();
            initPopOvers();

            Platform.runLater(() -> {
                initListeners();
                initToolbars();
                mRoot.setCenter(mWindowManager.getRoot());
                mRoot.setBottom(StatusBarView.getInstance());
                refreshUI();
                refreshEngine();
            });
        }).start();
    }

    @Override
    public Node activate() {
        initAccelerators();

        return mRoot;
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
    }

    private void createUI() {
        mSearchView = new SearchView();

        mBookmarksDrawerContent = new TitledDrawerContent(Dict.BOOKMARKS.toString(), new BookmarksView(null));
        mGridDrawerContent = new TitledDrawerContent(MDict.GRID.toString(), new GridView(null));
        mToolDrawerContent = new TitledDrawerContent(Dict.TOOLBOX.toString(), new ToolboxView());

        LocalGridsView.setLocalGridEditor(LocalGridEditor.getInstance());
    }

    private void initAccelerators() {
        KeyCodeCombination kcc;

        kcc = new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mBookmarkToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mTemporalToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mSearchView.getPresenter().requestFocus();
            ((TextField) mSearchView.getPresenter()).clear();
        });

        kcc = new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mGridToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mGoHomeToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mAttributionToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            if (!mLayerToolbarItem.isDisabled()) {
                mLayerToolbarItem.getOnClick().handle(null);
            }
        });

        kcc = new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mRulerToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mStyleToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mToolboxToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mWindowToolbarItem.fire();
        });

//        kcc = new KeyCodeCombination(KeyCode.F12, KeyCombination.SHORTCUT_ANY);
//        getKeyCodeCombinations().add(kcc);
//        getAccelerators().put(kcc, () -> {
//            mMapOnlyToolbarItem.getOnClick().handle(null);
//        });
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                updateDocumentInfo(evt.getValue());
            });
        }, MKey.MAP_DOCUMENT_INFO);

        mOptions2.general().engineProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            refreshEngine();
        });

        mOptions2.general().maximizedMapProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            refreshUI();
        });

        getWorkbench().drawerShownProperty().addListener((ObservableValue<? extends Region> observable, Region oldValue, Region newValue) -> {
            mDrawerContent = newValue;
        });
    }

    private void initPopOver(PopOver popOver, String title, Node content) {
        popOver.setTitle(title);
        popOver.setContentNode(content);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setCloseButtonEnabled(true);
        popOver.setDetachable(true);
        popOver.setAnimated(true);
        popOver.setAutoHide(false);

        mPopOvers.add(popOver);
    }

    private void initPopOvers() {
        initPopOver(mRulerPopOver = new PopOver(), Dict.RULER.toString(), Mapton.getEngine().getRulerView());
        initPopOver(mTemporalPopOver = new PopOver(), Dict.DATE.toString(), new TemporalView());
        mTemporalPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
    }

    private void initToolbars() {
        mGoHomeToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.HOME), event -> {
            Mapton.getEngine().goHome();
        });
        setTooltip(mGoHomeToolbarItem, Dict.HOME.toString(), new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN));

        ToolbarItem searchToolbarItem = new ToolbarItem(mSearchView.getPresenter());
        setTooltip(searchToolbarItem, Dict.SEARCH.toString(), new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));
        mRulerToolbarItem = new ToolbarItem(Dict.RULER.toString(), new MaterialDesignIconView(MaterialDesignIcon.RULER), event -> {
            if (shouldOpen(mRulerPopOver)) {
                mRulerPopOver.show(mRulerToolbarItem);
            }
        });
        setTooltip(mRulerToolbarItem, Dict.RULER.toString(), new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));

        mLayerToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.LAYERS), event -> {
            showDrawer(mLayersDrawerContent, Side.LEFT);
        });
        setTooltip(mLayerToolbarItem, Dict.LAYERS.toString(), new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN));

        mGridToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.GRID), event -> {
            showDrawer(mGridDrawerContent, Side.LEFT);
        });
        setTooltip(mGridToolbarItem, MDict.GRID.toString(), new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN));

        mBookmarkToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.BOOKMARK), event -> {
            showDrawer(mBookmarksDrawerContent, Side.LEFT);
        });
        setTooltip(mBookmarkToolbarItem, Dict.BOOKMARKS.toString(), new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN));

        mStyleToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.PALETTE), event -> {
            showDialog(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), Mapton.getEngine().getStyleView());
        });
        setTooltip(mStyleToolbarItem, Dict.STYLE.toString(), new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        mStyleToolbarItem.setDisable(true);

        mAttributionToolbarItem = new ToolbarItem(new MaterialDesignIconView(MaterialDesignIcon.COPYRIGHT), event -> {
            showDialog(Dict.COPYRIGHT.toString(), mAttributionView);
        });
        //mAttributionToolbarItem.setDisable(true);
        setTooltip(mAttributionToolbarItem, Dict.COPYRIGHT.toString(), new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));

        Action diagramAction = new Action(Dict.CHART.toString(), (event) -> {
            System.out.println(event);
        });

        Action objectPropertiesAction = new Action(Dict.OBJECT_PROPERTIES.toString(), (event) -> {
            System.out.println(event);
        });
        Action toolsAction = new Action(Dict.TOOLS.toString(), (event) -> {
            System.out.println(event);
        });

        mToolboxToolbarItem = new ToolbarItem(Dict.TOOLBOX.toString(), new MaterialDesignIconView(MaterialDesignIcon.BRIEFCASE), event -> {
            showDrawer(mToolDrawerContent, Side.RIGHT);
        });

        mTemporalToolbarItem = new ToolbarItem(Dict.DATE.toString(), new MaterialDesignIconView(MaterialDesignIcon.CALENDAR_RANGE), event -> {
            if (shouldOpen(mTemporalPopOver)) {
                mTemporalPopOver.show(mTemporalToolbarItem);
            }
        });

        mWindowToolbarItem = new ToolbarItem(Dict.WINDOW.toString(), new MaterialDesignIconView(MaterialDesignIcon.WINDOW_MAXIMIZE),
                ActionUtils.createMenuItem(toolsAction),
                ActionUtils.createMenuItem(objectPropertiesAction),
                ActionUtils.createMenuItem(diagramAction)
        );

        mMapOnlyToolbarItem = new ToolbarItem(
                mOptions2.general().isMaximizedMap() ? new MaterialDesignIconView(MaterialDesignIcon.FULLSCREEN_EXIT) : new MaterialDesignIconView(MaterialDesignIcon.FULLSCREEN), event -> {
            mOptions2.general().maximizedMapProperty().set(!mOptions2.general().isMaximizedMap());
        });
        setTooltip(mMapOnlyToolbarItem, NbBundle.getMessage(MOptions2.class, "maximize_map"));

        getToolbarControlsLeft().setAll(
                mGoHomeToolbarItem,
                searchToolbarItem,
                mBookmarkToolbarItem,
                mLayerToolbarItem,
                mGridToolbarItem,
                mAttributionToolbarItem,
                mStyleToolbarItem
        );

        getToolbarControlsRight().setAll(
                mRulerToolbarItem,
                mTemporalToolbarItem
        //                mToolboxToolbarItem,
        //                mWindowToolbarItem,
        //                mMapOnlyToolbarItem
        );
    }

    private void refreshEngine() {
        MEngine engine = Mapton.getEngine();
        Node layerView = engine.getLayerView();
        if (layerView != null) {
            mLayersDrawerContent = new TitledDrawerContent(Dict.LAYERS.toString(), layerView);
        }

        mStyleToolbarItem.setDisable(engine.getStyleView() == null);
        mLayerToolbarItem.setDisable(layerView == null);
        mRulerToolbarItem.setDisable(engine.getRulerView() == null);
        mRulerPopOver.setContentNode(engine.getRulerView());

        Mapton.getGlobalState().put(MKey.MAP_DOCUMENT_INFO, Mapton.getGlobalState().get(MKey.MAP_DOCUMENT_INFO));
    }

    private void refreshUI() {
        if (mOptions2.general().isMaximizedMap()) {
            mMapOnlyToolbarItem.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.FULLSCREEN_EXIT));
            mWindowManager.showOnlyWindowById(MapWindow.ID);
        } else {
            mMapOnlyToolbarItem.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.FULLSCREEN));
            mWindowManager.showRoot();
        }
        Mapton.getEngine().refreshUI();
    }

    private boolean shouldOpen(PopOver popOver) {
        boolean shouldOpen = !popOver.isShowing();
        popOver.hide();

        return shouldOpen;
    }

    private void showDialog(String title, Node content) {
        if (content == mWorkbenchDialog.getContent()) {
            getWorkbench().hideDialog(mWorkbenchDialog);
            return;
        }

        getWorkbench().hideDialog(mWorkbenchDialog);
        mWorkbenchDialog = WorkbenchDialog.builder(title, content, WorkbenchDialog.Type.INFORMATION).showButtonsBar(false).build();
        mWorkbenchDialog.showingProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                mWorkbenchDialog.setContent(null);
            }
        });

        getWorkbench().showDialog(mWorkbenchDialog);
    }

    private void showDrawer(Node content, Side side) {
        if (content == mDrawerContent) {
            getWorkbench().hideDrawer();
            return;
        }

        getWorkbench().showDrawer((Region) content, side, 20);
        if (content instanceof MActivatable) {
            ((MActivatable) content).activate();
        }
    }

    private void tooglePopOver(PopOver popOver, ToolbarItem toolbarItem) {
        Platform.runLater(() -> {
            if (popOver.isShowing()) {
                popOver.hide();
            } else {
                mPopOvers.forEach((item) -> {
                    item.hide();
                });

                toolbarItem.getOnClick().handle(null);
            }
        });
    }

    private void updateDocumentInfo(MDocumentInfo documentInfo) {
        mAttributionToolbarItem.setDisable(false);
        mStyleToolbarItem.setText(documentInfo.getName());
    }
}
