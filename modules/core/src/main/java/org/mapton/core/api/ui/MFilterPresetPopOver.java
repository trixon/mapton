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
package org.mapton.core.api.ui;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MDict;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MFilterPopOver;
import org.mapton.api.ui.MPopOver;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.DefaultEditableListItem;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MFilterPresetPopOver extends MPopOver {

    protected EditableList<DefaultEditableListItem> mEditableList;
    private final MFilterPopOver mFilterPopOver;
    private final ObjectProperty<ObservableList<DefaultEditableListItem>> mItemsProperty = new SimpleObjectProperty<>();
    private final Preferences mPreferences;

    public MFilterPresetPopOver(MFilterPopOver filterPopOver, String path) {
        mPreferences = NbPreferences.forModule(filterPopOver.getClass()).node("filterPresets").node(path);
        mFilterPopOver = filterPopOver;
        mItemsProperty.set(FXCollections.observableArrayList());
        createUI();
        try {
            var presets = Arrays.stream(mPreferences.childrenNames())
                    .sorted((o1, o2) -> StringUtils.compareIgnoreCase(o1, o2))
                    .map(s -> {
                        return new DefaultEditableListItem(s);
                    })
                    .toList();
            getItems().setAll(presets);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        initListeners();
    }

    private void createUI() {
        var title = MDict.QUICK_FILTERS.toString();
        setTitle(title);
        getAction().setText(title);
        getAction().setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBarInt()));

        mEditableList = new NbEditableList.Builder<DefaultEditableListItem>()
                .setIconSize(Mapton.getIconSizeToolBarInt())
                .setItemSingular(MDict.QUICK_FILTER.toString())
                .setItemPlural(MDict.QUICK_FILTERS.toString())
                .setItemsProperty(itemsProperty())
                .setOnAdd((String t, DefaultEditableListItem item) -> {
                    save(item);
                })
                .setOnRemoveAll(() -> {
                    getItems().clear();
                })
                .setOnRemove(t -> {
                    getItems().remove(t);
                })
                .build();

        mEditableList.setPrefSize(FxHelper.getUIScaled(250), FxHelper.getUIScaled(500));
        setContentNode(mEditableList);
//        setArrowLocation(ArrowLocation.TOP_RIGHT);
        setAutoHide(true);
        setCloseButtonEnabled(false);
        setDetachable(true);
        this.setOnHidden(windowEvent -> {
            mEditableList.getListView().getSelectionModel().select(null);
        });
    }

    public ObservableList<DefaultEditableListItem> getItems() {
        return mItemsProperty.get();
    }

    public ObjectProperty<ObservableList<DefaultEditableListItem>> itemsProperty() {
        return mItemsProperty;
    }

    private void initListeners() {
        mItemsProperty.get().addListener((ListChangeListener.Change<? extends DefaultEditableListItem> c) -> {
            while (c.next()) {
                c.getRemoved().forEach(item -> {
                    try {
                        mPreferences.node(item.getName()).removeNode();
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });

        mEditableList.getListView().getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            if (n != null) {
                mFilterPopOver.filterPresetRestore(mPreferences.node(n.getName()));
            }
        });
    }

    private DefaultEditableListItem save(DefaultEditableListItem item) {
        var panel = new MFilterPresetSavePanel();
        panel.load(mPreferences);
        var d = new DialogDescriptor(panel, Dict.SAVE_AS.toString());
        if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
            item = new DefaultEditableListItem();
            item.setName(panel.getPresetName());
            if (!getItems().contains(item)) {
                getItems().add(item);
                getItems().sort((o1, o2) -> StringUtils.compareIgnoreCase(o1.getName(), o2.getName()));
            }
            try {
                mPreferences.node(item.getName()).removeNode();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            mFilterPopOver.filterPresetStore(mPreferences.node(item.getName()));
        }

        return item;
    }
}
