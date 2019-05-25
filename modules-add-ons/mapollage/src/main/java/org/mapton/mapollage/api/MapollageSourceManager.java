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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.mapton.mapollage.Options;
import static org.mapton.mapollage.Options.KEY_SOURCES;
import org.mapton.mapollage.ui.SourcePanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class MapollageSourceManager {

    private final Gson mGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private final ObservableList<MapollageSource> mItems = FXCollections.observableArrayList();
    private final Options mOptions = Options.getInstance();

    public static MapollageSourceManager getInstance() {
        return Holder.INSTANCE;
    }

    private MapollageSourceManager() {
    }

    public void edit(final MapollageSource aLocalGrid) {
        SwingUtilities.invokeLater(() -> {
            MapollageSource newLocalGrid = aLocalGrid;
            boolean add = aLocalGrid == null;
            if (add) {
                newLocalGrid = new MapollageSource();
            }

            final MapollageSource localGrid = newLocalGrid;
            SourcePanel localGridPanel = new SourcePanel();
            DialogDescriptor d = new DialogDescriptor(localGridPanel, Dict.SOURCE.toString());
            localGridPanel.setDialogDescriptor(d);
            localGridPanel.initFx(() -> {
                localGridPanel.load(localGrid);
            });

            localGridPanel.setPreferredSize(new Dimension(600, 300));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(localGrid);
                    if (add) {
                        mItems.add(localGrid);
                    }

                    FXCollections.sort(mItems, (MapollageSource o1, MapollageSource o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public ObservableList<MapollageSource> getItems() {
        return mItems;
    }

    public ArrayList<MapollageSource> loadItems() {
        return mGson.fromJson(mOptions.get(KEY_SOURCES), new TypeToken<ArrayList<MapollageSource>>() {
        }.getType());
    }

    public void removeAll(MapollageSource... localGrids) {
        getItems().removeAll(localGrids);
    }

    public void save() {
        mOptions.put(KEY_SOURCES, mGson.toJson(mItems));
    }

    public void sourceExport(File file, ArrayList<MapollageSource> selectedSources) throws IOException {
        FileUtils.writeStringToFile(file, mGson.toJson(selectedSources), "utf-8");
    }

    public void sourceImport(File file) throws IOException {
        String json = FileUtils.readFileToString(file, "utf-8");
        ArrayList<MapollageSource> sources = mGson.fromJson(json, new TypeToken<ArrayList<MapollageSource>>() {
        }.getType());

        Platform.runLater(() -> {
            mItems.addAll(sources);
            FXCollections.sort(mItems, (MapollageSource o1, MapollageSource o2) -> o1.getName().compareTo(o2.getName()));
        });
    }

    private static class Holder {

        private static final MapollageSourceManager INSTANCE = new MapollageSourceManager();
    }
}
