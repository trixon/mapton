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

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.core.api.ui.MPresetPopOver;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BContentView {

    protected final BContentOptions mContentOptions;
    protected ButterflyFormFilter mFilter;
    protected BaseTabbedFilterPopOver mFilterPopOver;
    protected BLayerOptions mLayerOptions;
    protected final ComboBox<Integer> mListLimitComboBox = new ComboBox<>();
    protected final SessionComboBox<BListSortOrder> mListSortOrderScb = new SessionComboBox<>();
    protected MPresetPopOver mPresetPopOver;

    public BContentView(BContentOptions contentOptions, ButterflyFormFilter filter) {
        mContentOptions = contentOptions;
        mFilter = filter;
        init();
    }

    protected void bindFooterLabel(SingleListForm mListForm) {
        mListForm.bindFooterLabel(mContentOptions.listSortOrderProperty());
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

}
