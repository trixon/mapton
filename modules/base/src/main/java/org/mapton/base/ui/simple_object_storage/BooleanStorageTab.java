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
package org.mapton.base.ui.simple_object_storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.ToggleSwitch;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public class BooleanStorageTab<T extends MSimpleObjectStorageBoolean> extends BaseTab {

    private final Class<T> mClass;
    private final HashMap<Class, ToggleSwitch> mClassToToggleSwitch = new HashMap<>();
    private final VBox mItemBox = new VBox(FxHelper.getUIScaled(8));
    private final ScrollPane mScrollPane;

    public BooleanStorageTab(Class<T> c, String title) {
        super(title);
        mClass = c;
        setContent(mScrollPane = new ScrollPane(mItemBox));
        mScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        mScrollPane.setFitToWidth(true);
        mItemBox.setPadding(FxHelper.getUIScaledInsets(8));

        Lookup.getDefault().lookupResult(c).addLookupListener((LookupEvent ev) -> {
            populateItems();
        });

        populateItems();
    }

    @Override
    public void load(Object object) {
        Lookup.getDefault().lookupAll(mClass).forEach(simpleStorage -> {
            mClassToToggleSwitch.get(simpleStorage.getClass()).setSelected(mManager.getBoolean(simpleStorage.getClass(), simpleStorage.getDefaultValue()));
        });
    }

    @Override
    public void save(Object object) {
        Lookup.getDefault().lookupAll(mClass).forEach(simpleStorage -> {
            mManager.putBoolean(simpleStorage.getClass(), mClassToToggleSwitch.get(simpleStorage.getClass()).isSelected());
        });
    }

    private void populateItems() {
        FxHelper.runLater(() -> {
            ArrayList<T> simpleStorages = new ArrayList<>(Lookup.getDefault().lookupAll(mClass));
            Comparator<T> c1 = (T o1, T o2) -> StringUtils.defaultString(o1.getGroup()).compareToIgnoreCase(StringUtils.defaultString(o2.getGroup()));
            Comparator<T> c2 = (T o1, T o2) -> StringUtils.defaultString(o1.getName()).compareToIgnoreCase(StringUtils.defaultString(o2.getName()));

            simpleStorages.sort(c1.thenComparing(c2));
            HashSet<String> groups = new HashSet<>();
            mClassToToggleSwitch.clear();

            for (T simpleStorage : simpleStorages) {
                VBox box;
                ToggleSwitch toggleSwitch = new ToggleSwitch(simpleStorage.getName());
                toggleSwitch.prefWidthProperty().bind(mItemBox.widthProperty());
                box = new VBox(toggleSwitch);
                if (StringUtils.isNotBlank(simpleStorage.getTooltipText())) {
                    toggleSwitch.setTooltip(new Tooltip(simpleStorage.getTooltipText()));
                }
                mClassToToggleSwitch.put(simpleStorage.getClass(), toggleSwitch);

                String group = simpleStorage.getGroup();
                if (!groups.contains(group)) {
                    groups.add(group);
                    box.getChildren().add(0, getGroupLabel(group));
                }

                mItemBox.getChildren().add(box);
            }
        });
    }
}
