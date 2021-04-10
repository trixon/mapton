/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.addon.files.api;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.mapton.addon.files.coordinate_file_openers.GeoCoordinateFileOpener;
import org.mapton.addon.files.coordinate_file_openers.KmlCoordinateFileOpener;
import org.mapton.addon.files.coordinate_file_openers.ShpCoordinateFileOpener;
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

        for (var coordinateFile : loadedItems) {
            addMonitor(coordinateFile);
        }

        final var items = loadedItems; //Lambda below needs final
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

    public void removeAll(MCoordinateFile... coordinateFile) {
        FxHelper.runLater(() -> {
            try {
                if (coordinateFile == null || coordinateFile.length == 0) {
                    mItemsProperty.get().clear();
                } else {
                    mItemsProperty.get().removeAll(coordinateFile);
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
        Comparator<MCoordinateFile> c1 = (o1, o2) -> o1.getFile().getName().toLowerCase(Locale.getDefault()).compareTo(o2.getFile().getName().toLowerCase(Locale.getDefault()));

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
            addMonitor(coordinateFile);

            return true;
        }

        return false;
    }

    private void addMonitor(MCoordinateFile coordinateFile) {
        var file = coordinateFile.getFile();
        var directory = file.getParentFile();

        IOFileFilter directoryFilter = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.nameFileFilter(directory.getName()));

        IOFileFilter fileFilter = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.nameFileFilter(file.getName()));

        IOFileFilter filter = FileFilterUtils.or(directoryFilter, fileFilter);

        var observer = new FileAlterationObserver(directory, filter);
        var monitor = new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(5), observer);
        var listener = new FileAlterationListenerAdaptor() {
            private final File fileToMonitor = file;

            @Override
            public void onFileChange(File file) {
                if (file.equals(fileToMonitor)) {
                    refresh();
                }
            }

            @Override
            public void onFileDelete(File file) {
                if (file.equals(fileToMonitor)) {
                    removeAll(coordinateFile);
                    refresh();
                }
            }
        };

        new Thread(() -> {
            observer.addListener(listener);
            try {
                monitor.start();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
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
        }, GeoCoordinateFileOpener.class.getName(),
                KmlCoordinateFileOpener.class.getName(),
                ShpCoordinateFileOpener.class.getName()
        );
    }

    private static class Holder {

        private static final CoordinateFileManager INSTANCE = new CoordinateFileManager();
    }
}
