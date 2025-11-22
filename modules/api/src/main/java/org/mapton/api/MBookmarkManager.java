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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.jackson.LocalDateTimeDeserializer;
import org.mapton.api.jackson.LocalDateTimeSerializer;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MBookmarkManager extends MBaseDataManager<MBookmark> {

    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);
    private final File mFile;
    private final CsvMapper mMapper;
    private final CsvSchema mSchema;
    private String mStoredFilter = "";

    public static MBookmarkManager getInstance() {
        return Holder.INSTANCE;
    }

    private MBookmarkManager() {
        super(MBookmark.class);
        mFile = new File(Places.getUserDirectory(), "bookmarks.csv");
        var simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        mMapper = CsvMapper.builder()
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .addModule(simpleModule)
                .addModule(new JavaTimeModule())
                .build();
        mMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        mMapper.setVisibility(mMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        mSchema = mMapper.schemaFor(MBookmark.class)
                .withHeader()
                .withQuoteChar('"');

        csvLoad();
    }

    public void add(MBookmark bookmark) {
        getItems().add(bookmark);
        csvSave();
    }

    public void delete(MBookmark bookmark) {
        getItems().remove(bookmark);
        csvSave();
    }

    public void delete(String category) {
        getItems().removeAll(
                getItems().stream()
                        .filter(b -> Strings.CS.startsWith(b.getCategory(), category))
                        .toList()
        );
        csvSave();
    }

    public void deleteAll() {
        getItems().clear();
        csvSave();
    }

    public boolean exists(Object exceptForValue, String name, String category) {
        return false;
    }

    public TreeSet<String> getCategories() {
        return getItems().stream()
                .map(b -> b.getCategory())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public MLatLonBox getExtents(String category) {
        ArrayList<MLatLon> latLons = new ArrayList<>();

        mItemsProperty.get().stream()
                .filter((bookmark) -> (Strings.CS.startsWith(bookmark.getCategory(), category)))
                .forEachOrdered(bookmark -> {
                    latLons.add(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()));
                });

        return new MLatLonBox(latLons);
    }

//    public final ObservableList<MBookmark> getItems() {
//        return mItemsProperty.get();
//    }
    public Stream<MBookmark> getItems(String category) {
        return getItems().stream().filter(b -> Strings.CS.startsWith(b.getCategory(), category));
    }

    public void goTo(MBookmark bookmark) {
        Mapton.getEngine().panTo(bookmark.getLatLon(), bookmark.getZoom());
    }

//    public final ObjectProperty<ObservableList<MBookmark>> itemsProperty() {
//        return mItemsProperty;
//    }
    public void save() {
        csvSave();
    }

    public List<MBookmark> search(String filter) {
        return getItems().stream().filter(b -> {
            return StringHelper.matchesSimpleGlob(filter, true, true, b.getName(), b.getCategory(), b.getDescription());
//            return Strings.CI.contains(b.getName(), filter)
//                    || Strings.CI.contains(b.getCategory(), filter)
//                    || Strings.CI.contains(b.getDescription(), filter);
        })
                .toList();

    }

    @Override
    protected void applyTemporalFilter() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    protected void load(ArrayList<MBookmark> items) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void csvLoad() {
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

    private void csvSave() {
        debugPrint();
        try {
            mMapper.writer(mSchema).writeValue(mFile, getAllItems());
//            var objectWriter = mapper.writer(mSchema);
//            try (var sequenceWriter = objectWriter.writeValues(mFile)) {
//                sequenceWriter.writeAll(getItems());
//            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void debugPrint() {
        System.out.println("debugPrint");
        for (var bookmark : getAllItems()) {
            System.out.println(ToStringBuilder.reflectionToString(bookmark, ToStringStyle.MULTI_LINE_STYLE));
        }
    }

    private static class Holder {

        private static final MBookmarkManager INSTANCE = new MBookmarkManager();
    }
}
