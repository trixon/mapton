/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.base.ui.string_storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MStringStorage;
import org.mapton.api.MStringStorageManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public class StringStorageTab<T extends MStringStorage> extends Tab {

    private final Class<T> mClass;
    private final HashMap<Class, TextField> mClassToTextField = new HashMap<>();
    private final VBox mItemBox = new VBox(FxHelper.getUIScaled(8));
    private final MStringStorageManager mManager = MStringStorageManager.getInstance();
    private ScrollPane mScrollPane;

    public StringStorageTab(Class<T> c, String title) {
        mClass = c;
        setText(title);
        setClosable(false);
        setContent(mScrollPane = new ScrollPane(mItemBox));
        mScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        mScrollPane.setFitToWidth(true);
        mItemBox.setPadding(FxHelper.getUIScaledInsets(8));

        Lookup.getDefault().lookupResult(c).addLookupListener((LookupEvent ev) -> {
            populateItems();
        });

        populateItems();
    }

    void load() {
        Lookup.getDefault().lookupAll(mClass).forEach(stringStorage -> {
            mClassToTextField.get(stringStorage.getClass()).setText(mManager.getValue(stringStorage.getClass(), stringStorage.getDefaultValue()));
        });
    }

    void store() {
        Lookup.getDefault().lookupAll(mClass).forEach(stringStorage -> {
            mManager.putValue(stringStorage.getClass(), mClassToTextField.get(stringStorage.getClass()).getText());
        });
    }

    private void populateItems() {
        FxHelper.runLater(() -> {
            ArrayList<T> stringStorages = new ArrayList<>(Lookup.getDefault().lookupAll(mClass));
            Comparator<T> c1 = (T o1, T o2) -> StringUtils.defaultString(o1.getGroup()).compareToIgnoreCase(StringUtils.defaultString(o2.getGroup()));
            Comparator<T> c2 = (T o1, T o2) -> StringUtils.defaultString(o1.getName()).compareToIgnoreCase(StringUtils.defaultString(o2.getName()));

            stringStorages.sort(c1.thenComparing(c2));
            HashSet<String> groups = new HashSet<>();
            mClassToTextField.clear();

            for (T stringStorage : stringStorages) {
                VBox box;
                TextField textField;
                if (stringStorage instanceof MStringStorage.Path) {
                    MStringStorage.Path storagePath = (MStringStorage.Path) stringStorage;
                    FileChooserPane fileChooserPane = new FileChooserPane("title", storagePath.getName(), storagePath.getObjectMode(), SelectionMode.SINGLE);
                    textField = fileChooserPane.getTextField();
                    box = new VBox(fileChooserPane);
                } else {
                    Label nameLabel = new Label(stringStorage.getName());
                    textField = new TextField();
                    textField.prefWidthProperty().bind(mItemBox.widthProperty());
                    box = new VBox(nameLabel, textField);
                }
                textField.setPromptText(stringStorage.getPromptText());
                textField.setTooltip(new Tooltip(stringStorage.getTooltipText()));
                mClassToTextField.put(stringStorage.getClass(), textField);

                String group = stringStorage.getGroup();
                if (!groups.contains(group)) {
                    groups.add(group);
                    Label groupLabel = new Label(group);
                    box.getChildren().add(0, groupLabel);
                    groupLabel.setFont(Font.font(Font.getDefault().getSize() * 1.2));
                }

                mItemBox.getChildren().add(box);
            }
        });
    }

}
