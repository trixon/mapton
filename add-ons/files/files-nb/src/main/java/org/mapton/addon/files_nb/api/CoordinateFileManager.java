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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.mapton.addon.files_nb.coordinate_file_openers.GeoCoordinateFileOpener;
import org.mapton.addon.files_nb.coordinate_file_openers.KmlCoordinateFileOpener;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class CoordinateFileManager {

    private File mConfigDir;
    private ObjectProperty<ObservableList<MCoordinateFile>> mItemsProperty = new SimpleObjectProperty<>();
    private File mSourcesFile;
    private final LongProperty mUpdatedProperty = new SimpleLongProperty();

    public static CoordinateFileManager getInstance() {
        return Holder.INSTANCE;
    }

    private CoordinateFileManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        initListeners();
    }

    public boolean contains(MCoordinateFile coordinateFile) {
        return mItemsProperty.get().stream().anyMatch(fileSource -> (fileSource.getFile().equals(coordinateFile.getFile())));
    }

    public File getConfigDir() {
        if (mConfigDir == null) {
            mConfigDir = new File(Mapton.getConfigDir(), "files");
        }

        return mConfigDir;
    }

    public ObservableList<MCoordinateFile> getItems() {
        return mItemsProperty.get();
    }

    public final ObjectProperty<ObservableList<MCoordinateFile>> itemsProperty() {
        if (mItemsProperty == null) {
            mItemsProperty = new SimpleObjectProperty<>(this, "items");
        }

        return mItemsProperty;
    }

    public void load() {
        ArrayList<MCoordinateFile> loadedItems = new ArrayList<>();

        try {
            if (getSourcesFile().isFile()) {
                loadedItems = Mapo.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<MCoordinateFile>>() {
                }.getType());
            }
        } catch (IOException | JsonSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        final ArrayList<MCoordinateFile> items = loadedItems;

        Platform.runLater(() -> {
            mItemsProperty.get().setAll(items);
            Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, this);
        });
    }

    public void refresh() {
        long now = System.currentTimeMillis();
        if (mUpdatedProperty.get() != now) {
            mUpdatedProperty.set(now);
        }
    }

    public void removeAll(MCoordinateFile... documents) {
        FxHelper.runLater(() -> {
            try {
                if (documents == null || documents.length == 0) {
                    mItemsProperty.get().clear();
                } else {
                    mItemsProperty.get().removeAll(documents);
                }
            } catch (Exception e) {
            }
        });
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSourcesFile(), Mapo.getGson().toJson(mItemsProperty.get()), "utf-8");
        refresh();
    }

    public void sort() {
        Comparator<MCoordinateFile> c1 = (MCoordinateFile o1, MCoordinateFile o2) -> o1.getFile().getName().toLowerCase(Locale.getDefault()).compareTo(o2.getFile().getName().toLowerCase(Locale.getDefault()));

        FXCollections.sort(mItemsProperty.get(), c1);
    }

    public LongProperty updatedProperty() {
        return mUpdatedProperty;
    }

    private void addFiles(ArrayList<MCoordinateFile> fileOpenerFiles) {
        for (MCoordinateFile fileOpenerFile : fileOpenerFiles) {
            addIfMissing(fileOpenerFile);
        }

        sort();
    }

    private boolean addIfMissing(MCoordinateFile coordinateFile) {
        if (!contains(coordinateFile)) {
            getItems().add(coordinateFile);

            return true;
        }

        return false;
    }

    private File getSourcesFile() {
        if (mSourcesFile == null) {
            mSourcesFile = new File(getConfigDir(), "sources.json");
        }

        return mSourcesFile;
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            FxHelper.runLater(() -> {
                addFiles(gsce.getValue());
            });
        }, GeoCoordinateFileOpener.class.getName(), KmlCoordinateFileOpener.class.getName());
    }

    private static class Holder {

        private static final CoordinateFileManager INSTANCE = new CoordinateFileManager();
    }
}
