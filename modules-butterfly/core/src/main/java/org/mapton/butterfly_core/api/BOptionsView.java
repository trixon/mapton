/*
 * Copyright 2025 Patrik Karlström.
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MDict;
import org.mapton.api.ui.MPresetActions;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionCheckBox;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BOptionsView extends MOptionsView {

    protected final Label mColorLabel = new Label(Dict.COLOR.toString());
    protected final Label mGraphicLabel = new Label(Dict.GRAPHICS.toString());
    protected final Label mLabelLabel = new Label(Dict.LABEL.toString());
    protected final MenuButton mLabelMenuButton = new MenuButton();
    protected final Label mPointLabel = new Label(Dict.Geometry.POINT.toString());
    protected MPresetPopOver mPresetPopOver;
    private GridPane mBottomPane;
    private SliderPane mDistanceSliderPane;
    @Deprecated(forRemoval = true)
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty();
    @Deprecated(forRemoval = true)
    private final SimpleObjectProperty<LabelBy.Operations> mLabelByProperty = new SimpleObjectProperty<>();
    private final SessionCheckBox mPlotAnnotationScbx = new SessionCheckBox(MDict.ANNOTATION.toString());
    private final SessionCheckBox mPlotDebtScbx = new SessionCheckBox("Skuld");
    private final SessionCheckBox mPlotSelectedScbx = new SessionCheckBox("Bara valt");
    private MPresetActions mPresetActions;

    public BOptionsView(LayerBundle layerBundle, String title, MPresetActions presetActions, String key) {
        super(layerBundle, title);
        mPresetPopOver = new MPresetPopOver(presetActions, MPresetPopOver.PARENT_NODE_OPTIONS, key);
        mPresetActions = presetActions;
        createUI();
    }

    public BOptionsView() {
        super();
        createUI();
    }

    public BOptionsView(LayerBundle layerBundle) {
        super(layerBundle);
        createUI();
    }

    public BOptionsView(LayerBundle layerBundle, String title) {
        super(layerBundle, title);
        createUI();
    }

    public void activateAnnotation() {
        mPlotAnnotationScbx.setDisable(false);
    }

    public SliderPane getDistanceSliderPane() {
        return mDistanceSliderPane;
    }

    @Deprecated(forRemoval = true)
    public <T> T getLabelBy() {
        return (T) mLabelByProperty.get();
    }

    public MenuButton getLabelMenuButton() {
        return mLabelMenuButton;
    }

    public SessionCheckBox getPlotAnnotationScbx() {
        return mPlotAnnotationScbx;
    }

    public SessionCheckBox getPlotDebtScbx() {
        return mPlotDebtScbx;
    }

    public SessionCheckBox getPlotSelectedScbx() {
        return mPlotSelectedScbx;
    }

    @Deprecated(forRemoval = true)
    public void initListenersSuper() {
        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });

    }

    @Deprecated(forRemoval = true)
    public boolean isPlotDebt() {
        return mPlotDebtScbx.isSelected();
    }

    @Deprecated(forRemoval = true)
    public boolean isPlotSelected() {
        return mPlotSelectedScbx.isSelected();
    }

    @Deprecated(forRemoval = true)
    public SimpleStringProperty labelByIdProperty() {
        return mLabelByIdProperty;
    }

    @Deprecated(forRemoval = true)
    public SimpleObjectProperty<LabelBy.Operations> labelByProperty() {
        return mLabelByProperty;
    }

    @Deprecated(forRemoval = true)
    public BooleanProperty plotSelectedDebt() {
        return mPlotDebtScbx.selectedProperty();
    }

    @Deprecated(forRemoval = true)
    public BooleanProperty plotSelectedProperty() {
        return mPlotSelectedScbx.selectedProperty();
    }

    @Deprecated(forRemoval = true)
    public void registerLayerBundle(LayerBundle layerBundle) {
    }

    @Deprecated(forRemoval = true)
    public void restoreLabelFromId(Class enumClass, LabelBy.Operations defaultValue) {
        try {
            Enum a = Enum.valueOf(enumClass, mLabelByIdProperty.get());
            LabelBy.Operations b = (LabelBy.Operations) a;
            mLabelByProperty.set(b);
        } catch (IllegalArgumentException e) {
            mLabelByProperty.set(defaultValue);
        }
    }

    @Deprecated(forRemoval = true)
    public void restoreLabelFromId(Class enumClass, String enumName, LabelBy.Operations defaultValue) {
        try {
            Enum a = Enum.valueOf(enumClass, enumName);
            LabelBy.Operations b = (LabelBy.Operations) a;
            mLabelMenuButton.setText(b.getFullName());
        } catch (IllegalArgumentException e) {
            mLabelMenuButton.setText(defaultValue.getFullName());
        }
    }

    @Deprecated(forRemoval = true)
    public void setDefaultId(LabelBy.Operations labelBy) {
        mLabelByIdProperty.set(labelBy.name());
    }

    protected void initSession(BOptionsBase options) {
        mBottomPane.setDisable(false);
        if (options.plotAnnotationProperty() != null) {
            mPlotAnnotationScbx.selectedProperty().bindBidirectional(options.plotAnnotationProperty());
//            mPlotAnnotationScbx.setDisable(false);
        }
        if (options.plotDebtProperty() != null) {
            mPlotDebtScbx.selectedProperty().bindBidirectional(options.plotDebtProperty());
            mPlotDebtScbx.setDisable(false);
        }
        if (options.plotSelectedProperty() != null) {
            mPlotSelectedScbx.selectedProperty().bindBidirectional(options.plotSelectedProperty());
        }
        mDistanceSliderPane.selectedProperty().bindBidirectional(options.plotSelectedPlusProperty());
        mDistanceSliderPane.valueProperty().bindBidirectional(options.plotDistanceProperty());
    }

    private void createUI() {
        if (mPresetPopOver != null) {
            var restoreDefaultsRunnable = (Runnable) () -> {
                if (mPresetPopOver.restoreDefaultIfExists()) {
                    //
                } else {
                    mPresetActions.reset();
                }
            };
            var actions = List.of(
                    getRestoreDefaultsAction(restoreDefaultsRunnable),
                    ActionUtils.ACTION_SPAN,
                    mPresetPopOver.getAction()
            );
            createToolbar(actions);
        }

        mPlotDebtScbx.setDisable(true);
        mPlotAnnotationScbx.setDisable(true);
        mDistanceSliderPane = new SliderPane("...plus de inom (m)", 50.0, false);
        mDistanceSliderPane.disableProperty().bind(mPlotSelectedScbx.selectedProperty().not());

        mBottomPane = createGridPane();
        mBottomPane.setDisable(true);

        int row = 0;
        mBottomPane.addRow(row++, mPlotSelectedScbx, mPlotAnnotationScbx, mPlotDebtScbx);
        mBottomPane.add(mDistanceSliderPane, 0, row++, GridPane.REMAINING, 1);
        FxHelper.autoSizeColumn(mBottomPane, 3);

        setLabelPadding(mLabelLabel, mGraphicLabel);
        setBottom(mBottomPane);
    }

}
