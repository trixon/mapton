/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.mapollage.api;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.mapton.api.Mapton;
import org.mapton.mapollage.ui.SourcePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSourceManager {

    private File mCacheDir;
    private File mConfigDir;
    private final ObservableList<MapoSource> mItems = FXCollections.observableArrayList();
    private File mSourcesFile;

    public static MapoSourceManager getInstance() {
        return Holder.INSTANCE;
    }

    private MapoSourceManager() {
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

            localGridPanel.setPreferredSize(new Dimension(600, 400));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(source);
                    if (add) {
                        mItems.add(source);
                    }

                    FXCollections.sort(mItems, (MapoSource o1, MapoSource o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public File getCacheDir() {
        if (mCacheDir == null) {
            mCacheDir = new File(Mapton.getCacheDir(), "mapollage");
        }

        return mCacheDir;
    }

    public File getConfigDir() {
        if (mConfigDir == null) {
            mConfigDir = new File(Mapton.getConfigDir(), "mapollage");
        }

        return mConfigDir;
    }

    public ObservableList<MapoSource> getItems() {
        return mItems;
    }

    public void load() {
        mItems.setAll(loadItems());
        try {
            loadCollections();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void loadCollections() throws IOException {
        for (MapoSource source : getItems()) {
            if (source.isVisible()) {
                try {
                    source.setCollection(source.loadCollection());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        Mapton.getGlobalState().put(Mapo.KEY_SOURCE_MANAGER, null);
    }

    public ArrayList<MapoSource> loadItems() {
        try {
            return Mapo.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<MapoSource>>() {
            }.getType());
        } catch (IOException | JsonSyntaxException ex) {
            return new ArrayList<>();
        }
    }

    public void removeAll(MapoSource... localGrids) {
        getItems().removeAll(localGrids);
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSourcesFile(), Mapo.getGson().toJson(mItems), "utf-8");
    }

    public void sourceExport(File file, ArrayList<MapoSource> selectedSources) throws IOException {
        FileUtils.writeStringToFile(file, Mapo.getGson().toJson(selectedSources), "utf-8");
    }

    public void sourceImport(File file) throws IOException {
        String json = FileUtils.readFileToString(file, "utf-8");
        ArrayList<MapoSource> sources = Mapo.getGson().fromJson(json, new TypeToken<ArrayList<MapoSource>>() {
        }.getType());

        Platform.runLater(() -> {
            mItems.addAll(sources);
            FXCollections.sort(mItems, (MapoSource o1, MapoSource o2) -> o1.getName().compareTo(o2.getName()));
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
