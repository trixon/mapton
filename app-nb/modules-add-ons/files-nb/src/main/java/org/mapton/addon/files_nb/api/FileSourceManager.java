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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class FileSourceManager {

    private File mConfigDir;
    private ObjectProperty<ObservableList<FileSource>> mItems = new SimpleObjectProperty<>();
    private File mSourcesFile;

    public static FileSourceManager getInstance() {
        return Holder.INSTANCE;
    }

    private FileSourceManager() {
        mItems.setValue(FXCollections.observableArrayList());
    }

    public boolean contains(File file) {
        if (mItems.get().stream().anyMatch(fileSource -> (fileSource.getFile().equals(file)))) {
            return true;
        }

        return false;
    }

    public File getConfigDir() {
        if (mConfigDir == null) {
            mConfigDir = new File(Mapton.getConfigDir(), "files");
        }

        return mConfigDir;
    }

    public ObservableList<FileSource> getItems() {
        return mItems.get();
    }

    public final ObjectProperty<ObservableList<FileSource>> itemsProperty() {
        if (mItems == null) {
            mItems = new SimpleObjectProperty<>(this, "items");
        }

        return mItems;
    }

    public void load() {
        ArrayList<FileSource> loadedItems = new ArrayList<>();

        try {
            if (getSourcesFile().isFile()) {
                loadedItems = Mapo.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<FileSource>>() {
                }.getType());
            }
        } catch (IOException | JsonSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        final ArrayList<FileSource> items = loadedItems;
        Platform.runLater(() -> {
            mItems.get().setAll(items);
            Mapton.getGlobalState().put(Mapo.KEY_SOURCE_UPDATED, this);
        });
    }

    public void removeAll(FileSource... fileSources) {
        try {
            if (fileSources == null || fileSources.length == 0) {
                mItems.get().clear();
            } else {
                mItems.get().removeAll(fileSources);
            }
        } catch (Exception e) {
        }
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSourcesFile(), Mapo.getGson().toJson(mItems.get()), "utf-8");
    }

    public void sort() {
        Comparator<FileSource> c1 = (FileSource o1, FileSource o2) -> Boolean.compare(o1.getFile().isFile(), o2.getFile().isFile());
        Comparator<FileSource> c2 = (FileSource o1, FileSource o2) -> o1.getFile().getName().toLowerCase(Locale.getDefault()).compareTo(o2.getFile().getName().toLowerCase(Locale.getDefault()));

        FXCollections.sort(mItems.get(), c1.thenComparing(c2));
    }

    private File getSourcesFile() {
        if (mSourcesFile == null) {
            mSourcesFile = new File(getConfigDir(), "sources.json");
        }

        return mSourcesFile;
    }

    private static class Holder {

        private static final FileSourceManager INSTANCE = new FileSourceManager();
    }
}
