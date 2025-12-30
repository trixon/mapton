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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.jackson.LocalDateTimeDeserializer;
import org.mapton.api.jackson.LocalDateTimeSerializer;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MBookmarkManager extends MBaseDataManager<MBookmark> {

    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);
    private final File mFile;
    private final LongProperty mLastSavedProperty = new SimpleLongProperty();
    private final CsvMapper mMapper;
    private final CsvSchema mSchema;
    private String mStoredFilter = "";

    public static MBookmarkManager getInstance() {
        return Holder.INSTANCE;
    }

    private MBookmarkManager() {
        super(MBookmark.class);
        mFile = new File(Places.getUserDirectory(), "bookmarks.csv");
        initListeners();
        var simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        mMapper = CsvMapper.builder()
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModules(new JavaTimeModule(), simpleModule)
                .build();

        mMapper.setVisibility(mMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        mSchema = mMapper.schemaFor(MBookmark.class)
                .withHeader()
                .withColumnReordering(true)
                .withQuoteChar('"');

        load();
    }

    public void add(MBookmark bookmark) {
        getAllItems().add(bookmark);
        save();
    }

    public void add(List<MBookmark> bookmarks) {
        getAllItems().addAll(bookmarks);
        save();
    }

    public void add(File file) {
        if (file.isFile()) {
            try {
                MappingIterator<MBookmark> mappingIterator = mMapper
                        .readerFor(MBookmark.class)
                        .with(mSchema)
                        .readValues(file);

                getAllItems().addAll(mappingIterator.readAll());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean exists(MBookmark bookmark, String name, String category) {
        return getAllItems().stream()
                .filter(b -> b != bookmark)
                .anyMatch(p -> Strings.CI.equals(p.getName(), name) && Strings.CI.equals(p.getCategory(), category));
    }

    public void filter() {
        filter(mStoredFilter);
    }

    public void filter(String filter) {
        mStoredFilter = filter;
        var filteredItems = getAllItems().stream()
                .filter(b -> {
                    return StringHelper.matchesSimpleGlob(filter, true, true, b.getName(), b.getCategory(), b.getDescription());
                })
                .toList();

        getFilteredItems().setAll(filteredItems);
    }

    public TreeSet<String> getCategories() {
        return getAllItems().stream()
                .map(b -> b.getCategory())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public MLatLonBox getExtents(String category) {
        ArrayList<MLatLon> latLons = new ArrayList<>();

        getAllItems().stream()
                .filter((bookmark) -> (Strings.CS.startsWith(bookmark.getCategory(), category)))
                .forEachOrdered(bookmark -> {
                    latLons.add(MBookmark.createLatLon(bookmark));
                });

        return new MLatLonBox(latLons);
    }

    public Stream<MBookmark> getFilteredItems(String category) {
        return getFilteredItems().stream().filter(b -> Strings.CS.startsWith(b.getCategory(), category));
    }

    public void goTo(MBookmark bookmark) {
        Mapton.getEngine().panTo(MBookmark.createLatLon(bookmark), bookmark.getZoom());
    }

    public LongProperty lastSavedProperty() {
        return mLastSavedProperty;
    }

    public void remove(MBookmark bookmark) {
        getAllItems().remove(bookmark);
        save();
    }

    public void remove(String category) {
        getAllItems().removeAll(
                getFilteredItems().stream()
                        .filter(b -> Strings.CS.startsWith(b.getCategory(), category))
                        .toList()
        );
        save();
    }

    public void removeAll() {
        getAllItems().clear();
        save();
    }

    public void removeVisible() {
        getAllItems().removeAll(getFilteredItems());
        save();
    }

    public void save(ObservableList<MBookmark> items, File file) {
        //debugPrint();
        try {
            mMapper.writer(mSchema).writeValue(file, items);
            filter();
            FxHelper.runLater(() -> mLastSavedProperty.set(System.currentTimeMillis()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void save() {
        save(getAllItems(), mFile);
    }

    @Override
    protected void applyTemporalFilter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void load(ArrayList<MBookmark> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void debugPrint() {
        System.out.println("debugPrint");
        for (var bookmark : getAllItems()) {
            System.out.println(ToStringBuilder.reflectionToString(bookmark, ToStringStyle.MULTI_LINE_STYLE));
        }
    }

    private void initListeners() {
        selectedItemProperty().addListener((p, o, n) -> {
            LinkedHashMap<String, Object> propertyMap = null;

            if (n != null) {
                propertyMap = new LinkedHashMap<>();
                propertyMap.put(Dict.NAME.toString(), n.getName());
                propertyMap.put(Dict.DESCRIPTION.toString(), n.getDescription());
                propertyMap.put(Dict.CATEGORY.toString(), n.getCategory());
                propertyMap.put(Dict.COLOR.toString(), javafx.scene.paint.Color.web(n.getColor()));
                Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
                var latLon = MBookmark.createLatLon(n);
                var text = "%s\n\n%s\n%s".formatted(n.getName(), n.getCategory(), n.getDescription());
                var annotation = new MAnnotation(latLon, text, "bookmark");
                Mapton.getGlobalState().put(MKey.ANNOTATIONS, annotation);
            } else {
                Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, null);

            }

        });
    }

    private void load() {
        if (mFile.isFile()) {
            try {
                MappingIterator<MBookmark> mappingIterator = mMapper
                        .readerFor(MBookmark.class)
                        .with(mSchema)
                        .readValues(mFile);

                getAllItems().setAll(mappingIterator.readAll());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class Holder {

        private static final MBookmarkManager INSTANCE = new MBookmarkManager();
    }
}
