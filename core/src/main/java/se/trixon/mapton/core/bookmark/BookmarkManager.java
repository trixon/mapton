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
package se.trixon.mapton.core.bookmark;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.OrderObject;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.UpdateQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Constraint;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbConstraint;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import se.trixon.mapton.api.MLatLon;
import se.trixon.mapton.api.Mapton;
import se.trixon.mapton.core.db.DbBaseManager;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkManager extends DbBaseManager {

    public static final String COL_ID = "bookmark_id";

    private final DbColumn mCategory;
    private final Columns mColumns = new Columns();
    private final DbColumn mDescription;
    private final DbColumn mDisplayMarker;
    private ObjectProperty<ObservableList<Bookmark>> mItems = new SimpleObjectProperty<>();
    private final DbColumn mLatitude;
    private final DbColumn mLongitude;
    private final DbColumn mName;
    private final DbColumn mTimeAccessed;
    private final DbColumn mTimeCreated;
    private final DbColumn mTimeModified;
    private final DbColumn mZoom;

    public static BookmarkManager getInstance() {
        return Holder.INSTANCE;
    }

    private BookmarkManager() {
        mTable = getSchema().addTable("bookmark");

        mId = mTable.addColumn(COL_ID, SQL_IDENTITY, null);
        mName = mTable.addColumn("name", SQL_VARCHAR, Integer.MAX_VALUE);
        mCategory = mTable.addColumn("category", SQL_VARCHAR, Integer.MAX_VALUE);
        mDescription = mTable.addColumn("description", SQL_VARCHAR, Integer.MAX_VALUE);
        mDisplayMarker = mTable.addColumn("display_marker", SQL_BOOLEAN, null);
        mLatitude = mTable.addColumn("latitude", SQL_DOUBLE, null);
        mLongitude = mTable.addColumn("longitude", SQL_DOUBLE, null);
        mZoom = mTable.addColumn("zoom", SQL_DOUBLE, null);
        mTimeCreated = mTable.addColumn("created", SQL_TIMESTAMP, null);
        mTimeAccessed = mTable.addColumn("accessed", SQL_TIMESTAMP, null);
        mTimeModified = mTable.addColumn("modified", SQL_TIMESTAMP, null);

        addNotNullConstraints(mName, mCategory, mDescription);
        create();
        mItems.setValue(FXCollections.observableArrayList());

        dbLoad();
    }

    public Columns columns() {
        return mColumns;
    }

    @Override
    public void create() {
        String indexName = getIndexName(new DbColumn[]{mId}, "pkey");
        DbConstraint primaryKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.PRIMARY_KEY, mId);

        indexName = getIndexName(new DbColumn[]{mName}, "key");
        DbConstraint uniqueKeyConstraint = new DbConstraint(mTable, indexName, Constraint.Type.UNIQUE, mName);

        mDb.create(mTable, false, primaryKeyConstraint, uniqueKeyConstraint);
    }

    public void dbDelete(Bookmark bookmark) throws ClassNotFoundException, SQLException {
        mDb.delete(mTable, mId, bookmark.getId());
        getItems().remove(bookmark);
    }

    public void dbTruncate() throws ClassNotFoundException, SQLException {
        mDb.truncate(mTable);
        getItems().clear();

    }

    public void editBookmark(final Bookmark aBookmark) {
        SwingUtilities.invokeLater(() -> {
            Bookmark newBookmark = aBookmark;
            boolean add = aBookmark == null;
            if (add) {
                newBookmark = new Bookmark();
                newBookmark.setZoom(Mapton.getEngine().getZoom());
                newBookmark.setLatitude(Mapton.getEngine().getLatitude());
                newBookmark.setLongitude(Mapton.getEngine().getLongitude());
            }

            final Bookmark bookmark = newBookmark;
            BookmarkPanel bookmarkPanel = new BookmarkPanel();
            DialogDescriptor d = new DialogDescriptor(bookmarkPanel, Dict.BOOKMARK.toString());
            bookmarkPanel.setDialogDescriptor(d);
            bookmarkPanel.initFx(() -> {
                bookmarkPanel.load(bookmark);
            });

            bookmarkPanel.setPreferredSize(new Dimension(300, 400));

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                bookmarkPanel.save(bookmark);
                Platform.runLater(() -> {
                    try {
                        if (add) {
                            dbInsert(bookmark);
                            getItems().add(bookmark);
                        } else {
                            bookmark.setTimeModified(new Timestamp(System.currentTimeMillis()));
                            dbUpdate(bookmark);
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }

    public FxActionSwing getAddBookmarkAction() {
        FxActionSwing action = new FxActionSwing(Dict.ADD_BOOKMARK.toString(), () -> {
            editBookmark(null);
        });

        return action;
    }

    public final ObservableList<Bookmark> getItems() {
        return mItems == null ? null : mItems.get();
    }

    public void goTo(Bookmark bookmark) throws ClassNotFoundException, SQLException {
        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()), bookmark.getZoom());
        bookmark.setTimeAccessed(new Timestamp(System.currentTimeMillis()));
        dbUpdate(bookmark);
    }

    public final ObjectProperty<ObservableList<Bookmark>> itemsProperty() {
        if (mItems == null) {
            mItems = new SimpleObjectProperty<>(this, "items");
        }

        return mItems;
    }

    private Long dbInsert(Bookmark bookmark) throws ClassNotFoundException, SQLException {
        if (mInsertPreparedStatement == null) {
            mInsertPlaceHolders.init(
                    mName,
                    mCategory,
                    mDescription,
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
        mInsertPlaceHolders.get(mDisplayMarker).setBoolean(bookmark.isDisplayMarker(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLatitude).setObject(bookmark.getLatitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mLongitude).setObject(bookmark.getLongitude(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mZoom).setObject(bookmark.getZoom(), mInsertPreparedStatement);
        mInsertPlaceHolders.get(mTimeCreated).setObject(new Timestamp(System.currentTimeMillis()), mInsertPreparedStatement);

        int affectedRows = mInsertPreparedStatement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating album failed, no rows affected.");
        }

        try (ResultSet generatedKeys = mInsertPreparedStatement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new SQLException("Creating album failed, no ID obtained.");
            }
        }
    }

    private void dbLoad() {
        if (mSelectPreparedStatement == null) {
            SelectQuery selectQuery = new SelectQuery()
                    .addAllTableColumns(mTable)
                    .addOrdering(mCategory, OrderObject.Dir.ASCENDING)
                    .addOrdering(mName, OrderObject.Dir.ASCENDING)
                    .addOrdering(mDescription, OrderObject.Dir.ASCENDING)
                    .validate();

            String sql = selectQuery.toString();

            try {
                mSelectPreparedStatement = mDb.getAutoCommitConnection().prepareStatement(sql);
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        try {
            ResultSet rs = mSelectPreparedStatement.executeQuery();
            rs.beforeFirst();

            while (rs.next()) {
                Bookmark bookmark = new Bookmark();
                bookmark.setId(getLong(rs, mId));
                bookmark.setName(getString(rs, mName));
                bookmark.setCategory(getString(rs, mCategory));
                bookmark.setDescription(getString(rs, mDescription));
                bookmark.setDisplayMarker(getBoolean(rs, mDisplayMarker));
                bookmark.setLatitude(getDouble(rs, mLatitude));
                bookmark.setLongitude(getDouble(rs, mLongitude));
                bookmark.setZoom(getDouble(rs, mZoom));
                bookmark.setTimeCreated(getTimestamp(rs, mTimeCreated));
                bookmark.setTimeAccessed(getTimestamp(rs, mTimeAccessed));
                bookmark.setTimeModified(getTimestamp(rs, mTimeModified));

                getItems().add(bookmark);
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Long dbUpdate(Bookmark bookmark) throws ClassNotFoundException, SQLException {
        if (mUpdatePreparedStatement == null) {
            mUpdatePlaceHolders.init(
                    mId,
                    mName,
                    mCategory,
                    mDescription,
                    mDisplayMarker,
                    mLatitude,
                    mLongitude,
                    mZoom,
                    mTimeAccessed,
                    mTimeModified
            );

            UpdateQuery updateQuery = new UpdateQuery(mTable)
                    .addCondition(BinaryCondition.equalTo(mId, mUpdatePlaceHolders.get(mId)))
                    .addSetClause(mName, mUpdatePlaceHolders.get(mName))
                    .addSetClause(mCategory, mUpdatePlaceHolders.get(mCategory))
                    .addSetClause(mDescription, mUpdatePlaceHolders.get(mDescription))
                    .addSetClause(mDisplayMarker, mUpdatePlaceHolders.get(mDisplayMarker))
                    .addSetClause(mLatitude, mUpdatePlaceHolders.get(mLatitude))
                    .addSetClause(mLongitude, mUpdatePlaceHolders.get(mLongitude))
                    .addSetClause(mZoom, mUpdatePlaceHolders.get(mZoom))
                    .addSetClause(mTimeAccessed, mUpdatePlaceHolders.get(mTimeAccessed))
                    .addSetClause(mTimeModified, mUpdatePlaceHolders.get(mTimeModified))
                    .validate();

            String sql = updateQuery.toString();
            mUpdatePreparedStatement = mDb.getAutoCommitConnection().prepareStatement(sql);
        }

        mUpdatePlaceHolders.get(mId).setLong(bookmark.getId(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mName).setString(bookmark.getName(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mCategory).setString(bookmark.getCategory(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mDescription).setString(bookmark.getDescription(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mDisplayMarker).setBoolean(bookmark.isDisplayMarker(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mLatitude).setObject(bookmark.getLatitude(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mLongitude).setObject(bookmark.getLongitude(), mUpdatePreparedStatement);
        mUpdatePlaceHolders.get(mZoom).setObject(bookmark.getZoom(), mUpdatePreparedStatement);

        mUpdatePreparedStatement.setTimestamp(mUpdatePlaceHolders.get(mTimeAccessed).getIndex(), bookmark.getTimeAccessed());
        mUpdatePreparedStatement.setTimestamp(mUpdatePlaceHolders.get(mTimeModified).getIndex(), bookmark.getTimeModified());

        mUpdatePreparedStatement.executeUpdate();

        return bookmark.getId();
    }

    private static class Holder {

        private static final BookmarkManager INSTANCE = new BookmarkManager();
    }

    public class Columns extends DbBaseManager.Columns {

        public DbColumn getCategory() {
            return mCategory;
        }

        public DbColumn getDescription() {
            return mDescription;
        }

        public DbColumn getDisplayMarker() {
            return mDisplayMarker;
        }

        public DbColumn getLatitude() {
            return mLatitude;
        }

        public DbColumn getLongitude() {
            return mLongitude;
        }

        public DbColumn getName() {
            return mName;
        }

        public DbColumn getTimeAccessed() {
            return mTimeAccessed;
        }

        public DbColumn getTimeCreated() {
            return mTimeCreated;
        }

        public DbColumn getTimeModified() {
            return mTimeModified;
        }

        public DbColumn getZoom() {
            return mZoom;
        }
    }
}
