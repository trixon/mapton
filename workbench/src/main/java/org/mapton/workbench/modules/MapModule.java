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
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.util.HashSet;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDict;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MKey;
import org.mapton.api.MOptions2;
import org.mapton.api.MWorkbenchModule;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE;
import static org.mapton.api.Mapton.ICON_SIZE_MODULE_TOOLBAR;
import org.mapton.workbench.MaptonApplication;
import org.mapton.workbench.modules.map.AttributionView;
import org.mapton.workbench.modules.map.MapWindow;
import org.mapton.workbench.modules.map.RulerStage;
import org.mapton.workbench.modules.map.SearchView;
import org.mapton.workbench.modules.map.StatusBar;
import org.mapton.workbench.modules.map.ToolboxView;
import org.mapton.workbench.window.WindowManager;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MapModule extends MWorkbenchModule {

    private PopOver mAttributionPopOver;
    private ToolbarItem mAttributionToolbarItem;
    private AttributionView mAttributionView;
    private ToolbarItem mBookmarkToolbarItem;
    private ToolbarItem mGoHomeToolbarItem;
    private ToolbarItem mLayerToolbarItem;
    private ToolbarItem mMapOnlyToolbarItem;
    private final HashSet<PopOver> mPopOvers = new HashSet<>();
    private final BorderPane mRoot;
    private RulerStage mRulerStage;
    private ToolbarItem mRulerToolbarItem;
    private SearchView mSearchView;
    private PopOver mStylePopOver;
    private ToolbarItem mStyleToolbarItem;
    private PopOver mToolboxPopOver;
    private ToolbarItem mToolboxToolbarItem;
    private WindowManager mWindowManager;
    private ToolbarItem mWindowToolbarItem;
    private WorkbenchDialog mWorkbenchDialog;

    public MapModule() {
        super(Dict.MAP.toString(), MaterialIcon._Maps.MAP.getImageView(ICON_SIZE_MODULE).getImage());
        MaskerPane maskerPane = new MaskerPane();
        maskerPane.setText(NbBundle.getMessage(MaptonApplication.class, "loading_map"));
        mRoot = new BorderPane(maskerPane);
        mWorkbenchDialog = WorkbenchDialog.builder("", new Label(), WorkbenchDialog.Type.INFORMATION).build();
        mRulerStage = new RulerStage();

        new Thread(() -> {
            createUI();
            initPopOvers();
            initListeners();

            Platform.runLater(() -> {
                initToolbars();
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
        KeyCodeCombination kcc;

        kcc = new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mBookmarkToolbarItem.getOnClick().handle(null);
        });

        kcc = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            activateSearch();
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
            mLayerToolbarItem.getOnClick().handle(null);
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
            tooglePopOver(mToolboxPopOver, mToolboxToolbarItem);
        });

        kcc = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mWindowToolbarItem.fire();
        });

        kcc = new KeyCodeCombination(KeyCode.F12, KeyCombination.SHORTCUT_ANY);
        getKeyCodeCombinations().add(kcc);
        getAccelerators().put(kcc, () -> {
            mMapOnlyToolbarItem.getOnClick().handle(null);
        });
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                updateDocumentInfo(evt);
            });
        }, MKey.MAP_DOCUMENT_INFO);

        mOptions2.general().engineProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            refreshEngine();
        });

        mOptions2.general().maximizedMapProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            refreshUI();
        });
    }

    private void initPopOver(PopOver popOver, String title, Node content) {
        popOver.setTitle(title);
        popOver.setContentNode(content);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setCloseButtonEnabled(false);
        popOver.setDetachable(false);
        popOver.setAnimated(false);

        mPopOvers.add(popOver);
    }

    private void initPopOvers() {
        mToolboxPopOver = new PopOver();
        initPopOver(mToolboxPopOver, Dict.TOOLBOX.toString(), new ToolboxView());
        mToolboxPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);

        mStylePopOver = new PopOver();
        initPopOver(mStylePopOver, String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), new BorderPane());

        Platform.runLater(() -> {
            mAttributionPopOver = new PopOver();
            mAttributionView = new AttributionView(mAttributionPopOver);
            initPopOver(mAttributionPopOver, Dict.COPYRIGHT.toString(), mAttributionView);
            mAttributionPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
        });
    }

    private void initToolbars() {
        mGoHomeToolbarItem = new ToolbarItem(MaterialIcon._Action.HOME.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            Mapton.getEngine().goHome();
        });
        setTooltip(mGoHomeToolbarItem, Dict.HOME.toString(), new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN));

        ToolbarItem searchToolbarItem = new ToolbarItem(mSearchView.getPresenter());
        setTooltip(searchToolbarItem, Dict.SEARCH.toString(), new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN));

        mRulerToolbarItem = new ToolbarItem(
                Dict.RULER.toString(),
                MaterialIcon._Editor.SPACE_BAR.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> {
                    mRulerStage.show();
                });
        setTooltip(mRulerToolbarItem, Dict.RULER.toString(), new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));

        mLayerToolbarItem = new ToolbarItem(MaterialIcon._Maps.LAYERS.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            showDialog(Dict.LAYERS.toString(), Mapton.getEngine().getLayerView());
        });
        setTooltip(mLayerToolbarItem, Dict.LAYERS.toString(), new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN));

        mBookmarkToolbarItem = new ToolbarItem(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            showDialog(Dict.BOOKMARKS.toString(), new Button());
        });
        setTooltip(mBookmarkToolbarItem, Dict.BOOKMARKS.toString(), new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN));

        mStyleToolbarItem = new ToolbarItem(
                "OpenStreetMap",
                MaterialIcon._Image.COLOR_LENS.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> {
                    showDialog(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), Mapton.getEngine().getStyleView());
                });
        setTooltip(mStyleToolbarItem, Dict.STYLE.toString(), new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        mStyleToolbarItem.setDisable(true);

        mAttributionToolbarItem = new ToolbarItem(MaterialIcon._Action.COPYRIGHT.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            showDialog(Dict.COPYRIGHT.toString(), mAttributionView);
        });
        //mAttributionToolbarItem.setDisable(true);
        setTooltip(mAttributionToolbarItem, Dict.COPYRIGHT.toString(), new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN));

        Action diagramAction = new Action(Dict.CHART.toString(), (event) -> {
            System.out.println(event);
        });

        Action temporalAction = new Action(Dict.Time.DATE.toString(), (event) -> {
            System.out.println(event);
        });
        Action gridAction = new Action(MDict.GRIDS.toString(), (event) -> {
            System.out.println(event);
        });
        Action objectPropertiesAction = new Action(Dict.OBJECT_PROPERTIES.toString(), (event) -> {
            System.out.println(event);
        });
        Action toolsAction = new Action(Dict.TOOLS.toString(), (event) -> {
            System.out.println(event);
        });

        mToolboxToolbarItem = new ToolbarItem(
                Dict.TOOLBOX.toString(),
                MaterialIcon._Places.BUSINESS_CENTER.getImageView(ICON_SIZE_MODULE_TOOLBAR), event -> {
            if (shouldOpen(mToolboxPopOver)) {
                mToolboxPopOver.show(mToolboxToolbarItem);
            }
        });

        mWindowToolbarItem = new ToolbarItem(
                Dict.WINDOW.toString(),
                MaterialIcon._Av.WEB_ASSET.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                ActionUtils.createMenuItem(temporalAction),
                ActionUtils.createMenuItem(gridAction),
                ActionUtils.createMenuItem(toolsAction),
                ActionUtils.createMenuItem(objectPropertiesAction),
                ActionUtils.createMenuItem(diagramAction)
        );

        mMapOnlyToolbarItem = new ToolbarItem(
                mOptions2.general().isMaximizedMap() ? MaterialIcon._Navigation.FULLSCREEN_EXIT.getImageView(ICON_SIZE_MODULE_TOOLBAR) : MaterialIcon._Navigation.FULLSCREEN.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> {
                    mOptions2.general().maximizedMapProperty().set(!mOptions2.general().isMaximizedMap());
                });
        setTooltip(mMapOnlyToolbarItem, NbBundle.getMessage(MOptions2.class, "maximize_map"));

        getToolbarControlsLeft().setAll(
                mGoHomeToolbarItem,
                searchToolbarItem,
                mBookmarkToolbarItem,
                mLayerToolbarItem,
                mAttributionToolbarItem,
                mStyleToolbarItem
        );

        getToolbarControlsRight().setAll(
                mRulerToolbarItem,
                mToolboxToolbarItem,
                mWindowToolbarItem,
                mMapOnlyToolbarItem
        );
    }

    private void refreshEngine() {
        mStyleToolbarItem.setDisable(Mapton.getEngine().getStyleView() == null);
        mLayerToolbarItem.setDisable(Mapton.getEngine().getLayerView() == null);
        mRulerToolbarItem.setDisable(Mapton.getEngine().getRulerView() == null);
    }

    private void refreshUI() {
        if (mOptions2.general().isMaximizedMap()) {
            mMapOnlyToolbarItem.setGraphic(MaterialIcon._Navigation.FULLSCREEN_EXIT.getImageView(ICON_SIZE_MODULE_TOOLBAR));
            mRoot.setCenter(MapWindow.getInstance());
            mRoot.setBottom(StatusBar.getInstance());
        } else {
            mMapOnlyToolbarItem.setGraphic(MaterialIcon._Navigation.FULLSCREEN.getImageView(ICON_SIZE_MODULE_TOOLBAR));
            mWindowManager.setBottom(StatusBar.getInstance());
            mRoot.setCenter(mWindowManager);
            mRoot.setBottom(null);
        }
    }

    private boolean shouldOpen(PopOver popOver) {
        boolean shouldOpen = !popOver.isShowing();
        popOver.hide();

        return shouldOpen;
    }

    private void showDialog(String title, Node content) {
        if (mWorkbenchDialog.getTitle().equalsIgnoreCase(title)) {
            getWorkbench().hideDialog(mWorkbenchDialog);
            mWorkbenchDialog.setTitle("xxx");
            return;
        }
        getWorkbench().hideDialog(mWorkbenchDialog);
        mWorkbenchDialog = WorkbenchDialog.builder(title, content, WorkbenchDialog.Type.INFORMATION).showButtonsBar(false).build();

        getWorkbench().showDialog(mWorkbenchDialog);
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

    private void updateDocumentInfo(GlobalStateChangeEvent evt) {
        MDocumentInfo documentInfo = evt.getValue();
        mAttributionToolbarItem.setDisable(false);
        mStyleToolbarItem.setText(documentInfo.getName());
        mAttributionView.updateDocumentInfo(documentInfo);
    }
}
