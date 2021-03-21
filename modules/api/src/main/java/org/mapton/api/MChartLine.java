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
package org.mapton.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Patrik Karlström
 */
public class MChartLine extends MChart {

    private ObservableList<String> mColumns = FXCollections.observableArrayList();
    private boolean mPlotSymbols = true;
    private String[] mTitles;
    private String mValueTitle;
    private ObservableList<Number>[] mValues;

    public MChartLine() {
    }

    public MChartLine(String chartTitle, String valueTitle, String... titles) {
        setChartTitle(chartTitle);
        setValueTitle(valueTitle);
        setTitles(titles);
    }

    public ObservableList<String> getColumns() {
        return mColumns;
    }

    public int getNumOfSeries() {
        return mTitles.length;
    }

    public String[] getTitles() {
        return mTitles;
    }

    public String getValueTitle() {
        return mValueTitle;
    }

    public ObservableList<Number>[] getValues() {
        return mValues;
    }

    public boolean isPlotSymbols() {
        return mPlotSymbols;
    }

    public void setColumns(ObservableList<String> columns) {
        mColumns = columns;
    }

    public void setPlotSymbols(boolean plotSymbols) {
        mPlotSymbols = plotSymbols;
    }

    public void setTitles(String... titles) {
        mTitles = titles;

        mValues = new ObservableList[mTitles.length];

        for (int i = 0; i < getNumOfSeries(); i++) {
            mValues[i] = FXCollections.observableArrayList();
        }
    }

    public void setValueTitle(String valueTitle) {
        mValueTitle = valueTitle;
    }

    public void setValues(ObservableList<Number>[] values) {
        mValues = values;
    }
}
