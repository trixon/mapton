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

import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.SessionManager;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MPopOver;
import org.mapton.api.ui.MPresetActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.DefaultEditableListItem;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MPresetPopOver extends MPopOver {

    public static final String PARENT_NODE_FILTER = "filterPresets";
    public static final String PARENT_NODE_OPTIONS = "optionPresets";

    protected EditableList<DefaultEditableListItem> mEditableList;
    private final ObjectProperty<ObservableList<DefaultEditableListItem>> mItemsFilteredProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<DefaultEditableListItem>> mItemsRawProperty = new SimpleObjectProperty<>();
    private final Preferences mPreferences;
    private final MPresetActions mPresetActions;

    @Deprecated(forRemoval = true)
    public MPresetPopOver(MPresetActions filterPopOver, String path) {
        this(filterPopOver, PARENT_NODE_FILTER, path);
    }

    public MPresetPopOver(MPresetActions presetActions, String parent, String path) {
        mPreferences = NbPreferences.forModule(presetActions.getClass()).node(parent).node(path);
        mPresetActions = presetActions;
        mItemsFilteredProperty.set(FXCollections.observableArrayList());
        mItemsRawProperty.set(FXCollections.observableArrayList());
        createUI();
        try {
            var presets = Arrays.stream(mPreferences.childrenNames())
                    .sorted((o1, o2) -> Strings.CI.compare(o1, o2))
                    .map(s -> {
                        return new DefaultEditableListItem(s);
                    })
                    .toList();
            getItemsRaw().setAll(presets);
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        initListeners();

        var sessionManager = new SessionManager(mPreferences.parent());
        sessionManager.register("storedFilter_%s".formatted(path), mEditableList.filterTextProperty());
    }

    public boolean restoreDefaultIfExists() {
        try {
            var node = "(STANDARD)";
            if (mPreferences.nodeExists(node)) {
                mPresetActions.presetRestore(mPreferences.node(node));
                return true;
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    private void createUI() {
        var title = Dict.PRESETS.toString();
        setTitle(title);
        setHideOnEscape(true);
        getAction().setText(title);
        getAction().setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBarInt()));

        mEditableList = new NbEditableList.Builder<DefaultEditableListItem>()
                .setIconSize(Mapton.getIconSizeToolBarInt())
                .setItemSingular(Dict.PRESET.toString())
                .setItemPlural(Dict.PRESETS.toString())
                .setItemsProperty(itemsFilteredProperty())
                .setOnAdd((String t, DefaultEditableListItem item) -> {
                    save(item);
                })
                .setOnRemoveAll(() -> {
                    getItemsRaw().clear();
                })
                .setOnRemove(t -> {
                    getItemsRaw().remove(t);
                })
                .setOnFilter(s -> {
                    refreshFilteredItems();
                })
                .setWithFooter(true, getItemsRaw())
                .build();

        mEditableList.setPrefSize(FxHelper.getUIScaled(250), FxHelper.getUIScaled(500));
        setContentNode(mEditableList);
        setAutoHide(true);
        setCloseButtonEnabled(false);
        setDetachable(true);
        this.setOnHidden(windowEvent -> {
            mEditableList.getListView().getSelectionModel().select(null);
        });

        var desktopOpenAction = new Action(Dict.OPEN_DIRECTORY.toString(), actionEvent -> {
            var file = new File(Places.getUserDirectory(), "config/Preferences" + mPreferences.absolutePath());
            try {
                SystemHelper.desktopOpen(file);
            } catch (Exception e) {
                System.out.println(e);
            }
        });
        desktopOpenAction.setGraphic(MaterialIcon._Action.OPEN_WITH.getImageView(getIconSizeToolBarInt()));
        var toolBarItems = mEditableList.getToolBar().getItems();
        var doa = ActionUtils.createButton(desktopOpenAction, ActionTextBehavior.HIDE);
        toolBarItems.add(new Spacer());
        toolBarItems.add(doa);
        FxHelper.undecorateButtons(toolBarItems.stream());
        FxHelper.slimToolBar(mEditableList.getToolBar());
    }

    private ObservableList<DefaultEditableListItem> getItemsFiltered() {
        return itemsFilteredProperty().get();
    }

    private ObservableList<DefaultEditableListItem> getItemsRaw() {
        return itemsRawProperty().get();
    }

    private void initListeners() {
        mItemsRawProperty.get().addListener((ListChangeListener.Change<? extends DefaultEditableListItem> c) -> {
            while (c.next()) {
                c.getRemoved().forEach(item -> {
                    try {
                        mPreferences.node(item.getName()).removeNode();
                    } catch (BackingStoreException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
            refreshFilteredItems();
        });

        mEditableList.getListView().getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            if (n != null) {
                mPresetActions.presetRestore(mPreferences.node(n.getName()));
            }
        });

        mEditableList.getListView().setOnKeyPressed(keyEvent -> {
            if (Set.of(KeyCode.ENTER, KeyCode.SPACE).contains(keyEvent.getCode())) {
                hide();
            }
        });

        mEditableList.getListView().setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                hide();
            }
        });

        mEditableList.filterTextProperty().addListener((p, o, n) -> {
            refreshFilteredItems();
        });
    }

    private ObjectProperty<ObservableList<DefaultEditableListItem>> itemsFilteredProperty() {
        return mItemsFilteredProperty;
    }

    private ObjectProperty<ObservableList<DefaultEditableListItem>> itemsRawProperty() {
        return mItemsRawProperty;
    }

    private void refreshFilteredItems() {
        var filteredItems = getItemsRaw().stream()
                .filter(item -> StringHelper.matchesSimpleGlob(item.getName(), mEditableList.filterTextProperty().get(), true, true))
                .toList();

        getItemsFiltered().setAll(filteredItems);
    }

    private DefaultEditableListItem save(DefaultEditableListItem item) {
        var panel = new MPresetSavePanel();
        panel.load(mPreferences);
        var d = new DialogDescriptor(panel, Dict.SAVE_AS.toString());
        if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
            item = new DefaultEditableListItem();
            item.setName(panel.getPresetName());
            if (!getItemsRaw().contains(item)) {
                getItemsRaw().add(item);
                getItemsRaw().sort((o1, o2) -> Strings.CI.compare(o1.getName(), o2.getName()));
            }
            try {
                mPreferences.node(item.getName()).removeNode();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            mPresetActions.presetStore(mPreferences.node(item.getName()));
        }

        return item;
    }
}
