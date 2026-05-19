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
package org.mapton.core.ui;

import java.util.Comparator;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.ToggleSwitch;
import org.mapton.api.MChartSOSB;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.Mapton;
import org.mapton.core.api.ChartOptionsManager;
import org.mapton.core.api.ChartStartPoint;
import org.mapton.core.ui.simple_object_storage.BaseTab;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.Spacer;
import se.trixon.almond.util.fx.session.SessionCheckBox;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
class ChartPropertiesView extends BorderPane {

    private final ChartOptionsManager mChartOptionsManager = ChartOptionsManager.getInstance();
    private final Class<MSimpleObjectStorageBoolean.Misc> mClass = MSimpleObjectStorageBoolean.Misc.class;
    private final MSimpleObjectStorageManager mManager = MSimpleObjectStorageManager.getInstance();
    private final VBox mOverlayItemBox = new VBox(FxHelper.getUIScaled(8));
    private final Preferences mPreferences = NbPreferences.forModule(ChartPropertiesView.class).node("chartProperties");

    public ChartPropertiesView() {
        createUI();
        initListeners();
        populateOverlayItems();
    }

    private void createUI() {
        var overlayScrollPane = new ScrollPane(mOverlayItemBox);
        overlayScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        overlayScrollPane.setFitToWidth(true);
        overlayScrollPane.setBackground(FxHelper.createBackground(Color.TRANSPARENT));
        mOverlayItemBox.setPadding(FxHelper.getUIScaledInsets(8));

        var overlayTab = new Tab(MDict.OVERLAYS.toString(), overlayScrollPane);
        var dateView = new DateView();
        var dateTab = new Tab(Dict.DATE.toString(), dateView);
        var rootTabPane = new TabPane(overlayTab, dateTab);
        rootTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        rootTabPane.setSide(Side.LEFT);
        setCenter(rootTabPane);
        //            setTop(new Label("TODO? Toolbar with presets"));

        int activeTab = mPreferences.getInt("activeTab", 0);
        rootTabPane.getSelectionModel().select(activeTab);
        rootTabPane.getSelectionModel().selectedIndexProperty().addListener((p, o, n) -> {
            mPreferences.putInt("activeTab", n.intValue());
        });
    }

    private void initListeners() {
        Lookup.getDefault().lookupResult(mClass).addLookupListener(lookupEvent -> {
            populateOverlayItems();
        });
    }

    private void populateOverlayItems() {
        FxHelper.runLater(() -> {
            mOverlayItemBox.getChildren().clear();
            HashSet<String> groups = new HashSet<>();
            Comparator<MSimpleObjectStorageBoolean.Misc> c1 = (o1, o2) -> StringUtils.defaultString(o1.getGroup()).compareToIgnoreCase(StringUtils.defaultString(o2.getGroup()));
            Comparator<MSimpleObjectStorageBoolean.Misc> c2 = (o1, o2) -> StringUtils.defaultString(o1.getName()).compareToIgnoreCase(StringUtils.defaultString(o2.getName()));
            Lookup.getDefault().lookupAll(mClass).stream()
                    .filter(p -> Strings.CI.equals(p.getCategory(), "chart"))
                    .sorted(c1.thenComparing(c2))
                    .map(simpleStorage -> (MChartSOSB) simpleStorage)
                    .forEachOrdered(simpleStorage -> {
                        VBox box;
                        var toggleSwitch = new ToggleSwitch(simpleStorage.getName());
                        //                            toggleSwitch.setPadding(FxHelper.getUIScaledInsets(10));
                        toggleSwitch.prefWidthProperty().bind(mOverlayItemBox.widthProperty());
                        var coloredBorderPane = new BorderPane(toggleSwitch);
                        var colorPanel = new Pane();
                        //                            colorPanel.setPadding(FxHelper.getUIScaledInsets(10));
                        colorPanel.setPrefWidth(FxHelper.getUIScaled(8.0));
                        var spacer = new Spacer(Orientation.HORIZONTAL, FxHelper.getUIScaled(8.0));
                        coloredBorderPane.setLeft(new HBox(colorPanel, spacer));
                        colorPanel.setBackground(FxHelper.createBackground(simpleStorage.getColor()));
                        box = new VBox(coloredBorderPane);
                        if (StringUtils.isNotBlank(simpleStorage.getTooltipText())) {
                            toggleSwitch.setTooltip(new Tooltip(simpleStorage.getTooltipText()));
                        }
                        toggleSwitch.setSelected(mManager.getBoolean(simpleStorage.getClass(), simpleStorage.getDefaultValue()));
                        toggleSwitch.selectedProperty().addListener((p, o, n) -> {
                            SystemHelper.runLaterDelayed(250, () -> {
                                mManager.putBoolean(simpleStorage.getClass(), toggleSwitch.isSelected());
                                Mapton.getGlobalState().put(MKey.OBJECT_RESELECT, System.currentTimeMillis());
                            });
                        });
                        var group = simpleStorage.getGroup();
                        if (!groups.contains(group)) {
                            groups.add(group);
                            box.getChildren().add(0, BaseTab.createGroupLabel(group, 0.8));
                        }
                        mOverlayItemBox.getChildren().add(box);
                    });
        });
    }

    private class DateView extends VBox {

        private final SessionComboBox<ChartStartPoint> mPeriodComboBox = new SessionComboBox<>();
        private final SessionCheckBox mResetOnFirstScb = new SessionCheckBox("💀...och nollställ på första synliga");

        public DateView() {
            super(FxHelper.getUIScaled(8));
            createUI();
        }

        private void createUI() {
            setPadding(FxHelper.getUIScaledInsets(8));
            var headerLabel = new Label("Period");
            var chartEndTodayScb = new SessionCheckBox("Slutdatum idag");
            chartEndTodayScb.setTooltip(new Tooltip("... och inte senaste"));
            mPeriodComboBox.getItems().setAll(ChartStartPoint.values());
            initBindings();

            mPeriodComboBox.valueProperty().bindBidirectional(mChartOptionsManager.datePeriodProperty());
            mResetOnFirstScb.selectedProperty().bindBidirectional(mChartOptionsManager.dateResetOnFirst());
            chartEndTodayScb.selectedProperty().bindBidirectional(mChartOptionsManager.dateEndTodayProperty());

            var vbox = new VBox(
                    headerLabel,
                    mPeriodComboBox);

            getChildren().addAll(vbox,
                    chartEndTodayScb,
                    mResetOnFirstScb
            );

            BindingHelper.bindWidthForChildrens(this, vbox);
        }

        private void initBindings() {
            mResetOnFirstScb.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                        var selectedPeriod = mPeriodComboBox.getValue();

                        return selectedPeriod == null
                                || selectedPeriod == ChartStartPoint.FIRST
                                || selectedPeriod == ChartStartPoint.ZERO;
                    }, mPeriodComboBox.valueProperty())
            );
        }
    }
}
