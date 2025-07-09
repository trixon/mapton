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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.MenuButton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.MOptionsView;

/**
 *
 * @author Patrik Karlström
 */
public class BOptionsView extends MOptionsView {

    protected final MenuButton mLabelMenuButton = new MenuButton();
    private final SimpleStringProperty mLabelByIdProperty = new SimpleStringProperty();
    private final SimpleObjectProperty<LabelBy.Operations> mLabelByProperty = new SimpleObjectProperty<>();

    public BOptionsView() {
        super();
    }

    public BOptionsView(LayerBundle layerBundle) {
        super(layerBundle);
    }

    public BOptionsView(LayerBundle layerBundle, String title) {
        super(layerBundle, title);
    }

    public <T> T getLabelBy() {
        return (T) labelByProperty().get();
    }

    public MenuButton getLabelMenuButton() {
        return mLabelMenuButton;
    }

    public void initListenersSuper() {
        labelByProperty().addListener((p, o, n) -> {
            mLabelMenuButton.setText(n.getFullName());
            mLabelByIdProperty.set(n.name());
        });

    }

    public SimpleStringProperty labelByIdProperty() {
        return mLabelByIdProperty;
    }

    public SimpleObjectProperty<LabelBy.Operations> labelByProperty() {
        return mLabelByProperty;
    }

    public void restoreLabelFromId(Class enumClass, LabelBy.Operations defaultValue) {
        try {
            Enum a = Enum.valueOf(enumClass, labelByIdProperty().get());
            LabelBy.Operations b = (LabelBy.Operations) a;
            labelByProperty().set(b);
        } catch (IllegalArgumentException e) {
            labelByProperty().set(defaultValue);
        }
    }

    public void setDefaultId(LabelBy.Operations labelBy) {
        mLabelByIdProperty.set(labelBy.name());
    }

}
