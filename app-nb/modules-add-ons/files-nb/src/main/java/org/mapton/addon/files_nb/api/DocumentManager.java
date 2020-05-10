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
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class DocumentManager {

    private File mConfigDir;
    private ObjectProperty<ObservableList<Document>> mItemsProperty = new SimpleObjectProperty<>();
    private File mSourcesFile;
    private LongProperty mUpdatedProperty = new SimpleLongProperty();

    public static DocumentManager getInstance() {
        return Holder.INSTANCE;
    }

    private DocumentManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
    }

    public boolean addIfMissing(File file) {
        if (!contains(file)) {
            getItems().add(new Document(file));

            return true;
        }

        return false;
    }

    public boolean contains(File file) {
        if (mItemsProperty.get().stream().anyMatch(fileSource -> (fileSource.getFile().equals(file)))) {
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

    public ObservableList<Document> getItems() {
        return mItemsProperty.get();
    }

    public final ObjectProperty<ObservableList<Document>> itemsProperty() {
        if (mItemsProperty == null) {
            mItemsProperty = new SimpleObjectProperty<>(this, "items");
        }

        return mItemsProperty;
    }

    public void load() {
        ArrayList<Document> loadedItems = new ArrayList<>();

        try {
            if (getSourcesFile().isFile()) {
                loadedItems = Mapo.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<Document>>() {
                }.getType());
            }
        } catch (IOException | JsonSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        final ArrayList<Document> items = loadedItems;
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

    public void removeAll(Document... documents) {
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
        Comparator<Document> c1 = (Document o1, Document o2) -> Boolean.compare(o1.getFile().isFile(), o2.getFile().isFile());
        Comparator<Document> c2 = (Document o1, Document o2) -> o1.getFile().getName().toLowerCase(Locale.getDefault()).compareTo(o2.getFile().getName().toLowerCase(Locale.getDefault()));

        FXCollections.sort(mItemsProperty.get(), c1.thenComparing(c2));
    }

    public LongProperty updatedProperty() {
        return mUpdatedProperty;
    }

    private File getSourcesFile() {
        if (mSourcesFile == null) {
            mSourcesFile = new File(getConfigDir(), "sources.json");
        }

        return mSourcesFile;
    }

    private static class Holder {

        private static final DocumentManager INSTANCE = new DocumentManager();
    }
}
