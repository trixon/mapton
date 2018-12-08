/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_grid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Dimension;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.mapton.api.MDict;
import static org.mapton.ww_grid.Options.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridManager {

    private final Gson mGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private final ObservableList<LocalGrid> mItems = FXCollections.observableArrayList();
    private final Options mOptions = Options.getInstance();

    public static LocalGridManager getInstance() {
        return Holder.INSTANCE;
    }

    private LocalGridManager() {
    }

    public void edit(final LocalGrid aLocalGrid) {
        SwingUtilities.invokeLater(() -> {
            LocalGrid newLocalGrid = aLocalGrid;
            boolean add = aLocalGrid == null;
            if (add) {
                newLocalGrid = new LocalGrid();
            }

            final LocalGrid localGrid = newLocalGrid;
            LocalGridPanel localGridPanel = new LocalGridPanel();
            DialogDescriptor d = new DialogDescriptor(localGridPanel, MDict.GRID.toString());
            localGridPanel.setDialogDescriptor(d);
            localGridPanel.initFx(() -> {
                localGridPanel.load(localGrid);
            });

            localGridPanel.setPreferredSize(new Dimension(400, 400));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(localGrid);
                    if (add) {
                        mItems.add(localGrid);
                    }

                    FXCollections.sort(mItems, (LocalGrid o1, LocalGrid o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public ObservableList<LocalGrid> getItems() {
        return mItems;
    }

    public ArrayList<LocalGrid> loadItems() {
        return mGson.fromJson(mOptions.get(KEY_LOCAL_GRIDS), new TypeToken<ArrayList<LocalGrid>>() {
        }.getType());
    }

    void removeAll(LocalGrid... localGrids) {
        getItems().removeAll(localGrids);
    }

    void save() {
        mOptions.set(KEY_LOCAL_GRIDS, mGson.toJson(mItems));
    }

    private static class Holder {

        private static final LocalGridManager INSTANCE = new LocalGridManager();
    }
}
