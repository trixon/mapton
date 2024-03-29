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
package org.mapton.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javax.swing.SwingUtilities;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDict;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MKey;
import org.mapton.api.MToolMapCommand;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core.api.BaseToolBar;
import org.mapton.core.ui.bookmark.BookmarksView;
import org.mapton.core.ui.poi.PoisViewManager;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MapToolBar extends BaseToolBar {

    private final String CSS_FILE = getClass().getResource("toolbar_map.css").toExternalForm();
    private Action mAttributionAction;
    private PopOver mAttributionPopOver;
    private AttributionView mAttributionView;
    private Action mBookmarkAction;
    private PopOver mBookmarkPopOver;
    private Action mCommandAction;
    private ContextMenu mCommandContextMenu;
    private ObservableList<MenuItem> mCommandMenuItems;
    private FxActionSwing mHomeAction;
    private FxActionSwing mLayerAction;
    private PopOver mLayerPopOver;
    private Action mPoiAction;
    private PopOver mPoiPopOver;
    private Action mRulerAction;
    private PopOver mRulerPopOver;
    private FxActionSwing mStyleSwapAction;
    private Action mTemporalAction;
    private PopOver mTemporalPopOver;
    private TemporalView mTemporalView;

    public MapToolBar() {
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();

        refreshEngine();
        Mapton.getGlobalState().put(MKey.MAP_DOCUMENT_INFO, Mapton.getGlobalState().get(MKey.MAP_DOCUMENT_INFO));
    }

    public void toogleAttributionPopOver() {
        tooglePopOver(mAttributionPopOver, mAttributionAction);
    }

    public void toogleBookmarkPopOver() {
        tooglePopOver(mBookmarkPopOver, mBookmarkAction);
    }

    public void toogleCommandContextMenu() {
        FxHelper.runLater(() -> {
            if (mCommandContextMenu.isShowing()) {
                mCommandContextMenu.hide();
            } else if (shouldOpen(mCommandContextMenu)) {
                mCommandContextMenu.show(getButtonForAction(mCommandAction), Side.BOTTOM, 0, 0);
                getScene().getWindow().requestFocus();
                mCommandContextMenu.requestFocus();
            }
        });
    }

    public void toogleLayerPopOver() {
        tooglePopOver(mLayerPopOver, mLayerAction);
    }

    public void tooglePoiPopOver() {
        tooglePopOver(mPoiPopOver, mPoiAction);
    }

    public void toogleRulerPopOver() {
        if (Mapton.getEngine().getRulerView() != null) {
            tooglePopOver(mRulerPopOver, mRulerAction);
        }
    }

    public void toogleTemporalPopOver() {
        tooglePopOver(mTemporalPopOver, mTemporalAction);
    }

    private void init() {
        setStyle("-fx-spacing: 0px;");
        setPadding(Insets.EMPTY);

        var actions = new ArrayList<Action>();
        actions.addAll(Arrays.asList(
                mHomeAction,
                mCommandAction,
                mLayerAction,
                mStyleSwapAction,
                mAttributionAction,
                //mToolboxAction,
                mBookmarkAction,
                mPoiAction,
                ActionUtils.ACTION_SPAN,
                mTemporalAction,
                mRulerAction
        ));

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);

            storeButtonWidths(mAttributionAction, mTemporalAction, mRulerAction);
            FxHelper.adjustButtonHeight(getItems().stream(), getIconSizeToolBarInt() * 1.5);
            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeToolBarInt() * 1.0);
            FxHelper.undecorateButtons(this.getItems().stream());
            FxHelper.slimToolBar(this);
            setTextFromActions();

            mCommandContextMenu = new ContextMenu();
            mCommandContextMenu.setOnHiding(windowEvent -> {
                onObjectHiding(mCommandContextMenu);
            });
            mCommandMenuItems = mCommandContextMenu.getItems();
            Lookup.getDefault().lookupResult(MToolMapCommand.class).addLookupListener(event -> {
                populateCommands();
            });
            populateCommands();

            getButtonForAction(mPoiAction).setVisible(false);
            getButtonForAction(mBookmarkAction).setVisible(false);
        });
    }

    private void initActionsFx() {
        //Bookmark
        mBookmarkAction = new Action(Dict.BOOKMARKS.toString(), event -> {
            if (usePopOver(mBookmarkPopOver)) {
                if (shouldOpen(mBookmarkPopOver)) {
                    show(mBookmarkPopOver, this);
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "org.mapton.core.actions.BookmarkAction").actionPerformed(null);
                });
            }
        });

        //POI
        mPoiAction = new Action(MDict.POI.toString(), event -> {
            if (usePopOver(mPoiPopOver)) {
                if (shouldOpen(mPoiPopOver)) {
                    show(mPoiPopOver, this);
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "org.mapton.core.actions.PoiAction").actionPerformed(null);
                });
            }
        });

        //CommandAction
        mCommandAction = new Action(Dict.COMMANDS.toString(), event -> {
            toogleCommandContextMenu();
        });
        mCommandAction.setGraphic(MaterialIcon._Image.FLASH_ON.getImageView(getIconSizeToolBarInt()));
        FxHelper.setTooltip(mCommandAction, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));

        //Ruler
        mRulerAction = new Action(Dict.RULER.toString(), event -> {
            toogleRulerPopOver();
        });
        mRulerAction.setGraphic(MaterialIcon._Editor.SPACE_BAR.getImageView(getIconSizeToolBarInt()));
        mRulerAction.setDisabled(true);
        FxHelper.setTooltip(mRulerAction, new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN));
