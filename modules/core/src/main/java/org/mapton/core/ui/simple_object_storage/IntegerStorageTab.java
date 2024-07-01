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
package org.mapton.core.ui.simple_object_storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MSimpleObjectStorageInteger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public class IntegerStorageTab<T extends MSimpleObjectStorageInteger> extends BaseTab {

    private final Class<T> mClass;
    private final HashMap<Class, Spinner<Integer>> mClassToSpinner = new HashMap<>();
    private final VBox mItemBox = new VBox(FxHelper.getUIScaled(8));
    private final ScrollPane mScrollPane;

    public IntegerStorageTab(Class<T> c, String title) {
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
            var spinner = mClassToSpinner.get(simpleStorage.getClass());
            var value = mManager.getInteger(simpleStorage.getClass(), simpleStorage.getDefaultValue());
            spinner.getValueFactory().setValue(value);
        });
    }

    @Override
    public void save(Object object) {
        Lookup.getDefault().lookupAll(mClass).forEach(simpleStorage -> {
            mManager.putInteger(simpleStorage.getClass(), mClassToSpinner.get(simpleStorage.getClass()).getValue());
        });
    }

    private void populateItems() {
        FxHelper.runLater(() -> {
            ArrayList<T> simpleStorages = new ArrayList<>(Lookup.getDefault().lookupAll(mClass));
            Comparator<T> c1 = (T o1, T o2) -> StringUtils.defaultString(o1.getGroup()).compareToIgnoreCase(StringUtils.defaultString(o2.getGroup()));
            Comparator<T> c2 = (T o1, T o2) -> StringUtils.defaultString(o1.getName()).compareToIgnoreCase(StringUtils.defaultString(o2.getName()));

            simpleStorages.sort(c1.thenComparing(c2));
            var groups = new HashSet<String>();
            mClassToSpinner.clear();

            for (T simpleStorage : simpleStorages) {
                var label = new Label(simpleStorage.getName());
                var spinner = new Spinner<Integer>(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
                spinner.setPrefWidth(FxHelper.getUIScaled(150.0));
                spinner.setEditable(true);
                FxHelper.autoCommitSpinner(spinner);
                var gp = new GridPane();
                gp.addRow(1, label, spinner);
                GridPane.setHgrow(label, Priority.ALWAYS);
                if (StringUtils.isNotBlank(simpleStorage.getTooltipText())) {
                    spinner.setTooltip(new Tooltip(simpleStorage.getTooltipText()));
                }
                mClassToSpinner.put(simpleStorage.getClass(), spinner);

                var group = simpleStorage.getGroup();
                if (!groups.contains(group)) {
                    groups.add(group);
                    gp.add(getGroupLabel(group), 0, 0, GridPane.REMAINING, 1);
                }

                mItemBox.getChildren().add(gp);
            }
        });
    }
}
