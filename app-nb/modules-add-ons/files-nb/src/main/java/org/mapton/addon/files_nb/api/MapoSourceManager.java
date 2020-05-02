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
package org.mapton.addon.files_nb.api;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.mapton.addon.files_nb.ui.SourcePanel;
import org.mapton.api.Mapton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSourceManager {

    private File mCacheDir;
    private File mConfigDir;
    private ObjectProperty<ObservableList<MapoSource>> mItems = new SimpleObjectProperty<>();
    private File mSourcesFile;

    public static MapoSourceManager getInstance() {
        return Holder.INSTANCE;
    }

    private MapoSourceManager() {
        mItems.setValue(FXCollections.observableArrayList());
    }

    public void edit(final MapoSource aSource) {
        SwingUtilities.invokeLater(() -> {
            MapoSource newSource = aSource;
            boolean add = aSource == null;
            if (add) {
                newSource = new MapoSource();
                newSource.setId(System.currentTimeMillis());
            }

            final MapoSource source = newSource;
            SourcePanel localGridPanel = new SourcePanel();
            DialogDescriptor d = new DialogDescriptor(localGridPanel, Dict.SOURCE.toString());
            localGridPanel.setDialogDescriptor(d);
            localGridPanel.initFx(() -> {
                localGridPanel.load(source);
            });

            localGridPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 400));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(source);
                    if (add) {
                        mItems.get().add(source);
                    }

                    FXCollections.sort(mItems.get(), (MapoSource o1, MapoSource o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public File getCacheDir() {
        if (mCacheDir == null) {
            mCacheDir = new File(Mapton.getCacheDir(), "photos");
        }

        return mCacheDir;
    }

    public File getConfigDir() {
        if (mConfigDir == null) {
            mConfigDir = new File(Mapton.getConfigDir(), "photos");
        }

        return mConfigDir;
    }

    public ObservableList<MapoSource> getItems() {
        return mItems.get();
    }

    public LocalDate getMaxDate() {
        LocalDate localDate = LocalDate.MIN;

        for (MapoSource source : mItems.get()) {
            if (source.isVisible()) {
                try {
                    LocalDate collectionDate = source.getCollection().getDateMax().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (collectionDate.isAfter(localDate)) {
                        localDate = collectionDate;
                    }
                } catch (NullPointerException e) {
                }
            }
        }

        if (localDate.isEqual(LocalDate.MIN)) {
            localDate = LocalDate.of(2099, 12, 31);
        }

        return localDate;
    }

    public LocalDate getMinDate() {
        LocalDate localDate = LocalDate.MAX;

        for (MapoSource source : mItems.get()) {
            if (source.isVisible()) {
                try {
                    LocalDate collectionDate = source.getCollection().getDateMin().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (collectionDate.isBefore(localDate)) {
                        localDate = collectionDate;
                    }
                } catch (NullPointerException e) {
                }
            }
        }

        if (localDate.isEqual(LocalDate.MAX)) {
            localDate = LocalDate.of(1900, 1, 1);
        }

        return localDate;
    }

    public final ObjectProperty<ObservableList<MapoSource>> itemsProperty() {
        if (mItems == null) {
            mItems = new SimpleObjectProperty<>(this, "items");
        }

        return mItems;
    }

    public void load() {
        ArrayList<MapoSource> loadedItems = new ArrayList<>();

        try {
            if (getSourcesFile().isFile()) {
                loadedItems = Mapo.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<MapoSource>>() {
                }.getType());
                for (MapoSource source : loadedItems) {
                    try {
                        source.setCollection(source.loadCollection());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } catch (IOException | JsonSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        final ArrayList<MapoSource> items = loadedItems;
        Platform.runLater(() -> {
            mItems.get().setAll(items);
            Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, this);
        });
    }

    public void removeAll(MapoSource... mapoSources) {
        mItems.get().removeAll(mapoSources);
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSourcesFile(), Mapo.getGson().toJson(mItems.get()), "utf-8");
    }

    public void sourceExport(File file, ArrayList<MapoSource> selectedSources) throws IOException {
        FileUtils.writeStringToFile(file, Mapo.getGson().toJson(selectedSources), "utf-8");
    }

    public void sourceImport(File file) throws IOException {
        String json = FileUtils.readFileToString(file, "utf-8");
        ArrayList<MapoSource> sources = Mapo.getGson().fromJson(json, new TypeToken<ArrayList<MapoSource>>() {
        }.getType());

        Platform.runLater(() -> {
            mItems.get().addAll(sources);
            FXCollections.sort(mItems.get(), (MapoSource o1, MapoSource o2) -> o1.getName().compareTo(o2.getName()));
        });
    }

    private File getSourcesFile() {
        if (mSourcesFile == null) {
            mSourcesFile = new File(getConfigDir(), "sources.json");
        }

        return mSourcesFile;
    }

    private static class Holder {

        private static final MapoSourceManager INSTANCE = new MapoSourceManager();
    }
}
