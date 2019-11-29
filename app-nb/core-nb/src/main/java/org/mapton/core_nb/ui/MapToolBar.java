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
package org.mapton.core_nb.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeContextMenu;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.base.ui.AttributionView;
import org.mapton.base.ui.SearchView;
import org.mapton.base.ui.TemporalView;
import org.mapton.base.ui.bookmark.BookmarksView;
import org.openide.awt.Actions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.almond.util.fx.FxActionSwingCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MapToolBar extends ToolBar {

    private Action mAttributionAction;
    private PopOver mAttributionPopOver;
    private AttributionView mAttributionView;
    private Action mBookmarkAction;
    private PopOver mBookmarkPopOver;
    private final HashMap<Action, Double> mButtonWidths = new HashMap<>();
    private FxActionSwing mHomeAction;
    private Action mLayerAction;
    private PopOver mLayerPopOver;
    private final MOptions mOptions = MOptions.getInstance();
    private final HashSet<PopOver> mPopOvers = new HashSet<>();
    private final HashMap<PopOver, Long> mPopoverClosingTimes = new HashMap<>();
    private Action mRulerAction;
    private PopOver mRulerPopOver;
    private SearchView mSearchView;
    private Action mStyleAction;
    private PopOver mStylePopOver;
    private FxActionSwingCheck mSysViewMapAction;
    private Action mTemporalAction;
    private PopOver mTemporalPopOver;
    private TemporalView mTemporalView;
    private Action mToolboxAction;
    private PopOver mToolboxPopOver;

    public MapToolBar() {
        initPopOvers();
        initActionsFx();
        initActionsSwing();
        init();
        initListeners();
    }

    public void activateSearch() {
        Platform.runLater(() -> {
            getScene().getWindow().requestFocus();
            mSearchView.getPresenter().requestFocus();
            ((TextField) mSearchView.getPresenter()).clear();
        });
    }

    public void toogleAttributionPopOver() {
        tooglePopOver(mAttributionPopOver, mAttributionAction);
    }

    public void toogleBookmarkPopOver() {
        tooglePopOver(mBookmarkPopOver, mBookmarkAction);
    }

    public void toogleLayerPopOver() {
        tooglePopOver(mLayerPopOver, mLayerAction);
    }

    public void toogleRulerPopOver() {
        tooglePopOver(mRulerPopOver, mRulerAction);
    }

    public void toogleStylePopOver() {
        tooglePopOver(mStylePopOver, mStyleAction);
    }

    public void toogleTemporalPopOver() {
        tooglePopOver(mTemporalPopOver, mTemporalAction);
    }

    public void toogleToolboxPopOver() {
        tooglePopOver(mToolboxPopOver, mToolboxAction);
    }

    void refreshEngine(MEngine engine) {
        mStyleAction.setDisabled(engine.getStyleView() == null);
    }

    private ButtonBase getButtonForAction(Action action) {
        for (Node item : getItems()) {
            if (item instanceof ButtonBase) {
                ButtonBase buttonBase = (ButtonBase) item;
                if (buttonBase.getOnAction().equals(action)) {
                    return buttonBase;
                }
            }
        }

        return null;
    }

    private void init() {
        setStyle("-fx-spacing: 0px;");
        setPadding(Insets.EMPTY);

        ArrayList<Action> actions = new ArrayList<>();
        actions.addAll(Arrays.asList(
                mHomeAction,
                mBookmarkAction,
                mLayerAction,
                mAttributionAction,
                mStyleAction,
                ActionUtils.ACTION_SPAN,
                mRulerAction,
                mTemporalAction,
                mToolboxAction,
                mSysViewMapAction
        ));

        Platform.runLater(() -> {
            ActionUtils.updateToolBar(this, actions, ActionUtils.ActionTextBehavior.HIDE);

            storeButtonWidths(mStyleAction, mRulerAction, mTemporalAction, mToolboxAction);
            FxHelper.adjustButtonWidth(getItems().stream(), getIconSizeContextMenu() * 1.5);
            setTextFromActions();

            getItems().stream().filter((item) -> (item instanceof ButtonBase))
                    .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
                FxHelper.undecorateButton(buttonBase);
            });

            mSearchView = new SearchView();
            getItems().add(1, mSearchView.getPresenter());
        });
    }

    private void initActionsFx() {
        //Bookmark
        mBookmarkAction = new Action(Dict.BOOKMARKS.toString(), (ActionEvent event) -> {
            if (usePopOver()) {
                if (shouldOpen(mLayerPopOver)) {
                    mBookmarkPopOver.show((Node) event.getSource());
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "org.mapton.core_nb.actions.BookmarkAction").actionPerformed(null);
                });
            }
        });
        mBookmarkAction.setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBarInt()));
        mBookmarkAction.setSelected(mOptions.isBookmarkVisible());

        //Layer
        mLayerAction = new Action(Dict.LAYERS.toString(), (ActionEvent event) -> {
            if (usePopOver()) {
                if (shouldOpen(mLayerPopOver)) {
                    mLayerPopOver.show((Node) event.getSource());
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "org.mapton.core_nb.actions.LayerAction").actionPerformed(null);
                });
            }
        });
        mLayerAction.setGraphic(MaterialIcon._Maps.LAYERS.getImageView(getIconSizeToolBarInt()));
        mLayerAction.setSelected(mOptions.isBookmarkVisible());

        //mToolbox
        mToolboxAction = new Action(Dict.TOOLBOX.toString(), (event) -> {
            if (usePopOver()) {
                if (shouldOpen(mToolboxPopOver)) {
                    mToolboxPopOver.show((Node) event.getSource());
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    Actions.forID("Mapton", "org.mapton.core_nb.actions.ToolboxAction").actionPerformed(null);
                });
            }
        });
        mToolboxAction.setGraphic(MaterialIcon._Places.BUSINESS_CENTER.getImageView(getIconSizeToolBarInt()));

        //Style
        mStyleAction = new Action(String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), (ActionEvent event) -> {
            if (shouldOpen(mStylePopOver)) {
                BorderPane pane = (BorderPane) mStylePopOver.getContentNode();
                pane.setCenter(Mapton.getEngine().getStyleView());
                mStylePopOver.show((Node) event.getSource());
            }
        });
        mStyleAction.setGraphic(MaterialIcon._Image.COLOR_LENS.getImageView(getIconSizeToolBarInt()));
        mStyleAction.setDisabled(true);

        //Ruler
        mRulerAction = new Action(Dict.MEASURE.toString(), (event) -> {
            toogleRulerPopOver();
        });
        mRulerAction.setGraphic(MaterialIcon._Editor.SPACE_BAR.getImageView(getIconSizeToolBarInt()));

        //Temporal
        mTemporalAction = new Action(Dict.Time.DATE.toString(), (ActionEvent event) -> {
            toogleTemporalPopOver();
        });
        mTemporalAction.setGraphic(MaterialIcon._Action.DATE_RANGE.getImageView(getIconSizeToolBarInt()));

        //Copyright
        mAttributionAction = new Action("Copyright", (ActionEvent event) -> {
            if (shouldOpen(mAttributionPopOver)) {
                mAttributionPopOver.show((Node) event.getSource());
            }
        });
        mAttributionAction.setGraphic(MaterialIcon._Action.COPYRIGHT.getImageView(getIconSizeToolBarInt()));
        mAttributionAction.setDisabled(true);
    }

    private void initActionsSwing() {
        //Home
        mHomeAction = new FxActionSwing(Dict.HOME.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.HomeAction").actionPerformed(null);
        });
        mHomeAction.setGraphic(MaterialIcon._Action.HOME.getImageView(getIconSizeToolBarInt()));

        //Map
        mSysViewMapAction = new FxActionSwingCheck(Dict.MAP.toString(), () -> {
            Actions.forID("Mapton", "org.mapton.core_nb.actions.OnlyMapAction").actionPerformed(null);
        });
        mSysViewMapAction.setGraphic(MaterialIcon._Maps.MAP.getImageView(getIconSizeToolBarInt()));
        mSysViewMapAction.setAccelerator(KeyCombination.keyCombination("F12"));
        mSysViewMapAction.setSelected(mOptions.isMapOnly());
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case MOptions.KEY_MAP_ONLY:
                    mSysViewMapAction.setSelected(mOptions.isMapOnly());
                    break;

                default:
                    break;
            }
        });

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                updateDocumentInfo(evt);
            });
        }, MKey.MAP_DOCUMENT_INFO);
    }

    private void initPopOver(PopOver popOver, String title, Node content) {
        popOver.setTitle(title);
        popOver.setContentNode(content);
        popOver.setArrowLocation(ArrowLocation.TOP_LEFT);
        popOver.setHeaderAlwaysVisible(true);
        popOver.setCloseButtonEnabled(false);
        popOver.setDetachable(false);
        popOver.setAnimated(false);
        popOver.setOnHiding(windowEvent -> {
            mPopoverClosingTimes.put(popOver, System.currentTimeMillis());
        });
        mPopOvers.add(popOver);
    }

    private void initPopOvers() {
        mBookmarkPopOver = new PopOver();
        initPopOver(mBookmarkPopOver, Dict.BOOKMARKS.toString(), new BookmarksView());

        mLayerPopOver = new PopOver();
        initPopOver(mLayerPopOver, Dict.LAYERS.toString(), null);
        mLayerPopOver.setOnShowing(event -> {
            mLayerPopOver.setContentNode(new LayerView());
        });

        mStylePopOver = new PopOver();
        initPopOver(mStylePopOver, String.format("%s & %s", Dict.TYPE.toString(), Dict.STYLE.toString()), new BorderPane());

        mToolboxPopOver = new PopOver();
        initPopOver(mToolboxPopOver, Dict.TOOLBOX.toString(), new ToolboxView());
        mToolboxPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);

        mRulerPopOver = new PopOver();
        initPopOver(mRulerPopOver, Dict.RULER.toString(), new RulerView());
        mRulerPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        mRulerPopOver.setAutoHide(false);
        mRulerPopOver.setCloseButtonEnabled(true);
        mRulerPopOver.setDetachable(true);

        mTemporalPopOver = new PopOver();
        mTemporalView = new TemporalView();
        initPopOver(mTemporalPopOver, Dict.Time.DATE.toString(), mTemporalView);
        mTemporalPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        mTemporalPopOver.setAutoHide(false);
        mTemporalPopOver.setCloseButtonEnabled(true);
        mTemporalPopOver.setDetachable(true);

        Platform.runLater(() -> {
            mAttributionPopOver = new PopOver();
            mAttributionView = new AttributionView(mAttributionPopOver);
            initPopOver(mAttributionPopOver, Dict.COPYRIGHT.toString(), mAttributionView);
            mAttributionPopOver.setArrowLocation(ArrowLocation.TOP_RIGHT);
        });
    }

    private void setTextFromActions() {
        for (Map.Entry<Action, Double> entry : mButtonWidths.entrySet()) {
            ButtonBase b = getButtonForAction(entry.getKey());
            b.setPrefWidth(entry.getValue());
            b.textProperty().bind(entry.getKey().textProperty());
        }
    }

    private boolean shouldOpen(PopOver popOver) {
        return System.currentTimeMillis() - mPopoverClosingTimes.getOrDefault(popOver, 0L) > 200;
    }

    private void storeButtonWidths(Action... actions) {
        for (Action action : actions) {
            mButtonWidths.put(action, getButtonForAction(mStyleAction).prefWidthProperty().getValue());
        }
    }

    private void tooglePopOver(PopOver popOver, Action action) {
        Platform.runLater(() -> {
            if (popOver.isAutoHide()) {
                if (popOver.isShowing()) {
                    popOver.hide();
                } else {
                    mPopOvers.forEach((item) -> {
                        item.hide();
                    });

                    getItems().stream()
                            .filter((item) -> (item instanceof ButtonBase))
                            .map((item) -> (ButtonBase) item)
                            .filter((buttonBase) -> (buttonBase.getOnAction() == action))
                            .forEachOrdered((buttonBase) -> {
                                buttonBase.fire();
                            });
                }
            } else {
                if (popOver.isShowing()) {
                    popOver.hide();
                } else {
                    popOver.show(getButtonForAction(action));
                }
            }
        });
    }

    private void updateDocumentInfo(GlobalStateChangeEvent evt) {
        MDocumentInfo documentInfo = evt.getValue();
        mAttributionAction.setDisabled(false);
        mStyleAction.setText(documentInfo.getName());
    }

    private boolean usePopOver() {
        return MOptions2.getInstance().general().isPreferPopover() || mOptions.isMapOnly();
    }
}
