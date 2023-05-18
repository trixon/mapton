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

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgBinaryCondition;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.db.DbBaseManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class MBookmarkManager extends DbBaseManager {

    public static final String COL_CATEGORY = "category";
    public static final String COL_COLOR = "color";
    public static final String COL_CREATED = "created";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DISPLAY_MARKER = "display_marker";
    public static final String COL_ID = "bookmark_id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_MODIFIED = "modified";
    public static final String COL_NAME = "name";
    public static final String COL_URL = "url";
    public static final String COL_ZOOM = "zoom";
    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);

    private final DbColumn mCategory;
    private final DbColumn mColor;
    private final Columns mColumns = new Columns();
    private final DbColumn mDescription;
    private final DbColumn mDisplayMarker;
    private final ObjectProperty<ObservableList<MBookmark>> mItemsProperty = new SimpleObjectProperty<>();
    private final DbColumn mLatitude;
    private final DbColumn mLongitude;
    private final DbColumn mName;
    private String mStoredFilter = "";
    private final DbColumn mTimeCreated;
    private final DbColumn mTimeModified;
    private final DbColumn mUrl;
    private final DbColumn mZoom;

    public static MBookmarkManager getInstance() {
        return Holder.INSTANCE;
    }

    private MBookmarkManager() {
        mTable = getSchema().addTable("bookmark");

        mId = mTable.addColumn(COL_ID, SQL_IDENTITY, null);
        mName = mTable.addColumn(COL_NAME, SQL_VARCHAR, TYPE_LENGTH_1M);
        mCategory = mTable.addColumn(COL_CATEGORY, SQL_VARCHAR, TYPE_LENGTH_1M);
        mDescription = mTable.addColumn(COL_DESCRIPTION, SQL_VARCHAR, TYPE_LENGTH_1M);
        mUrl = mTable.addColumn(COL_URL, SQL_VARCHAR, TYPE_LENGTH_1M);
        mColor = mTable.addColumn(COL_COLOR, SQL_VARCHAR, TYPE_LENGTH_1M);
        mDisplayMarker = mTable.addColumn(COL_DISPLAY_MARKER, SQL_BOOLEAN, null);
        mLatitude = mTable.addColumn(COL_LATITUDE, SQL_DOUBLE, null);
        mLongitude = mTable.addColumn(COL_LONGITUDE, SQL_DOUBLE, null);
        mZoom = mTable.addColumn(COL_ZOOM, SQL_DOUBLE, null);
        mTimeCreated = mTable.addColumn(COL_CREATED, SQL_TIMESTAMP, null);
        mTimeModified = mTable.addColumn(COL_MODIFIED, SQL_TIMESTAMP, null);

        addNotNullConstraints(mName, mCategory, mDescription);
        create();
        mItemsProperty.setValue(FXCollections.observableArrayList());
        addMissingColumns();
        dbLoad();
    }

    public Columns columns() {
        return mColumns;
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mName, mCategory}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mName, mCategory);

        mDb.create(mTable, false, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public synchronized void dbDelete(MBookmark bookmark) throws ClassNotFoundException, SQLException {
        mDb.delete(mTable, mId, bookmark.getId());
        dbLoad();
    }

    public synchronized void dbDelete(String category) throws ClassNotFoundException, SQLException {
        for (MBookmark bookmark : mItemsProperty.get()) {
            if (StringUtils.startsWith(bookmark.getCategory(), category)) {
                mDb.delete(mTable, mId, bookmark.getId());
            }
        }

        dbLoad();
    }

    public synchronized void dbDelete() throws ClassNotFoundException, SQLException {
        for (MBookmark bookmark : mItemsProperty.get()) {
            mDb.delete(mTable, mId, bookmark.getId());
        }

        dbLoad();
    }

    public synchronized void dbInsert(MBookmark bookmark) throws ClassNotFoundException, SQLException {
        dbInsertSilent(bookmark);
        dbLoad();
    }

    public synchronized Point dbInsert(ArrayList<MBookmark> bookmarks) {
        int imports = 0;
        int errors = 0;

        for (MBookmark bookmark : bookmarks) {
            try {
                dbInsertSilent(bookmark);
                imports++;
            } catch (ClassNotFoundException | SQLException ex) {
                errors++;
            }
        }

        Platform.runLater(() -> {
            dbLoad();
        });

        return new Point(imports, errors);
    }

    public synchronized ArrayList<MBookmark> dbLoad() {
        return dbLoad(mStoredFilter, true);
    }

    public synchronized ArrayList<MBookmark> dbLoad(String filter, boolean addToList) {
        if (mSelectPreparedStatement == null) {
            mSelectPlaceHolders.init(
                    mCategory,
                    mName,
                    mDescription
            );

            ComboCondition comboCondition = ComboCondition.or(
                    PgBinaryCondition.iLike(mCategory, mSelectPlaceHolders.get(mCategory)),
                    PgBinaryCondition.iLike(mDescription, mSelectPlaceHolders.get(mDescription)),
                    PgBinaryCondition.iLike(mName, mSelectPlaceHolders.get(mName))
            );

            SelectQuery selectQuery = new SelectQuery()
                    .addAllTableColumns(mTable)
                    .addOrderings(mCategory, mName, mDescription)
                    .addCondition(comboCondition)
                    .validate();

            String sql = selectQuery.toString();

            try {
                mSelectPreparedStatement = mDb.getAutoCommitConnection().prepareStatement(sql);
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        ArrayList<MBookmark> bookmarks = new ArrayList<>();

        try {
            mStoredFilter = filter;
            filter = getFilterPattern(filter);
            mSelectPlaceHolders.get(mCategory).setString(filter, mSelectPreparedStatement);
            mSelectPlaceHolders.get(mName).setString(filter, mSelectPreparedStatement);
            mSelectPlaceHolders.get(mDescription).setString(filter, mSelectPreparedStatement);

            ResultSet rs = mSelectPreparedStatement.executeQuery();
            rs.beforeFirst();

            while (rs.next()) {
                var bookmark = new MBookmark();
                bookmark.setId(getLong(rs, mId));
                bookmark.setName(getString(rs, mName));
                bookmark.setCategory(getString(rs, mCategory));
                bookmark.setDescription(getString(rs, mDescription));
                bookmark.setUrl(getString(rs, mUrl));
                bookmark.setColor(getString(rs, mColor));
                bookmark.setDisplayMarker(getBoolean(rs, mDisplayMarker));
                bookmark.setLatitude(getDouble(rs, mLatitude));
                bookmark.setLongitude(getDouble(rs, mLongitude));
                bookmark.setZoom(getDouble(rs, mZoom));
                bookmark.setTimeCreated(getTimestamp(rs, mTimeCreated));
                bookmark.setTimeModified(getTimestamp(rs, mTimeModified));

                bookmarks.add(bookmark);
            }
            if (addToList) {
                getItems().setAll(bookmarks);
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

        //debugPrint();
        return bookmarks;
    }

    public synchronized void dbTruncate() throws ClassNotFoundException, SQLException {
        mDb.truncate(mTable);
        dbLoad();
    }

    public void dbUpdate(MBookmark bookmark) throws SQLException {
        if (mUpdatePreparedStatement == null) {
            mUpdatePlaceHolders.init(
                    mId,
                    mName,
                    mCategory,
                    mDescription,
                    mUrl,
                    mColor,
                    mDisplayMarker,
                    mLatitude,
                    mLongitude,
                    mZoom,
                    mTimeModified
            );

            UpdateQuery updateQuery = new UpdateQuery(mTable)
                    .addCondition(BinaryCondition.equalTo(mId, mUpdatePlaceHolders.get(mId)))
                    .addSetClause(mName, mUpdatePlaceHolders.get(mName))
                    .addSetClause(mCategory, mUpdatePlaceHolders.get(mCategory))
                    .addSetClause(mDescription, mUpdatePlaceHolders.get(mDescription))
                    .addSetClause(mUrl, mUpdatePlaceHolders.get(mUrl))
                    .addSetClause(mColor, mUpdatePlaceHolders.get(mColor))
                    .addSetClause(mDisplayMarker, mUpdatePlaceHolders.get(mDisplayMarker))
                    .addSetClause(mLatitude, mUpdatePlaceHolders.get(mLatitude))
                    .addSetClause(mLongitude, mUpdatePlaceHolders.get(mLongitude))
                    .addSetClause(mZoom, mUpdatePlaceHolders.get(mZoom))
                    .addSetClause(mTimeModified, mUpdatePlaceHolders.get(mTimeModified))
                    .validate();

            String sql = updateQuery.toString();
            mUpdatePreparedStatement = mDb.getAutoCommitConnection().prepareStatement(sql);
        }

        mUpdatePlaceHolders.get(mId).setLong(bookmark.getId(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mName).setString(bookmark.getName(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mCategory).setString(bookmark.getCategory(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mDescription).setString(bookmark.getDescription(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mUrl).setString(bookmark.getUrl(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mColor).setString(bookmark.getColor(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mDisplayMarker).setBoolean(bookmark.isDisplayMarker(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mLatitude).setObject(bookmark.getLatitude(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mLongitude).setObject(bookmark.getLongitude(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mZoom).setObject(bookmark.getZoom(), mUpdatePreparedStatement);

        mUpdatePreparedStatement.setTimestamp(mUpdatePlaceHolders.get(mTimeModified).getIndex(), bookmark.getTimeModified());

        mUpdatePreparedStatement.executeUpdate();
    }

    public boolean exists(Object exceptForValue, String name, String category) {
        HashMap<DbColumn, Object> map = new HashMap<>();
        map.put(mName, name);
        map.put(mCategory, category);

        return exists(mId, exceptForValue, map);
    }

    public TreeSet<String> getCategories() {
        SelectQuery selectQuery = new SelectQuery()
                .addFromTable(mTable)
                .addColumns(mCategory)
                .addOrdering(mCategory, OrderObject.Dir.ASCENDING)
                .addGroupings(mCategory)
                .validate();

        TreeSet<String> categories = new TreeSet<>();
        categories.add("");
        try ( Statement statement = mDb.getAutoCommitConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String sql = selectQuery.toString();
            ResultSet rs = statement.executeQuery(sql);
            rs.beforeFirst();
            while (rs.next()) {
                categories.add(getString(rs, mCategory));
            }

        } catch (SQLException ex) {
            //Exceptions.printStackTrace(ex);
        }

        return categories;
    }

    public MLatLonBox getExtents(String category) {
        ArrayList<MLatLon> latLons = new ArrayList<>();

        mItemsProperty.get().stream()
                .filter((bookmark) -> (StringUtils.startsWith(bookmark.getCategory(), category)))
                .forEachOrdered((bookmark) -> {
                    latLons.add(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()));
                });

        return new MLatLonBox(latLons);
    }

    public final ObservableList<MBookmark> getItems() {
        return mItemsProperty.get();
    }

    public void goTo(MBookmark bookmark) throws ClassNotFoundException, SQLException {
        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()), bookmark.getZoom());
    }

    public final ObjectProperty<ObservableList<MBookmark>> itemsProperty() {
        return mItemsProperty;
    }

    private void addMissingColumns() {
        try {
            mDb.addMissingColumn(mTable.getAbsoluteName(), COL_COLOR, SQL_VARCHAR + "(10)", COL_DESCRIPTION);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void dbInsertSilent(MBookmark bookmark) throws ClassNotFoundException, SQLException {
        if (mInsertPreparedStatement == null) {
            mInsertPlaceHolders.init(
                    mName,
                    mCategory,
                    mDescription,
                    mUrl,
                    mColor,
                    mDisplayMarker,
                    mLatitude,
                    mLongitude,
                    mZoom,
                    mTimeCreated
            );

            InsertQuery insertQuery = new InsertQuery(mTable)
                    .addColumn(mName, mInsertPlaceHolders.get(mName))
                    .addColumn(mCategory, mInsertPlaceHolders.get(mCategory))
                    .addColumn(mDescription, mInsertPlaceHolders.get(mDescription))
                    .addColumn(mUrl, mInsertPlaceHolders.get(mUrl))
                    .addColumn(mColor, mInsertPlaceHolders.get(mColor))
                    .addColumn(mDisplayMarker, mInsertPlaceHolders.get(mDisplayMarker))
                    .addColumn(mLatitude, mInsertPlaceHolders.get(mLatitude))
                    .addColumn(mLongitude, mInsertPlaceHolders.get(mLongitude))
                    .addColumn(mZoom, mInsertPlaceHolders.get(mZoom))
                    .addColumn(mTimeCreated, mInsertPlaceHolders.get(mTimeCreated))
                    .validate();

            String sql = insertQuery.toString();
            mInsertPreparedStatement = mDb.getAutoCommitConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            //System.out.println(mInsertPreparedStatement.toString());
        }

        mInsertPlaceHolders.get(mName).setString(bookmark.getName(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mCategory).setString(bookmark.getCategory(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mDescription).setString(bookmark.getDescription(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mUrl).setString(bookmark.getUrl(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mColor).setString(bookmark.getColor(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mDisplayMarker).setBoolean(bookmark.isDisplayMarker(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLatitude).setObject(bookmark.getLatitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLongitude).setObject(bookmark.getLongitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mZoom).setObject(bookmark.getZoom(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mTimeCreated).setObject(new Timestamp(System.currentTimeMillis()), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            Exceptions.printStackTrace(new SQLException("Creating bookmark failed"));
        }

    }

    private void debugPrint() {
        System.out.println("debugPrint");
        for (MBookmark bookmark : getItems()) {
            System.out.println(ToStringBuilder.reflectionToString(bookmark, ToStringStyle.MULTI_LINE_STYLE));
        }
    }

    private static class Holder {

        private static final MBookmarkManager INSTANCE = new MBookmarkManager();
    }
}
