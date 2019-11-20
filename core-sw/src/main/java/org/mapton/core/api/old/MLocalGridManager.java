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
package org.mapton.core.api.old;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import static org.mapton.api.MOptions.KEY_LOCAL_GRIDS;
import org.mapton.core.ui.grid.LocalGridPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MLocalGridManager {

    private final Gson mGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();
    private final ObservableList<MLocalGrid> mItems = FXCollections.observableArrayList();
    private final MOptions mOptions = MOptions.getInstance();

    public static MLocalGridManager getInstance() {
        return Holder.INSTANCE;
    }

    private MLocalGridManager() {
    }

    public void edit(final MLocalGrid aLocalGrid) {
        SwingUtilities.invokeLater(() -> {
            MLocalGrid newLocalGrid = aLocalGrid;
            boolean add = aLocalGrid == null;
            if (add) {
                newLocalGrid = new MLocalGrid();
            }

            final MLocalGrid localGrid = newLocalGrid;
            LocalGridPanel localGridPanel = new LocalGridPanel();
            DialogDescriptor d = new DialogDescriptor(localGridPanel, MDict.GRID.toString());
            localGridPanel.setDialogDescriptor(d);
            localGridPanel.initFx(() -> {
                localGridPanel.load(localGrid);
            });

            localGridPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 380));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(localGrid);
                    if (add) {
                        mItems.add(localGrid);
                    }

                    FXCollections.sort(mItems, (MLocalGrid o1, MLocalGrid o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public ObservableList<MLocalGrid> getItems() {
        return mItems;
    }

    public void gridExport(File file, ArrayList<MLocalGrid> selectedGrids) throws IOException {
        FileUtils.writeStringToFile(file, mGson.toJson(selectedGrids), "utf-8");
    }

    public void gridImport(File file) throws IOException {
        String json = FileUtils.readFileToString(file, "utf-8");
        ArrayList<MLocalGrid> grids = mGson.fromJson(json, new TypeToken<ArrayList<MLocalGrid>>() {
        }.getType());

        Platform.runLater(() -> {
            mItems.addAll(grids);
            FXCollections.sort(mItems, (MLocalGrid o1, MLocalGrid o2) -> o1.getName().compareTo(o2.getName()));
        });
    }

    public ArrayList<MLocalGrid> loadItems() {
        return mGson.fromJson(mOptions.get(KEY_LOCAL_GRIDS), new TypeToken<ArrayList<MLocalGrid>>() {
        }.getType());
    }

    public void removeAll(MLocalGrid... localGrids) {
        getItems().removeAll(localGrids);
    }

    public void save() {
        mOptions.put(KEY_LOCAL_GRIDS, mGson.toJson(mItems));
    }

    private static class Holder {

        private static final MLocalGridManager INSTANCE = new MLocalGridManager();
    }
}
