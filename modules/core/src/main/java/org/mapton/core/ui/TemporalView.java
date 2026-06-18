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

import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.controlsfx.control.SegmentedButton;
import org.mapton.api.MTemporalManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.DateRangePane;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SelectionModelSession;
import se.trixon.almond.util.fx.session.SpinnerIntegerSession;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class TemporalView extends BorderPane {

    private final DateRangePane mDateRangePane = new DateRangePane();
    private final MTemporalManager mManager = MTemporalManager.getInstance();
    private final StringProperty mTitleProperty = new SimpleStringProperty();
    private final AnimatorPane mAnimatorPane = new AnimatorPane();

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
        setPrefWidth(FxHelper.getUIScaled(350));
        setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(mDateRangePane.getRoot());
        setBottom(mAnimatorPane.getRoot());

        mAnimatorPane.getRoot().prefWidthProperty().bind(mDateRangePane.getRoot().widthProperty());
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

    class AnimatorPane {

        private LocalDate mCurrentDate;
        private final long mDelay = 1000L;
        private LocalDate mEndDate;
        private final CheckBox mLoopCheckBox = new CheckBox();
        private final ToggleButton mPlayButton = new ToggleButton();
        private final Preferences mPreferences = NbPreferences.forModule(AnimatorPane.class).node("animator");
        private final CheckBox mReversedCheckBox = new CheckBox();
        private final GridPane mRoot = new GridPane(0d, FxHelper.getUIScaled(8d));
        private final SessionManager mSessionManager = new SessionManager(mPreferences);
        private final ComboBox<String> mSpeedComboBox = new ComboBox<>();
        private final Spinner<Integer> mSpeedSpinner = new Spinner<>(1, 999, 10);
        private LocalDate mStartDate;
        private final ToggleButton mStopButton = new ToggleButton();
        private Timeline mTimeline;

        public AnimatorPane() {
            createUI();
            initListeners();
        }

        private void createUI() {
            mTimeline = new Timeline(new KeyFrame(Duration.millis(mDelay), event -> {
                long speed = getSpeed();
                if (mReversedCheckBox.isSelected()) {
                    if (mCurrentDate.isAfter(mStartDate)) {
                        mCurrentDate = DateHelper.getMax(mCurrentDate.minusDays(speed), mStartDate);
                        mManager.setLowDate(mCurrentDate);
                        validateLoop(mStartDate);
                    }
                } else {
                    if (mCurrentDate.isBefore(mEndDate)) {
                        mCurrentDate = DateHelper.getMin(mCurrentDate.plusDays(speed), mEndDate);
                        mManager.setHighDate(mCurrentDate);
                        validateLoop(mEndDate);
                    }
                }
            }));
            mTimeline.setCycleCount(Animation.INDEFINITE);

            mPlayButton.setGraphic(MaterialIcon._Av.PLAY_ARROW.getImageView(getIconSizeToolBarInt()));
            mStopButton.setGraphic(MaterialIcon._Av.STOP.getImageView(getIconSizeToolBarInt()));
            var buttonInsets = FxHelper.getUIScaledInsets(2);
            for (var region : List.of(mPlayButton, mStopButton, mLoopCheckBox, mReversedCheckBox)) {
                region.setPadding(buttonInsets);
            }
            mLoopCheckBox.setGraphic(MaterialIcon._Av.LOOP.getImageView(getIconSizeToolBarInt()));
            mReversedCheckBox.setGraphic(MaterialIcon._Navigation.CHEVRON_LEFT.getImageView(getIconSizeToolBarInt()));
            mSpeedComboBox.getItems().setAll("d/s", "s");
            mSpeedComboBox.getSelectionModel().selectFirst();
            mSpeedSpinner.setPrefWidth(FxHelper.getUIScaled(80));
            var speedBox = new HBox(FxHelper.getUIScaled(4d), mSpeedSpinner, mSpeedComboBox);
            FxHelper.setEditable(true, mSpeedSpinner);
//            FxHelper.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 0), mLoopCheckBox, mReversedCheckBox);
            FxHelper.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 16), speedBox);
            var hbox = new HBox(FxHelper.getUIScaled(0d),
                    new SegmentedButton(mPlayButton, mStopButton),
                    speedBox,
                    new Spacer(),
                    mLoopCheckBox,
                    mReversedCheckBox
            );
            hbox.setAlignment(Pos.CENTER);
            var row = 0;
            mRoot.addRow(row++, new Separator());
            mRoot.addRow(row++, hbox);
//            mRoot.addRow(row++, mSlider);
            mRoot.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0));
            mRoot.widthProperty();
            FxHelper.autoSizeColumn(mRoot, 1);
        }

        private Region getRoot() {
            return mRoot;
        }

        private long getSpeed() {
            var value = mSpeedSpinner.getValue();
            if (mSpeedComboBox.getSelectionModel().isSelected(0)) {
                return value * 1000L / mDelay;
            } else {
                var days = ChronoUnit.DAYS.between(mStartDate, mEndDate);
                return (days / value) * 1000L / mDelay;
            }
        }

        private void initListeners() {
            mPlayButton.setOnAction(ae -> {
                mPlayButton.setSelected(false);
                initStart();
                mTimeline.play();
            });

            mStopButton.setOnAction(ae -> {
                mStopButton.setSelected(false);
                mTimeline.stop();
                mManager.setLowDate(mStartDate);
                mManager.setHighDate(mEndDate);
            });

            var speedComboSession = new SelectionModelSession(mSpeedComboBox.getSelectionModel());
            mSessionManager.register("speedMode", speedComboSession.selectedIndexProperty());

            var speedSpinnerSession = new SpinnerIntegerSession(mSpeedSpinner);
            mSessionManager.register("speedValue", speedSpinnerSession.valueProperty());

            mSessionManager.register("loop", mLoopCheckBox.selectedProperty());
            mSessionManager.register("reversed", mReversedCheckBox.selectedProperty());
        }

        private void initStart() {
            mStartDate = mManager.getLowDate();
            mEndDate = mManager.getHighDate();
            mCurrentDate = mReversedCheckBox.isSelected() ? mEndDate : mStartDate;
        }

        private void validateLoop(LocalDate targetDate) {
            if (mCurrentDate.isEqual(targetDate)) {
                if (mLoopCheckBox.isSelected()) {
                    initStart();
                } else {
                    mTimeline.stop();
                }
            }
        }
    }
}
