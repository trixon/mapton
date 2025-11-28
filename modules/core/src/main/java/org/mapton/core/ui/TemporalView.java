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

import java.time.LocalDate;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MTemporalManager;
import org.mapton.api.ui.forms.DateRangePane;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TemporalView extends BorderPane {

    private final MTemporalManager mManager = MTemporalManager.getInstance();
    private final StringProperty mTitleProperty = new SimpleStringProperty();
    private final DateRangePane mDateRangePane = new DateRangePane();

    public TemporalView() {
        createUI();
        initListeners();

        setDisable(true);
        mDateRangePane.setMinMaxDate(mManager.getMinDate(), mManager.getMaxDate());

        mManager.refresh();
    }

    public StringProperty titleProperty() {
        return mTitleProperty;
    }

    private void createUI() {
        setPrefWidth(FxHelper.getUIScaled(300));
        setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(mDateRangePane.getRoot());
    }

    private void initListeners() {
        ChangeListener<LocalDate> minMaxChangeListener = (p, o, n) -> {
            mDateRangePane.setMinMaxDate(mManager.getMinDate(), mManager.getMaxDate());
            try {
                setDisable(mManager.getMinDate().equals(LocalDate.of(1900, 1, 1)) && mManager.getMaxDate().equals(LocalDate.of(2099, 12, 31)));
            } catch (Exception e) {
                setDisable(true);
            }
            refreshTitle();
        };

        mManager.minDateProperty().addListener(minMaxChangeListener);
        mManager.maxDateProperty().addListener(minMaxChangeListener);

        ChangeListener<LocalDate> rangeChangeListener = (ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) -> {
            refreshTitle();
        };

        mManager.lowDateProperty().addListener(rangeChangeListener);
        mManager.highDateProperty().addListener(rangeChangeListener);

        mManager.lowDateProperty().bindBidirectional(mDateRangePane.lowDateProperty());
        mManager.highDateProperty().bindBidirectional(mDateRangePane.highDateProperty());
    }

    private void refreshTitle() {
        FxHelper.runLater(() -> {
            var text = "%s %s %s".formatted(
                    mManager.getLowDate(),
                    Dict.TO.toString().toLowerCase(Locale.getDefault()),
                    mManager.getHighDate()
            );

            mTitleProperty.set(isDisabled() ? Dict.DATE.toString() : text);
        });
    }

}