//        mRulerAction.textProperty().set(Dict.RULER.toString());
        mRulerAction.textProperty().bind(mRulerPopOver.titleProperty());

        //Temporal
        mTemporalAction = new Action(Dict.Time.DATE.toString(), event -> {
            toogleTemporalPopOver();
        });
        mTemporalAction.setGraphic(MaterialIcon._Action.DATE_RANGE.getImageView(getIconSizeToolBarInt()));
        FxHelper.setTooltip(mTemporalAction, new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN));
        mTemporalAction.textProperty().bind(mTemporalView.titleProperty());

        //Copyright
        mAttributionAction = new Action("Copyright", event -> {
            if (shouldOpen(mAttributionPopOver)) {
                show(mAttributionPopOver, event.getSource());
            }
        });
        mAttributionAction.setGraphic(MaterialIcon._Action.COPYRIGHT.getImageView(getIconSizeToolBarInt()));
        mAttributionAction.setDisabled(true);
    }

    private void initActionsSwing() {
        //Home
        mHomeAction = new FxActionSwing(Dict.HOME.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core.actions.HomeAction").actionPerformed(null);
        });
        mHomeAction.setGraphic(MaterialIcon._Action.HOME.getImageView(getIconSizeToolBarInt()));
        FxHelper.setTooltip(mHomeAction, new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));

        //Swap Style
        mStyleSwapAction = new FxActionSwing(MDict.PREVIOUS_STYLE.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core.actions.StyleSwapAction").actionPerformed(null);
        });
        mStyleSwapAction.setGraphic(MaterialIcon._Action.SWAP_HORIZ.getImageView(getIconSizeToolBarInt()));
        mStyleSwapAction.setDisabled(true);
        FxHelper.setTooltip(mStyleSwapAction, new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCodeCombination.SHIFT_DOWN));

        //Layer
        mLayerAction = new FxActionSwing(Dict.LAYERS.toString(), () -> {
            var tc = WindowManager.getDefault().findTopComponent("LayerTopComponent");

            if (tc.isOpened() && !mOptions.isMapOnly()) {
                tc.requestVisible();
                tc.requestAttention(true);
            } else {
                FxHelper.runLater(() -> {
                    if (shouldOpen(mLayerPopOver)) {
                        show(mLayerPopOver, getButtonForAction(mLayerAction));
                    }
                });
            }
        });

        mLayerAction.setGraphic(MaterialIcon._Maps.LAYERS.getImageView(getIconSizeToolBarInt()));
    }

    private void initListeners() {
        mOptions.mapOnlyProperty().addListener((p, o, n) -> {
            FxHelper.runLater(() -> mLayerPopOver.hide());
        });

        mOptions.engineProperty().addListener((p, o, n) -> {
            refreshEngine();
        });

        Mapton.getGlobalState().addListener(evt -> {
            Platform.runLater(() -> {
                updateDocumentInfo(evt.getValue());
            });
        }, MKey.MAP_DOCUMENT_INFO);
    }

    private void initPopOvers() {
        mBookmarkPopOver = new PopOver();
        initPopOver(mBookmarkPopOver, Dict.BOOKMARKS.toString(), new BookmarksView(mBookmarkPopOver), false);
        mBookmarkPopOver.setArrowLocation(ArrowLocation.TOP_CENTER);

        mPoiPopOver = new PopOver();
        initPopOver(mPoiPopOver, MDict.POI.toString(), PoisViewManager.getInstance().getPoisView(), false);
        mPoiPopOver.setOnShowing(event -> {
            mPoiPopOver.setContentNode(null);
            mPoiPopOver.setContentNode(PoisViewManager.getInstance().getPoisView());
            setPopOverWidths(FxHelper.getUIScaled(DEFAULT_POP_OVER_WIDTH), mPoiPopOver);
        });
        mPoiPopOver.setArrowLocation(ArrowLocation.TOP_CENTER);

        mLayerPopOver = new PopOver();
        initPopOver(mLayerPopOver, Dict.LAYERS.toString(), null, false);
        mLayerPopOver.setOnShowing(event -> {
            mLayerPopOver.setContentNode(null);
            mLayerPopOver.setContentNode(LayerView.getInstance());
            setPopOverWidths(FxHelper.getUIScaled(DEFAULT_POP_OVER_WIDTH), mLayerPopOver);
        });
        mLayerPopOver.setArrowLocation(ArrowLocation.TOP_LEFT);

        mRulerPopOver = new PopOver();
        initPopOver(mRulerPopOver, Dict.RULER.toString(), new RulerView(), true);
        mRulerPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        mRulerPopOver.setAutoHide(false);
        mRulerPopOver.setCloseButtonEnabled(true);
        mRulerPopOver.setDetachable(true);

        mTemporalPopOver = new PopOver();
        mTemporalView = new TemporalView();
        initPopOver(mTemporalPopOver, Dict.Time.DATE.toString(), mTemporalView, true);
        mTemporalPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        mTemporalPopOver.setAutoHide(false);
        mTemporalPopOver.setCloseButtonEnabled(true);
        mTemporalPopOver.setDetachable(true);
        mTemporalPopOver.setOnShowing(event -> {
            getButtonForAction(mTemporalAction).getStylesheets().remove(CSS_FILE);
        });
        mTemporalPopOver.setOnHiding(event -> {
            getButtonForAction(mTemporalAction).getStylesheets().add(CSS_FILE);
            onObjectHiding(mTemporalPopOver);
        });

        setPopOverWidths(FxHelper.getUIScaled(DEFAULT_POP_OVER_WIDTH), mBookmarkPopOver, mPoiPopOver);

        Platform.runLater(() -> {
            mAttributionPopOver = new PopOver();
            mAttributionView = new AttributionView(mAttributionPopOver);
            initPopOver(mAttributionPopOver, Dict.COPYRIGHT.toString(), mAttributionView, true);
            mAttributionPopOver.setArrowLocation(ArrowLocation.TOP_LEFT);
        });
    }

    private void populateCommands() {
        new Thread(() -> {
            synchronized (this) {
                mCommandMenuItems.clear();
                Lookup.getDefault().lookupAll(MToolMapCommand.class).forEach(command -> {
                    var menuItem = new MenuItem(command.getAction().getText());
                    menuItem.setAccelerator(command.getKeyCodeCombination());
                    menuItem.setOnAction(actionEvent -> {
                        command.getAction().handle(null);
                    });
                    mCommandMenuItems.add(menuItem);
                });

                if (!mCommandMenuItems.isEmpty()) {
                    mCommandMenuItems.sort((o1, o2) -> o1.getText().compareTo(o2.getText()));
                }
            }
        }, getClass().getCanonicalName()).start();
    }

    private void refreshEngine() {
        mStyleSwapAction.setDisabled(Mapton.getEngine().getLayerBackgroundView() == null);
        mRulerAction.setDisabled(Mapton.getEngine().getRulerView() == null);
    }

    private void updateDocumentInfo(MDocumentInfo documentInfo) {
        mAttributionAction.setDisabled(false);

        if (documentInfo != null) {
            mAttributionAction.setText(documentInfo.getName());
        }
    }
}
