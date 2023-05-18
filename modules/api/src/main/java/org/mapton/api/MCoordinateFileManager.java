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
package org.mapton.api;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MCoordinateFileManager {

    public static final String KEY_SOURCE_UPDATED = "files.source_updated";

    private File mConfigDir;
    private final MFileWatcher mFileWatcher = MFileWatcher.getInstance();
    private ObjectProperty<ObservableList<MCoordinateFile>> mItemsProperty = new SimpleObjectProperty<>();
    private File mSourcesFile;
    private final LongProperty mUpdatedProperty = new SimpleLongProperty();

    public static MCoordinateFileManager getInstance() {
        return Holder.INSTANCE;
    }

    private MCoordinateFileManager() {
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

    public String[] getFileOpenerKeys() {
        var filterredFileOpeners = new ArrayList<String>();

        Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).stream()
                .filter(coordinateFileOpener -> (coordinateFileOpener.isUsedInFiles()))
                .forEachOrdered(coordinateFileOpener -> {
                    filterredFileOpeners.add(coordinateFileOpener.getClass().getName());
                });

        return filterredFileOpeners.toArray(new String[0]);
    }

    public ObservableList<MCoordinateFile> getItems() {
        return mItemsProperty.get();
    }

//    public ArrayList<MCoordinateFile> getSublistByExtensions(String... extensions) {
//        ArrayList<MCoordinateFile> coordinateFiles = new ArrayList<>();
//
//        for (var coordinateFile : mItemsProperty.get()) {
//            String ext = FilenameUtils.getExtension(coordinateFile.getFile().getName()).toLowerCase(Locale.getDefault());
//            if (StringUtils.equalsAnyIgnoreCase(ext, extensions)) {
//                coordinateFiles.add(coordinateFile);
//            }
//        }
//
//        return coordinateFiles;
//    }
    public ArrayList<MCoordinateFile> getSublistBySupportedOpeners(Set<String> coordinateFileOpeners) {
        ArrayList<MCoordinateFile> coordinateFiles = new ArrayList<>();

        for (var coordinateFile : mItemsProperty.get()) {
            var opener = coordinateFile.getCoordinateFileOpenerName();
            if (coordinateFileOpeners.contains(opener)) {
                coordinateFiles.add(coordinateFile);
            }
        }

        return coordinateFiles;
    }

    public final ObjectProperty<ObservableList<MCoordinateFile>> itemsProperty() {
        if (mItemsProperty == null) {
            mItemsProperty = new SimpleObjectProperty<>(this, "items");
        }

        return mItemsProperty;
    }

    public void load() {
        var loadedItems = new ArrayList<MCoordinateFile>();

        try {
            if (getSourcesFile().isFile()) {
                loadedItems = Mapton.getGson().fromJson(FileUtils.readFileToString(getSourcesFile(), "utf-8"), new TypeToken<ArrayList<MCoordinateFile>>() {
                }.getType());

                var invalidItems = new ArrayList<MCoordinateFile>();
                for (var coordinateFile : loadedItems) {
                    if (!coordinateFile.getFile().isFile()) {
                        invalidItems.add(coordinateFile);
                    }
                }

                loadedItems.removeAll(invalidItems);
            }
        } catch (IOException | JsonSyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        for (var coordinateFile : loadedItems) {
            addWatcher(coordinateFile);
        }

        final var items = loadedItems; //Lambda below needs final
        Platform.runLater(() -> {
            mItemsProperty.get().setAll(items);
            Mapton.getGlobalState().put(KEY_SOURCE_UPDATED, this);
        });
    }

    public void refresh() {
        long now = System.currentTimeMillis();
        if (mUpdatedProperty.get() != now) {
            mUpdatedProperty.set(now);
        }
    }

    public void removeAll(MCoordinateFile... coordinateFiles) {
        FxHelper.runLater(() -> {
            try {
                if (coordinateFiles == null || coordinateFiles.length == 0) {
                    mItemsProperty.get().clear();
                } else {
                    mItemsProperty.get().removeAll(coordinateFiles);
                }
            } catch (Exception e) {
            }
        });
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSourcesFile(), Mapton.getGson().toJson(mItemsProperty.get()), "utf-8");
        refresh();
    }

    public void sort() {
        Comparator<MCoordinateFile> c1 = (o1, o2) -> o1.getFile().getName().toLowerCase(Locale.getDefault()).compareTo(o2.getFile().getName().toLowerCase(Locale.getDefault()));

        FXCollections.sort(mItemsProperty.get(), c1);
    }

    public LongProperty updatedProperty() {
        return mUpdatedProperty;
    }

    private void addFiles(ArrayList<MCoordinateFile> coordinateFiles) {
        if (coordinateFiles != null) {
            for (var coordinateFile : coordinateFiles) {
                addIfMissing(coordinateFile);
            }
        }

        sort();
    }

    private boolean addIfMissing(MCoordinateFile coordinateFile) {
        if (!contains(coordinateFile)) {
            getItems().add(coordinateFile);
            addWatcher(coordinateFile);

            return true;
        }

        return false;
    }

    private void addWatcher(MCoordinateFile coordinateFile) {
        mFileWatcher.addWatch(coordinateFile.getFile(), TimeUnit.SECONDS.toMillis(1), new MFileWatcherListener() {
            @Override
            public void onFileChange(File file) {
                FxHelper.runLater(() -> {
                    mItemsProperty.get().removeAll(coordinateFile);
                    addIfMissing(coordinateFile);
                    sort();
                    refresh();
                });
            }

            @Override
            public void onFileDelete(File file) {
                removeAll(coordinateFile);
                refresh();
            }
        });
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
        }, getFileOpenerKeys());
    }

    private static class Holder {

        private static final MCoordinateFileManager INSTANCE = new MCoordinateFileManager();
    }
}
