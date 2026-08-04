/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.util.List;
import java.util.function.Supplier;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import org.mapton.api.ui.MToolBarPopOver;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.core.api.ui.ExportAction;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionComboBox;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BContentView {

    protected final BContentOptions mContentOptions;
    protected ButterflyFormFilter mFilter;
    protected BaseTabbedFilterPopOver mFilterPopOver;
    protected BLayerOptions mLayerOptions;
    protected SingleListForm<? extends BaseManager, ? extends BXyzPoint> mListForm;
    protected final ComboBox<Integer> mListLimitComboBox = new ComboBox<>();
    protected final SessionComboBox<BListSortOrder> mListSortOrderScb = new SessionComboBox<>();
    protected MPresetPopOver mPresetPopOver;
    protected MToolBarPopOver mToolBarPopOver = new MToolBarPopOver();
    private Action mResetStandardAction;

    public BContentView(BContentOptions contentOptions, ButterflyFormFilter filter) {
        mContentOptions = contentOptions;
        mFilter = filter;
        init();
    }

    public Pane getView() {
        return mListForm.getView();
    }

    public void setListCellFactory(Supplier<? extends ListCell<?>> cellSupplier) {
        @SuppressWarnings("unchecked")
        Callback rawCellFactory = lv -> cellSupplier.get();

        mListForm.getListView().setCellFactory(rawCellFactory);
    }

    protected void bindFooterLabel(SingleListForm mListForm) {
        mListForm.bindFooterLabel(mContentOptions.listSortOrderProperty());
    }

    protected List<Action> getDefaultSubToolBarActions(BaseManager manager, SubToolBarConfig config) {
        var exportAction = new ExportAction(config.exportLookupKey);
        exportAction.setDisabled(config.exportLookupKey == null);

        var copyNamesAction = new CopyNamesAction(manager);
        var addPointsAction = new AddToBasePointsAction(manager);

        copyNamesAction.setDisabled(config.disableCopyName);

        return List.of(
                new ExternalSearchAction(manager),
                exportAction,
                copyNamesAction,
                addPointsAction,
                manager.geZoomExtentstAction()
        );
    }

    protected List<Action> getDefaultToolBarActions() {
        if (mResetStandardAction == null) {
            mFilterPopOver.setAutoArrowLocation(false);
            mFilterPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            mPresetPopOver.setAutoArrowLocation(false);
            mPresetPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
            mResetStandardAction = new Action(Dict.DEFAULT.toString(), actionEvent -> {
                mFilterPopOver.reset();
            });
            mResetStandardAction.setGraphic(MaterialIcon._Action.RESTORE_PAGE.getImageView(Mapton.getIconSizeToolBarInt()));
        }

        return List.of(
                mResetStandardAction,
                mPresetPopOver.getAction(),
                mFilterPopOver.getAction(),
                mFilter.getListSortPopOver().getAction(),
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                ActionUtils.ACTION_SEPARATOR,
                mToolBarPopOver.getAction()
        );
    }

    private void init() {
        mListSortOrderScb.getItems().setAll(BListSortOrder.values());
        mListSortOrderScb.valueProperty().bindBidirectional(mContentOptions.listSortOrderProperty());

        mListLimitComboBox.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item == 0) {
                        setText("Obegränsat antal objekt");
                    } else {
                        setText("Max %d objekt".formatted(item));
                    }
                }
            }
        });

        mListLimitComboBox.setButtonCell(mListLimitComboBox.getCellFactory().call(null));
        mListLimitComboBox.getItems().setAll(0, 5, 10, 25, 50, 100, 1000, 10000);
        mListLimitComboBox.setValue(mContentOptions.getListLimitRaw());
        mListLimitComboBox.valueProperty().addListener((p, o, n) -> {
            if (n != null) {
                mContentOptions.listLimitProperty().set(n);
            }
        });

        var sortAndLimitPane = new VBox(8, mListSortOrderScb, mListLimitComboBox);
        mListLimitComboBox.prefWidthProperty().bind(mListSortOrderScb.widthProperty());
        sortAndLimitPane.setPadding(FxHelper.getUIScaledInsets(8, 16, 16, 16));
        mFilter.getListSortPopOver().setNode(sortAndLimitPane);
    }

    public record SubToolBarConfig(Object exportLookupKey, boolean disableCopyName) {

    }

}
