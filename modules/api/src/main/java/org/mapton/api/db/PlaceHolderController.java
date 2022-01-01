/* 
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.api.db;

import com.healthmarketscience.sqlbuilder.QueryPreparer;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.util.HashMap;

/**
 *
 * @author Patrik Karlström
 */
public class PlaceHolderController {

    private final HashMap<Object, QueryPreparer.PlaceHolder> mPlaceHolders = new HashMap<>();
    private QueryPreparer mQueryPreparer;

    public PlaceHolderController() {
        mPlaceHolders.clear();
        mQueryPreparer = new QueryPreparer();
    }

    public void add(Column... columns) {
        for (Column column : columns) {
            mPlaceHolders.put(column, mQueryPreparer.getNewPlaceHolder());
        }
    }

    public void add(String... columnNames) {
        for (String columnName : columnNames) {
            mPlaceHolders.put(columnName, mQueryPreparer.getNewPlaceHolder());
        }
    }

    public QueryPreparer.PlaceHolder get(Object column) {
        if (mPlaceHolders.containsKey(column)) {
            return mPlaceHolders.get(column);
        } else {
            String objectString = column.toString();
            if (column instanceof DbColumn) {
                objectString = ((DbColumn) column).getColumnNameSQL();
            }
            System.err.println(new NullPointerException("Column not in map: " + objectString));
            return null;
        }
    }

    public QueryPreparer getPreparer() {
        return mQueryPreparer;
    }

    public void init(Column... columns) {
        mPlaceHolders.clear();
        mQueryPreparer = new QueryPreparer();

        add(columns);
    }
}
