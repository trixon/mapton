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

import com.dlsc.gemsfx.util.SessionManager;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.ActionUtils;
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
    private final VBox mBottomBox = new VBox(FxHelper.getUIScaled(4));
    private SliderPane mDistanceSliderPane;
    @Deprecated(forRemoval = true)
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty();
    @Deprecated(forRemoval = true)
    private final SimpleObjectProperty<LabelBy.Operations> mLabelByProperty = new SimpleObjectProperty<>();
    private final SessionCheckBox mPlotDebtScbx = new SessionCheckBox("Plotta skuld");
    private final SessionCheckBox mPlotSelectedScbx = new SessionCheckBox("Plotta bara valt objekt");
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

    public SessionCheckBox getPlotDebtScbx() {
        return mPlotDebtScbx;
    }

    public SessionCheckBox getPlotSelectedScbx() {
        return mPlotSelectedScbx;
    }

    public void initListenersSuper() {
        mLabelByProperty.addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });

    }

    public boolean isPlotDebt() {
        return mPlotDebtScbx.isSelected();
    }

    public boolean isPlotSelected() {
        return mPlotSelectedScbx.isSelected();
    }

    public SimpleStringProperty labelByIdProperty() {
        return mLabelByIdProperty;
    }

    @Deprecated(forRemoval = true)
    public SimpleObjectProperty<LabelBy.Operations> labelByProperty() {
        return mLabelByProperty;
    }

    public BooleanProperty plotSelectedDebt() {
        return mPlotDebtScbx.selectedProperty();
    }

    public BooleanProperty plotSelectedProperty() {
        return mPlotSelectedScbx.selectedProperty();
    }

    @Deprecated
    public void registerLayerBundle(LayerBundle layerBundle) {
        mLabelByProperty.addListener((p, o, n) -> {
            layerBundle.repaint();
        });

        mPlotSelectedScbx.selectedProperty().addListener((p, o, n) -> {
            layerBundle.repaint();
        });
        mPlotDebtScbx.selectedProperty().addListener((p, o, n) -> {
            layerBundle.repaint();
        });

        getDistanceSliderPane().selectedProperty().addListener((p, o, n) -> {
            layerBundle.repaint();
        });

        getDistanceSliderPane().valueProperty().addListener((p, o, n) -> {
            layerBundle.repaint();
        });

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

    public void restoreLabelFromId(Class enumClass, String enumName, LabelBy.Operations defaultValue) {
        try {
            Enum a = Enum.valueOf(enumClass, enumName);
            LabelBy.Operations b = (LabelBy.Operations) a;
            mLabelMenuButton.setText(b.getFullName());
        } catch (IllegalArgumentException e) {
            mLabelMenuButton.setText(defaultValue.getFullName());
        }
    }

    public void setDefaultId(LabelBy.Operations labelBy) {
        mLabelByIdProperty.set(labelBy.name());
    }

    protected void initSession(SessionManager sessionManager, boolean enableDebt) {
        initSession(sessionManager);
        mPlotDebtScbx.setDisable(!enableDebt);
    }

    protected void initSession(BOptionsBase options) {
        mBottomBox.setDisable(false);
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

    protected void initSession(SessionManager sessionManager) {
        mBottomBox.setDisable(false);
        sessionManager.register(getKeyOptions("plotDebt"), mPlotDebtScbx.selectedProperty());
        sessionManager.register(getKeyOptions("plotSelected"), mPlotSelectedScbx.selectedProperty());
        mDistanceSliderPane.initSession(getKeyOptions("plotSelectedDistance"), sessionManager);
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

        mBottomBox.setPadding(FxHelper.getUIScaledInsets(8));
        mBottomBox.setDisable(true);

        mDistanceSliderPane = new SliderPane("...plus de inom (m)", 50.0, false);
        mBottomBox.getChildren().addAll(mPlotDebtScbx, mPlotSelectedScbx, mDistanceSliderPane);
        mDistanceSliderPane.disableProperty().bind(mPlotSelectedScbx.selectedProperty().not());

        setLabelPadding(mLabelLabel, mGraphicLabel);

        setBottom(mBottomBox);
    }

}
